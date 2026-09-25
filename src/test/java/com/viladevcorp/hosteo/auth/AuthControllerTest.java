package com.viladevcorp.hosteo.auth;

import static com.viladevcorp.hosteo.common.TestConstants.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viladevcorp.hosteo.common.BaseControllerTest;
import com.viladevcorp.hosteo.common.TestUtils;
import com.viladevcorp.hosteo.model.User;
import com.viladevcorp.hosteo.model.UserSession;
import com.viladevcorp.hosteo.model.ValidationCode;
import com.viladevcorp.hosteo.model.dto.UserDto;
import com.viladevcorp.hosteo.model.forms.LoginForm;
import com.viladevcorp.hosteo.model.forms.RegisterForm;
import com.viladevcorp.hosteo.repository.UserRepository;
import com.viladevcorp.hosteo.repository.UserSessionRepository;
import com.viladevcorp.hosteo.repository.ValidationCodeRepository;
import com.viladevcorp.hosteo.utils.ApiResponse;
import com.viladevcorp.hosteo.utils.CodeErrors;
import com.viladevcorp.hosteo.utils.ValidationCodeType;

import jakarta.servlet.http.Cookie;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest extends BaseControllerTest {

  private static final String INACTIVE_USER_USERNAME = ACTIVE_USER_USERNAME_2;
  private static final String INACTIVE_USER_PASSWORD = ACTIVE_USER_PASSWORD_2;

  @Autowired private UserRepository userRepository;

  @Autowired private ValidationCodeRepository validationCodeRepository;

  @Autowired private UserSessionRepository userSessionRepository;

  @Autowired private JwtUtils jwtUtils;

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void When_ApiHealth_Ok() throws Exception {
    mockMvc.perform(get("/api/public/health")).andExpect(status().isOk());
  }

  @Nested
  @DisplayName("User register")
  class UserRegister {
    @Test
    void When_RegisterUser_Ok() throws Exception {
      RegisterForm form = new RegisterForm(NEW_USER_EMAIL, NEW_USER_USERNAME, NEW_USER_PASSWORD);

      String resultString =
          mockMvc
              .perform(
                  post("/api/public/register")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(form)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();
      ApiResponse<User> result = null;
      TypeReference<ApiResponse<User>> typeReference = new TypeReference<ApiResponse<User>>() {};

      try {
        result = objectMapper.readValue(resultString, typeReference);
      } catch (Exception e) {
        fail("Error parsing response");
      }

      String username = result.getData().getUsername();
      User user = userRepository.findByUsername(username);
      PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
      assertEquals(NEW_USER_EMAIL, user.getEmail());
      assertEquals(NEW_USER_USERNAME, user.getUsername());
      assertTrue(passwordEncoder.matches(NEW_USER_PASSWORD, user.getPassword()));
      userRepository.delete(user);
    }

    @Test
    void When_RegisterAlreadyRegisterMail_Conflict() throws Exception {
      RegisterForm form = new RegisterForm(ACTIVE_USER_EMAIL_1, NEW_USER_EMAIL, NEW_USER_PASSWORD);

      String resultString =
          mockMvc
              .perform(
                  post("/api/public/register")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(form)))
              .andExpect(status().isConflict())
              .andReturn()
              .getResponse()
              .getContentAsString();

      ApiResponse<User> result = null;
      TypeReference<ApiResponse<User>> typeReference = new TypeReference<ApiResponse<User>>() {};

      try {
        result = objectMapper.readValue(resultString, typeReference);
      } catch (Exception e) {
        fail("Error parsing response");
      }

      // We remove the quotes from the UUID (extra quotes being added)
      String errorCode = result.getErrorCode();
      assertEquals(CodeErrors.EMAIL_IN_USE, errorCode);
    }

    @Test
    void When_RegisterAlreadyRegisterUsername_Conflict() throws Exception {
      RegisterForm form =
          new RegisterForm(NEW_USER_EMAIL, ACTIVE_USER_USERNAME_1, NEW_USER_PASSWORD);

      String resultString =
          mockMvc
              .perform(
                  post("/api/public/register")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(form)))
              .andExpect(status().isConflict())
              .andReturn()
              .getResponse()
              .getContentAsString();

      ApiResponse<User> result = null;
      TypeReference<ApiResponse<User>> typeReference = new TypeReference<ApiResponse<User>>() {};

      try {
        result = objectMapper.readValue(resultString, typeReference);
      } catch (Exception e) {
        fail("Error parsing response");
      }

      // We remove the quotes from the UUID (extra quotes being added)
      String errorCode = result.getErrorCode();
      assertEquals(CodeErrors.USERNAME_IN_USE, errorCode);
    }

    @Test
    void When_RegisterEmptyMandatoryFields_BadRequest() throws Exception {
      RegisterForm form1 = new RegisterForm(null, NEW_USER_USERNAME, NEW_USER_PASSWORD);
      RegisterForm form2 = new RegisterForm(NEW_USER_EMAIL, null, NEW_USER_PASSWORD);
      RegisterForm form3 = new RegisterForm(NEW_USER_EMAIL, NEW_USER_USERNAME, null);

      mockMvc
          .perform(
              post("/api/public/register")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form1)))
          .andExpect(status().isBadRequest());
      mockMvc
          .perform(
              post("/api/public/register")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form2)))
          .andExpect(status().isBadRequest());

      mockMvc
          .perform(
              post("/api/public/register")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form3)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("User login")
  class UserLogin {

    @Test
    void When_LoginEmptyFields_BadRequest() throws Exception {
      LoginForm form1 = new LoginForm(null, ACTIVE_USER_PASSWORD_1, false);
      LoginForm form2 = new LoginForm(ACTIVE_USER_USERNAME_1, null, false);

      mockMvc
          .perform(
              post("/api/public/login")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form1)))
          .andExpect(status().isBadRequest());
      mockMvc
          .perform(
              post("/api/public/login")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form2)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void When_LoginInvalidCredentials_Unauthorized() throws Exception {
      LoginForm form = new LoginForm(ACTIVE_USER_USERNAME_1, NEW_USER_PASSWORD, false);

      mockMvc
          .perform(
              post("/api/public/login")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form)))
          .andExpect(status().isUnauthorized());
    }

    @Test
    void When_LoginSuccesful_Ok() throws Exception {
      LoginForm form = new LoginForm(ACTIVE_USER_USERNAME_1, ACTIVE_USER_PASSWORD_1, false);

      String resultString =
          mockMvc
              .perform(
                  post("/api/public/login")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(form)))
              .andExpect(status().isOk())
              .andExpect(cookie().exists("REFRESH_TOKEN"))
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<AuthResultDto>> typeReference =
          new TypeReference<ApiResponse<AuthResultDto>>() {};
      ApiResponse<AuthResultDto> result;
      try {
        result = objectMapper.readValue(resultString, typeReference);
        UUID sessionId = result.getData().getSessionId();
        String authToken = result.getData().getAuthToken();
        Authentication auth = jwtUtils.validateToken(authToken);
        assertEquals(ACTIVE_USER_USERNAME_1, auth.getName());
        assertEquals(
            sessionId.toString(), jwtUtils.extractClaims(authToken).get("sessionId", String.class));
      } catch (Exception e) {
        fail("Error parsing response");
      }
    }
  }

  @Test
  void When_RefreshSuccesful_Ok() throws Exception {
    LoginForm form = new LoginForm(ACTIVE_USER_USERNAME_1, ACTIVE_USER_PASSWORD_1, false);

    MockHttpServletResponse response =
        mockMvc
            .perform(
                post("/api/public/login")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
            .andReturn()
            .getResponse();
    String resultLoginString = response.getContentAsString();
    Cookie refreshTokenCookie = response.getCookie("REFRESH_TOKEN");

    TypeReference<ApiResponse<AuthResultDto>> typeReference =
        new TypeReference<ApiResponse<AuthResultDto>>() {};
    ApiResponse<AuthResultDto> resultLogin;
    resultLogin = objectMapper.readValue(resultLoginString, typeReference);
    UUID sessionId = resultLogin.getData().getSessionId();

    String resultString =
        mockMvc
            .perform(post("/api/public/refresh-token").cookie(refreshTokenCookie))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    UserSession oldSession = userSessionRepository.findById(sessionId).orElse(null);
    assertNotNull(oldSession);
    assertNotNull(oldSession.getDeletedAt());
    ApiResponse<AuthResultDto> result;
    try {
      result = objectMapper.readValue(resultString, typeReference);
      UUID sessionId2 = result.getData().getSessionId();
      String authToken = result.getData().getAuthToken();
      Authentication auth = jwtUtils.validateToken(authToken);
      assertEquals(ACTIVE_USER_USERNAME_1, auth.getName());
      assertEquals(
          sessionId2.toString(), jwtUtils.extractClaims(authToken).get("sessionId", String.class));
    } catch (Exception e) {
      fail("Error parsing response");
    }
  }

//  @Nested
//  @DisplayName("Account validation")
//  class AccountValidation {
//
//    @BeforeEach
//    void initValidationSetup() throws Exception {
//      testSetupHelper.resetTestBase();
//      User invalidatedUser = testSetupHelper.getTestUsers().get(1);
//      invalidatedUser.setValidated(false);
//      userRepository.save(invalidatedUser);
//    }
//
//    @Test
//    void When_LoginNotActivatedAccount_Forbidden() throws Exception {
//      LoginForm form = new LoginForm(INACTIVE_USER_USERNAME, INACTIVE_USER_PASSWORD, false);
//
//      mockMvc
//          .perform(
//              post("/api/public/login")
//                  .contentType("application/json")
//                  .content(objectMapper.writeValueAsString(form)))
//          .andExpect(status().isForbidden());
//    }
//
//    @Test
//    void When_AccountValidationWrongCode_Unauthorized() throws Exception {
//      ValidationCode validationCode =
//          validationCodeRepository
//              .findByUserUsernameAndTypeOrderByCreatedAtDesc(
//                  INACTIVE_USER_USERNAME, ValidationCodeType.ACTIVATE_ACCOUNT.getType())
//              .get(0);
//      mockMvc
//          .perform(
//              post(
//                  "/api/public/validate/"
//                      + INACTIVE_USER_USERNAME
//                      + "/"
//                      + validationCode.getCode()
//                      + "1"))
//          .andExpect(status().isUnauthorized());
//    }
//
//    @Test
//    void When_AccountValidationExpiredCode_Gone() throws Exception {
//      List<ValidationCode> validationCodeList =
//          validationCodeRepository.findByUserUsernameAndTypeOrderByCreatedAtDesc(
//              INACTIVE_USER_USERNAME, ValidationCodeType.ACTIVATE_ACCOUNT.getType());
//      // We take the first validation code (more recent) and we delete the rest to
//      // avoid conflicts with other tests
//      ValidationCode validationCode = validationCodeList.get(0);
//      List<ValidationCode> validationCodeToDelete =
//          validationCodeList.subList(1, validationCodeList.size());
//      validationCodeToDelete.forEach(vc -> validationCodeRepository.delete(vc));
//      Instant oldCreationDate = validationCode.getCreatedAt();
//      Instant expiredDate =
//          oldCreationDate.minusSeconds((ValidationCode.EXPIRATION_MINUTES + 1) * 60);
//      validationCode.setCreatedAt(expiredDate);
//      validationCodeRepository.save(validationCode);
//      mockMvc
//          .perform(
//              post(
//                  "/api/public/validate/"
//                      + INACTIVE_USER_USERNAME
//                      + "/"
//                      + validationCode.getCode()))
//          .andExpect(status().isGone());
//      validationCode.setCreatedAt(oldCreationDate);
//      validationCodeRepository.save(validationCode);
//    }
//
//    @Test
//    void When_AccountValidationAlreadyUsedCode_Conflict() throws Exception {
//      ValidationCode validationCode =
//          validationCodeRepository
//              .findByUserUsernameAndTypeOrderByCreatedAtDesc(
//                  INACTIVE_USER_USERNAME, ValidationCodeType.ACTIVATE_ACCOUNT.getType())
//              .get(0);
//      validationCode.setUsed(true);
//      validationCodeRepository.save(validationCode);
//      mockMvc
//          .perform(
//              post(
//                  "/api/public/validate/"
//                      + INACTIVE_USER_USERNAME
//                      + "/"
//                      + validationCode.getCode()))
//          .andExpect(status().isConflict());
//    }
//
//    @Test
//    void When_AccountValidationSuccesful_Ok() throws Exception {
//      ValidationCode validationCode =
//          validationCodeRepository
//              .findByUserUsernameAndTypeOrderByCreatedAtDesc(
//                  INACTIVE_USER_USERNAME, ValidationCodeType.ACTIVATE_ACCOUNT.getType())
//              .get(0);
//      mockMvc
//          .perform(
//              post(
//                  "/api/public/validate/"
//                      + INACTIVE_USER_USERNAME
//                      + "/"
//                      + validationCode.getCode()))
//          .andExpect(status().isOk());
//      User user = userRepository.findByUsername(INACTIVE_USER_USERNAME);
//      assertTrue(user.isValidated());
//      validationCode =
//          validationCodeRepository
//              .findByUserUsernameAndTypeOrderByCreatedAtDesc(
//                  INACTIVE_USER_USERNAME, ValidationCodeType.ACTIVATE_ACCOUNT.getType())
//              .get(0);
//      assertTrue(validationCode.isUsed());
//    }
//  }

  @Test
  void When_ResetPassword_Ok() throws Exception {
    mockMvc
        .perform(post("/api/public/forgotten-password/" + ACTIVE_USER_USERNAME_1))
        .andExpect(status().isOk());

    ValidationCode validationCode =
        validationCodeRepository
            .findByUserUsernameAndTypeOrderByCreatedAtDesc(
                ACTIVE_USER_USERNAME_1, ValidationCodeType.RESET_PASSWORD.getType())
            .get(0);

    mockMvc
        .perform(
            post("/api/public/reset-password/"
                    + ACTIVE_USER_USERNAME_1
                    + "/"
                    + validationCode.getCode())
                .content(NEW_USER_PASSWORD))
        .andExpect(status().isOk());

    LoginForm formOldPass = new LoginForm(ACTIVE_USER_USERNAME_1, ACTIVE_USER_PASSWORD_1, false);
    LoginForm formNewPass = new LoginForm(ACTIVE_USER_USERNAME_1, NEW_USER_PASSWORD, false);

    mockMvc
        .perform(
            post("/api/public/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(formOldPass)))
        .andExpect(status().isUnauthorized());
    mockMvc
        .perform(
            post("/api/public/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(formNewPass)))
        .andExpect(status().isOk());
  }

  @Test
  void When_Self_Ok() throws Exception {
    TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
    String resultString =
        mockMvc
            .perform(get("/api/self"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    ApiResponse<UserDto> result = null;
    TypeReference<ApiResponse<UserDto>> typeReference =
        new TypeReference<ApiResponse<UserDto>>() {};

    try {
      result = objectMapper.readValue(resultString, typeReference);
    } catch (Exception e) {
      fail("Error parsing response");
    }
    UserDto user = result.getData();
    assertEquals(ACTIVE_USER_EMAIL_1, user.getEmail());
    assertEquals(ACTIVE_USER_USERNAME_1, user.getUsername());
  }
}
