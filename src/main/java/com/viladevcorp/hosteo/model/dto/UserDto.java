package com.viladevcorp.hosteo.model.dto;

import com.viladevcorp.hosteo.model.User;
import com.viladevcorp.hosteo.model.types.ImageType;
import com.viladevcorp.hosteo.utils.ImageUrlResolver;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserDto extends BaseEntityDto {

  String email;

  String username;

  boolean validated;

  String imageUrl;

  public UserDto(User user) {
    if (user == null) {
      return;
    }
    this.email = user.getEmail();
    this.username = user.getUsername();
    this.validated = user.isValidated();
    this.imageUrl =
        ImageUrlResolver.resolve(ImageType.PROFILE, user.getUsername(), user.isHasImage());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    UserDto user = (UserDto) obj;
    return username.equals(user.username);
  }
}
