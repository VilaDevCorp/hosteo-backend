package com.viladevcorp.hosteo.service;

import com.viladevcorp.hosteo.exceptions.FileTooBigException;
import com.viladevcorp.hosteo.exceptions.FileTypeNotSupportedException;
import com.viladevcorp.hosteo.model.types.ImageType;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MinioServiceTest {

  @Mock private MinioClient minioClient;

  private MinioService minioService;

  @BeforeEach
  void setUp() throws Exception {
    minioService = new MinioService(minioClient);
    ReflectionTestUtils.setField(minioService, "minioBucket", "test-bucket");
    lenient().when(minioClient.bucketExists(any())).thenReturn(true);
  }

  @Test
  @DisplayName("Object keys are prefixed by image type")
  void When_BuildObjectName_IncludesTypePrefix() {
    UUID id = UUID.randomUUID();

    assertEquals("profile_" + id, ImageType.PROFILE.objectName(id.toString()));
    assertEquals("apartment_" + id, ImageType.APARTMENT.objectName(id.toString()));
    assertEquals("worker_" + id, ImageType.WORKER.objectName(id.toString()));
  }

  @Test
  @DisplayName("Uploading a valid image stores it")
  void When_UpdateImage_Ok() throws Exception {
    MockMultipartFile image = new MockMultipartFile("image", "a.png", "image/png", new byte[10]);
    String id = UUID.randomUUID().toString();

    minioService.updateImage(ImageType.WORKER, id, image);

    verify(minioClient).putObject(any(PutObjectArgs.class));
  }

  @Test
  @DisplayName("Uploading a null image removes the stored object")
  void When_UpdateImageWithNull_RemovesObject() throws Exception {
    String id = UUID.randomUUID().toString();

    minioService.updateImage(ImageType.APARTMENT, id, null);

    verify(minioClient).removeObject(any(RemoveObjectArgs.class));
    verify(minioClient, never()).putObject(any(PutObjectArgs.class));
  }

  @Test
  @DisplayName("Uploading an image over 5MB is rejected")
  void When_UpdateImageTooBig_ThrowsFileTooBig() {
    MockMultipartFile image =
        new MockMultipartFile("image", "a.png", "image/png", new byte[5_000_001]);

    assertThrows(
        FileTooBigException.class,
        () -> minioService.updateImage(ImageType.PROFILE, "user", image));
  }

  @Test
  @DisplayName("Uploading a non image file is rejected")
  void When_UpdateImageWithUnsupportedType_Throws() {
    MockMultipartFile image = new MockMultipartFile("image", "a.gif", "image/gif", new byte[10]);

    assertThrows(
        FileTypeNotSupportedException.class,
        () -> minioService.updateImage(ImageType.PROFILE, "user", image));
  }

  @Test
  @DisplayName("Getting an image url signs the type key")
  void When_GetImageUrl_ReturnsPresignedUrl() throws Exception {
    String id = UUID.randomUUID().toString();
    when(minioClient.getPresignedObjectUrl(any())).thenReturn("http://minio/signed");

    String url = minioService.getImageUrl(ImageType.APARTMENT, id);

    assertEquals("http://minio/signed", url);
    ArgumentCaptor<GetPresignedObjectUrlArgs> captor =
        ArgumentCaptor.forClass(GetPresignedObjectUrlArgs.class);
    verify(minioClient).getPresignedObjectUrl(captor.capture());
    assertEquals("apartment_" + id, captor.getValue().object());
    assertEquals(Method.GET, captor.getValue().method());
  }

  @Test
  @DisplayName("Getting an url for a missing image returns null")
  void When_GetImageUrlIfExistsNotStored_ReturnsNull() throws Exception {
    when(minioClient.statObject(any())).thenThrow(new IOException("not found"));

    assertNull(minioService.getImageUrlIfExists(ImageType.WORKER, UUID.randomUUID().toString()));
  }

  @Test
  @DisplayName("Getting an url for an existing image returns it")
  void When_GetImageUrlIfExistsStored_ReturnsUrl() throws Exception {
    when(minioClient.statObject(any())).thenReturn(null);
    when(minioClient.getPresignedObjectUrl(any())).thenReturn("http://minio/signed");

    String url = minioService.getImageUrlIfExists(ImageType.PROFILE, "john");

    assertEquals("http://minio/signed", url);
  }

  @Test
  @DisplayName("Presigning failures surface as MinioException")
  void When_GetImageUrlFails_ThrowsMinioException() throws Exception {
    when(minioClient.getPresignedObjectUrl(any())).thenThrow(new IOException("boom"));

    MinioException exception =
        assertThrows(
            MinioException.class, () -> minioService.getImageUrl(ImageType.PROFILE, "john"));

    assertTrue(exception.getMessage().contains("profile"));
  }
}
