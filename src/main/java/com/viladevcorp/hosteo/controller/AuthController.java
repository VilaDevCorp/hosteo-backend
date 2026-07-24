package com.viladevcorp.hosteo.controller;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.viladevcorp.hosteo.auth.AuthResult;
import com.viladevcorp.hosteo.auth.AuthResultDto;
import com.viladevcorp.hosteo.exceptions.AlreadyUsedValidationCodeException;
import com.viladevcorp.hosteo.exceptions.EmailAlreadyInUseException;
import com.viladevcorp.hosteo.exceptions.EmptyFormFieldsException;
import com.viladevcorp.hosteo.exceptions.ExpiredValidationCodeException;
import com.viladevcorp.hosteo.exceptions.IncorrectValidationCodeException;
import com.viladevcorp.hosteo.exceptions.InvalidCredentialsException;
import com.viladevcorp.hosteo.exceptions.InvalidJwtException;
import com.viladevcorp.hosteo.exceptions.NotValidatedAccountException;
import com.viladevcorp.hosteo.exceptions.SendEmailException;
import com.viladevcorp.hosteo.exceptions.TokenAlreadyUsedException;
import com.viladevcorp.hosteo.exceptions.UserAlreadyValidatedException;
import com.viladevcorp.hosteo.exceptions.UsernameAlreadyInUseException;
import com.viladevcorp.hosteo.model.User;
import com.viladevcorp.hosteo.model.dto.UserDto;
import com.viladevcorp.hosteo.model.forms.LoginForm;
import com.viladevcorp.hosteo.model.forms.RegisterForm;
import com.viladevcorp.hosteo.repository.UserRepository;
import com.viladevcorp.hosteo.service.AuthService;
import com.viladevcorp.hosteo.utils.ApiResponse;
import com.viladevcorp.hosteo.utils.AuthUtils;
import com.viladevcorp.hosteo.utils.CodeErrors;
import com.viladevcorp.hosteo.utils.ValidationCodeType;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api")
public class AuthController {

  private final AuthService authService;
  private final UserRepository userRepository;

  @Autowired
  public AuthController(AuthService authService, UserRepository userRepository) {
    this.authService = authService;
    this.userRepository = userRepository;
  }

  @GetMapping("/public/health")
  public ResponseEntity<String> health() {
    return ResponseEntity.ok().body("API IS UP AND RUNNING");
  }

