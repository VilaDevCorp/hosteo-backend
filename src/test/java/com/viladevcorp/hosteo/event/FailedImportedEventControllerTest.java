package com.viladevcorp.hosteo.event;

import static com.viladevcorp.hosteo.common.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viladevcorp.hosteo.common.BaseControllerTest;
import com.viladevcorp.hosteo.common.TestSetupHelper;
import com.viladevcorp.hosteo.common.TestUtils;
import com.viladevcorp.hosteo.model.dto.ImportResultDto;
import com.viladevcorp.hosteo.model.dto.FailedImportedEventDto;
import com.viladevcorp.hosteo.model.forms.FailedImportedEventUpdateForm;
import com.viladevcorp.hosteo.repository.UserRepository;
import com.viladevcorp.hosteo.utils.ApiResponse;
import com.viladevcorp.hosteo.utils.CodeErrors;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

class FailedImportedEventControllerTest extends BaseControllerTest {

  @Autowired private UserRepository userRepository;

  @Autowired private MockMvc mockMvc;

  @Autowired TestSetupHelper testSetupHelper;

  @Autowired private ObjectMapper objectMapper;

  @BeforeEach
  void setup() {
    testSetupHelper.resetImportApartments();
  }

  private MockMultipartFile fileFor(String path) throws Exception {
    File importFile = new File(path);
    return new MockMultipartFile(
        "file", importFile.getName(), "text/csv", new FileInputStream(importFile));
  }

  @Nested
  @DisplayName("Upload airbnb bookings")
  class UploadAirbnb {

    @Test
    void When_UploadAirbnb_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      String resultString =
          mockMvc
              .perform(
                  multipart("/api/imported-events/upload")
                      .file(fileFor("src/test/resources/import_airbnb.csv"))
                      .param("source", "AIRBNB"))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      ImportResultDto result =
          objectMapper
              .readValue(
                  resultString, new TypeReference<ApiResponse<ImportResultDto>>() {})
              .getData();
      assertEquals(9, result.getSuccessCount());
      assertEquals(1, result.getFailedEvents().size());
      assertEquals(CodeErrors.UNRECOGNIZED_APARTMENT_ID, result.getFailedEvents().get(0).getError());
      assertTrue(result.getFailedEvents().get(0).getName().startsWith("Oliver Dvorsk"));
    }
  }

  @Nested
  @DisplayName("Upload airbnb bookings with conflicts")
  class UploadAirbnbWithConflicts {

    @Test
    void When_UploadAirbnbWithConflicts_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      testSetupHelper.resetImportConflicts(true);

      String resultString =
          mockMvc
              .perform(
                  multipart("/api/imported-events/upload")
                      .file(fileFor("src/test/resources/import_airbnb_with_conflicts.csv"))
                      .param("source", "AIRBNB"))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      ImportResultDto result =
          objectMapper
              .readValue(
                  resultString, new TypeReference<ApiResponse<ImportResultDto>>() {})
              .getData();
      assertEquals(6, result.getSuccessCount());

      Map<String, Long> errorsByCode =
          result.getFailedEvents().stream()
              .collect(
                  Collectors.groupingBy(FailedImportedEventDto::getError, Collectors.counting()));
      assertEquals(4, result.getFailedEvents().size());
      assertEquals(1L, errorsByCode.getOrDefault(CodeErrors.UNRECOGNIZED_APARTMENT_ID, 0L));
      assertEquals(3L, errorsByCode.getOrDefault(CodeErrors.NOT_AVAILABLE_DATES, 0L));
    }
  }

  @Nested
  @DisplayName("Upload booking bookings")
  class UploadBooking {

    @Test
    void When_UploadBooking_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      String resultString =
          mockMvc
              .perform(
                  multipart("/api/imported-events/upload")
                      .file(fileFor("src/test/resources/import_booking.csv"))
                      .param("source", "BOOKING"))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      ImportResultDto result =
          objectMapper
              .readValue(
                  resultString, new TypeReference<ApiResponse<ImportResultDto>>() {})
              .getData();
      assertEquals(7, result.getSuccessCount());
      assertEquals(2, result.getFailedEvents().size());
      result
          .getFailedEvents()
          .forEach(
              failed -> assertEquals(CodeErrors.UNRECOGNIZED_APARTMENT_ID, failed.getError()));
    }
  }

  @Nested
  @DisplayName("Manage imported events")
  class ManageFailedImportedEvents {

    @Test
    void When_ListPatchRetryDismiss_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      String uploadString =
          mockMvc
              .perform(
                  multipart("/api/imported-events/upload")
                      .file(fileFor("src/test/resources/import_airbnb.csv"))
                      .param("source", "AIRBNB"))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      ImportResultDto uploadResult =
          objectMapper
              .readValue(
                  uploadString, new TypeReference<ApiResponse<ImportResultDto>>() {})
              .getData();
      assertEquals(1, uploadResult.getFailedEvents().size());

      String listString =
          mockMvc
              .perform(get("/api/imported-events"))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      List<FailedImportedEventDto> importedEvents =
          objectMapper
              .readValue(
                  listString,
                  new TypeReference<ApiResponse<List<FailedImportedEventDto>>>() {})
              .getData();
      assertEquals(1, importedEvents.size());

      FailedImportedEventDto failed = importedEvents.get(0);
      FailedImportedEventUpdateForm patchForm =
          FailedImportedEventUpdateForm.builder()
              .apartmentId(testSetupHelper.getTestApartments().get(0).getId())
              .build();
      String patchString =
          mockMvc
              .perform(
                  patch("/api/imported-events/" + failed.getId())
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(patchForm)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      FailedImportedEventDto patched =
          objectMapper
              .readValue(
                  patchString, new TypeReference<ApiResponse<FailedImportedEventDto>>() {})
              .getData();
      assertEquals(testSetupHelper.getTestApartments().get(0).getId(), patched.getApartmentId());

      String retryString =
          mockMvc
              .perform(post("/api/imported-events/retry"))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      ImportResultDto retryResult =
          objectMapper
              .readValue(
                  retryString, new TypeReference<ApiResponse<ImportResultDto>>() {})
              .getData();
      assertEquals(1, retryResult.getSuccessCount());
      assertEquals(0, retryResult.getFailedEvents().size());

      mockMvc.perform(get("/api/imported-events")).andExpect(status().isOk()).andReturn();
    }

    @Test
    void When_DismissSingleAndAll_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      mockMvc
          .perform(
              multipart("/api/imported-events/upload")
                  .file(fileFor("src/test/resources/import_airbnb.csv"))
                  .param("source", "AIRBNB"))
          .andExpect(status().isOk())
          .andReturn();

      String listString =
          mockMvc
              .perform(get("/api/imported-events"))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      List<FailedImportedEventDto> importedEvents =
          objectMapper
              .readValue(
                  listString,
                  new TypeReference<ApiResponse<List<FailedImportedEventDto>>>() {})
              .getData();
      assertEquals(1, importedEvents.size());

      UUID id = importedEvents.get(0).getId();
      mockMvc.perform(delete("/api/imported-events/" + id)).andExpect(status().isOk());

      mockMvc
          .perform(
              multipart("/api/imported-events/upload")
                  .file(fileFor("src/test/resources/import_airbnb.csv"))
                  .param("source", "AIRBNB"))
          .andExpect(status().isOk())
          .andReturn();

      mockMvc.perform(delete("/api/imported-events")).andExpect(status().isOk());
    }
  }
}