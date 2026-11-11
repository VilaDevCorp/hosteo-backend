package com.viladevcorp.hosteo.service;

import com.viladevcorp.hosteo.exceptions.FileTooBigException;
import com.viladevcorp.hosteo.exceptions.FileTypeNotSupportedException;
import com.viladevcorp.hosteo.model.types.ImageType;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class MinioService {
  private static final long MAX_IMAGE_SIZE_BYTES = 5000000L;
  private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/png", "image/jpeg");
  private static final int URL_EXPIRY_HOURS = 2;

  private final MinioClient minioClient;
  private volatile boolean bucketReady = false;

  @Value("${minio.bucket}")
  private String minioBucket;

  @Autowired
  public MinioService(
      @Value("${minio.url}") String minioUrl,
      @Value("${minio.access-key}") String minioAccessKey,
      @Value("${minio.secret-key}") String minioSecretKey) {
    this(
        MinioClient.builder()
            .endpoint(minioUrl)
            .credentials(minioAccessKey, minioSecretKey)
            .build());
  }

  MinioService(MinioClient minioClient) {
    this.minioClient = minioClient;
  }

  @PostConstruct
  void initBucket() {
    try {
      ensureBucketExists();
      log.info("Bucket '{}' is ready", minioBucket);
    } catch (Exception e) {
      log.error("Could not verify or create bucket '{}'", minioBucket, e);
    }
  }

  private void ensureBucketExists() throws Exception {
    if (bucketReady) {
      return;
    }
    if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioBucket).build())) {
      try {
        minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioBucket).build());
      } catch (ErrorResponseException e) {
        String code = e.errorResponse().code();
        if (!"BucketAlreadyOwnedByYou".equals(code) && !"BucketAlreadyExists".equals(code)) {
          throw e;
        }
      }
    }
    bucketReady = true;
  }

  /**
   * Stores the given image for the owner identified by {@code id}. If {@code image} is null or
   * empty, the current image is removed instead.
   */
  public void updateImage(ImageType type, String id, MultipartFile image)
      throws FileTooBigException, FileTypeNotSupportedException, MinioException {
    String objectName = type.objectName(id);
    try {
      ensureBucketExists();
      if (image == null || image.isEmpty()) {
        removeObject(objectName);
        return;
      }
      validateImage(image, type);
      minioClient.putObject(
          PutObjectArgs.builder()
              .bucket(minioBucket)
              .object(objectName)
              .contentType(image.getContentType())
              .stream(image.getInputStream(), image.getSize(), -1)
              .build());
    } catch (FileTooBigException | FileTypeNotSupportedException e) {
      throw e;
    } catch (Exception e) {
      throw new MinioException("Error uploading " + type.name().toLowerCase() + " image");
    }
  }

  /** Returns a presigned URL without checking whether the object exists. */
  public String getImageUrl(ImageType type, String id) throws MinioException {
    try {
      ensureBucketExists();
      return createPresignedUrl(type.objectName(id));
    } catch (Exception e) {
      throw new MinioException("Error getting " + type.name().toLowerCase() + " image");
    }
  }

  /** Returns a presigned URL, or {@code null} when no image is stored for the owner. */
  public String getImageUrlIfExists(ImageType type, String id) throws MinioException {
    try {
      ensureBucketExists();
      String objectName = type.objectName(id);
      try {
        minioClient.statObject(
            StatObjectArgs.builder().bucket(minioBucket).object(objectName).build());
      } catch (Exception e) {
        return null;
      }
      return createPresignedUrl(objectName);
    } catch (Exception e) {
      throw new MinioException("Error getting " + type.name().toLowerCase() + " image");
    }
  }

  /** Best-effort removal that never fails the caller. */
  public void deleteImage(ImageType type, String id) {
    try {
      ensureBucketExists();
      removeObject(type.objectName(id));
    } catch (Exception e) {
      log.warn("Could not delete {} image for {}", type, id, e);
    }
  }

  private String createPresignedUrl(String objectName) throws Exception {
    return minioClient.getPresignedObjectUrl(
        GetPresignedObjectUrlArgs.builder()
            .bucket(minioBucket)
            .object(objectName)
            .method(Method.GET)
            .expiry(URL_EXPIRY_HOURS, TimeUnit.HOURS)
            .build());
  }

  private void removeObject(String objectName) throws Exception {
    minioClient.removeObject(
        RemoveObjectArgs.builder().bucket(minioBucket).object(objectName).build());
  }

  private void validateImage(MultipartFile image, ImageType type)
      throws FileTooBigException, FileTypeNotSupportedException {
    if (image.getSize() > MAX_IMAGE_SIZE_BYTES) {
      throw new FileTooBigException(type.name().toLowerCase() + " image is too big");
    }
    if (image.getContentType() == null
        || !ALLOWED_CONTENT_TYPES.contains(image.getContentType())) {
      throw new FileTypeNotSupportedException(
          type.name().toLowerCase() + " image type not supported");
    }
  }
}
