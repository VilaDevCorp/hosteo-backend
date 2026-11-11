package com.viladevcorp.hosteo.utils;

import com.viladevcorp.hosteo.model.types.ImageType;
import com.viladevcorp.hosteo.service.MinioService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * DTOs are created in many places (including nested DTOs), so the MinIO client is exposed through
 * a static reference instead of threading it through every constructor.
 */
@Slf4j
@Component
public class ImageUrlResolver {

  private static MinioService minioService;

  @Autowired
  public ImageUrlResolver(MinioService minioService) {
    ImageUrlResolver.minioService = minioService;
  }

  /** {@code id} must match the identifier used when the image was stored, e.g. the username. */
  public static String resolve(ImageType type, String id, boolean hasImage) {
    if (!hasImage || id == null || minioService == null) {
      return null;
    }
    try {
      return minioService.getImageUrl(type, id);
    } catch (Exception e) {
      log.warn("Could not generate {} image url for {}", type, id, e);
      return null;
    }
  }
}