  @PostMapping("/public/register")
  public ResponseEntity<ApiResponse<UserDto>> registerUser(@RequestBody RegisterForm registerForm)
      throws EntityNotFoundException,
          SendEmailException,
          EmptyFormFieldsException,
          UserAlreadyValidatedException {
    try {
      User newUser =
          authService.registerUser(
              registerForm.getEmail(), registerForm.getUsername(), registerForm.getPassword());
      return ResponseEntity.ok().body(new ApiResponse<>(new UserDto(newUser)));

    } catch (EmailAlreadyInUseException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.EMAIL_IN_USE, e.getMessage()));

    } catch (UsernameAlreadyInUseException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.USERNAME_IN_USE, e.getMessage()));
    }
  }

  @PostMapping("/public/login")
  public ResponseEntity<ApiResponse<AuthResultDto>> login(
      @RequestBody LoginForm loginForm, HttpServletResponse response)
      throws EmptyFormFieldsException {
    AuthResult authResult;
    try {
      authResult =
          authService.authenticate(
              loginForm.getUsername(), loginForm.getPassword(), loginForm.isRememberMe());
      if (authResult == null) {
        throw new InternalError();
      }

    } catch (InvalidCredentialsException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new ApiResponse<>(CodeErrors.INVALID_CREDENTIALS, e.getMessage()));
    } catch (NotValidatedAccountException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(new ApiResponse<>(CodeErrors.NOT_VALIDATED_ACCOUNT, e.getMessage()));
    }
    return authService.processAuthResult(authResult, response);
  }

  @GetMapping("/self")
  public ResponseEntity<ApiResponse<UserDto>> self() {
    String selfUsername = AuthUtils.getUsername();
    User selfUser = userRepository.findByUsername(selfUsername);
    return ResponseEntity.ok().body(new ApiResponse<>(new UserDto(selfUser)));
  }

  @PostMapping("/public/validate/{username}/{validationCode}")
  public ResponseEntity<ApiResponse<Void>> validateAccount(
      @PathVariable String username, @PathVariable String validationCode)
      throws EntityNotFoundException, EmptyFormFieldsException {
    if (username == null || validationCode == null) {
      throw new EmptyFormFieldsException();
    }
    try {
      authService.activateAccount(username, validationCode);
    } catch (ExpiredValidationCodeException e) {
      return ResponseEntity.status(HttpStatus.GONE)
          .body(new ApiResponse<>(CodeErrors.EXPIRED_VALIDATION_CODE, e.getMessage()));
    } catch (AlreadyUsedValidationCodeException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.USED_VALIDATION_CODE, e.getMessage()));
    } catch (IncorrectValidationCodeException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new ApiResponse<>(CodeErrors.INCORRECT_VALIDATION_CODE, e.getMessage()));
    }
    return ResponseEntity.ok().body(new ApiResponse<>());
  }

  @PostMapping("/public/validate/{username}/resend")
  public ResponseEntity<ApiResponse<Void>> resendValidationCode(@PathVariable String username)
      throws EmptyFormFieldsException, EntityNotFoundException, SendEmailException {
    if (username == null) {
      throw new EmptyFormFieldsException();
    }
    try {
      authService.createValidationCode(username, ValidationCodeType.ACTIVATE_ACCOUNT);

    } catch (UserAlreadyValidatedException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.VALIDATED_ACCOUNT, e.getMessage()));
    }

    return ResponseEntity.ok().body(new ApiResponse<>());
  }

  @PostMapping("/public/forgotten-password/{username}")
  public ResponseEntity<ApiResponse<Void>> sendResetPasswordCode(@PathVariable String username)
      throws EntityNotFoundException,
          SendEmailException,
          EmptyFormFieldsException,
          UserAlreadyValidatedException {
    if (username == null) {
      throw new EmptyFormFieldsException();
    }
    authService.createValidationCode(username, ValidationCodeType.RESET_PASSWORD);
    return ResponseEntity.ok().body(new ApiResponse<>());
  }

  @PostMapping("/public/reset-password/{username}/{validationCode}")
  public ResponseEntity<ApiResponse<Void>> resetPassword(
      @PathVariable String username,
      @PathVariable String validationCode,
      @RequestBody String newPassword)
      throws EntityNotFoundException, EmptyFormFieldsException {
    if (username == null || validationCode == null) {
      throw new EmptyFormFieldsException();
    }
    try {
      authService.resetPassword(username, validationCode, newPassword);
    } catch (ExpiredValidationCodeException e) {
      return ResponseEntity.status(HttpStatus.GONE)
          .body(new ApiResponse<>(CodeErrors.EXPIRED_VALIDATION_CODE, e.getMessage()));
    } catch (AlreadyUsedValidationCodeException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.USED_VALIDATION_CODE, e.getMessage()));
    } catch (IncorrectValidationCodeException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new ApiResponse<>(CodeErrors.INCORRECT_VALIDATION_CODE, e.getMessage()));
    }
    return ResponseEntity.ok().body(new ApiResponse<>());
  }

  @PostMapping("/public/refresh-token")
  public ResponseEntity<ApiResponse<AuthResultDto>> refreshToken(
      @CookieValue("REFRESH_TOKEN") String refreshToken, HttpServletResponse response) {
    if (refreshToken == null || refreshToken.isEmpty()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(new ApiResponse<>(CodeErrors.NOT_REFRESH_JWT_TOKEN, "Refresh token is empty"));
    }
    AuthResult result;
    try {
      result = authService.refreshToken(refreshToken);
    } catch (InvalidJwtException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new ApiResponse<>(CodeErrors.INVALID_TOKEN, e.getMessage()));
    } catch (TokenAlreadyUsedException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new ApiResponse<>(CodeErrors.TOKEN_USED, e.getMessage()));
    }
    return authService.processAuthResult(result, response);
  }

  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout() {
    authService.logout();
    return ResponseEntity.ok().body(new ApiResponse<>());
  }
}
