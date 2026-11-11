package com.viladevcorp.hosteo.service;

import com.viladevcorp.hosteo.exceptions.FileTooBigException;
import com.viladevcorp.hosteo.exceptions.FileTypeNotSupportedException;
import com.viladevcorp.hosteo.model.User;
import com.viladevcorp.hosteo.model.types.ImageType;
import com.viladevcorp.hosteo.repository.UserRepository;
import com.viladevcorp.hosteo.utils.AuthUtils;
import io.minio.errors.MinioException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserService {

  private final UserRepository userRepository;
  private final MinioService minioService;

  @Autowired
  public UserService(UserRepository userRepository, MinioService minioService) {
    this.userRepository = userRepository;
    this.minioService = minioService;
  }

  public String updateProfileImage(MultipartFile image)
      throws FileTooBigException, FileTypeNotSupportedException, MinioException {
    User user = getAuthenticatedUser();
    String username = user.getUsername();
    minioService.updateImage(ImageType.PROFILE, username, image);
    user.setHasImage(image != null && !image.isEmpty());
    userRepository.save(user);
    return user.isHasImage() ? minioService.getImageUrl(ImageType.PROFILE, username) : null;
  }

  public String getProfileImageUrl() throws MinioException {
    User user = getAuthenticatedUser();
    return minioService.getImageUrlIfExists(ImageType.PROFILE, user.getUsername());
  }

  private User getAuthenticatedUser() {
    User user = userRepository.findByUsername(AuthUtils.getUsername());
    if (user == null) {
      throw new EntityNotFoundException("User not found");
    }
    return user;
  }
}
