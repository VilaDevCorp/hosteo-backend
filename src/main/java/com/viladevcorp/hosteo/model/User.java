package com.viladevcorp.hosteo.model;

import java.util.HashSet;
import java.util.Set;

import com.viladevcorp.hosteo.model.dto.UserDto;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User extends BaseEntity implements UserDetails {

  @Column(nullable = false, unique = true)
  @NotNull
  private String email;

  @Column(nullable = false, unique = true)
  @NotNull
  private String username;

  @Column(nullable = false)
  @NotNull
  @JsonIgnore
  private String password;

  @JsonIgnore private boolean validated = false;

  private boolean hasImage = false;

  @OneToMany(mappedBy = "user")
  @JsonIgnore
  Set<ValidationCode> validationCodes = new HashSet<>();

  @OneToMany(mappedBy = "user")
  @JsonIgnore
  Set<UserSession> userSessions = new HashSet<>();

  public User(String email, String username, String password) {
    this.email = email;
    this.username = username;
    this.password = password;
  }

  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    User user = (User) obj;
    return username.equals(user.username);
  }

  public int hashCode() {
    return username.hashCode();
  }

  @Override
  @JsonIgnore
  public Set<GrantedAuthority> getAuthorities() {
    return Set.of();
  }

  @Override
  @JsonIgnore
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  @JsonIgnore
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  @JsonIgnore
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  @JsonIgnore
  public boolean isEnabled() {
    return true;
  }

  @Override
  public UserDto toDto() {
    return new UserDto(this);
  }
}
