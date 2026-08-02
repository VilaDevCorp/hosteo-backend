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
import com.viladevcorp.hosteo.model.Event;
import com.viladevcorp.hosteo.model.ImpBooking;
import com.viladevcorp.hosteo.model.dto.ImpBookingDto;
import com.viladevcorp.hosteo.model.dto.ImportResultDto;
import com.viladevcorp.hosteo.model.types.EventState;
import com.viladevcorp.hosteo.model.types.ConflictType;
import com.viladevcorp.hosteo.model.types.EventType;
import com.viladevcorp.hosteo.repository.EventRepository;
import com.viladevcorp.hosteo.repository.ImpBookingRepository;
import com.viladevcorp.hosteo.repository.UserRepository;
import com.viladevcorp.hosteo.utils.ApiResponse;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import com.viladevcorp.hosteo.utils.AuthUtils;
import com.viladevcorp.hosteo.utils.CodeErrors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

class ImportBookingControllerTest extends BaseControllerTest {

  @Autowired private UserRepository userRepository;

  @Autowired private EventRepository eventRepository;

  @Autowired private ImpBookingRepository impBookingRepository;

  @Autowired private MockMvc mockMvc;

  @Autowired TestSetupHelper testSetupHelper;

  @Autowired private ObjectMapper objectMapper;

  @BeforeEach
  void setup() throws Exception {
    testSetupHelper.resetImportApartments();
  }

  @Nested
  @DisplayName("Import airbnb bookings")
  class ImportAirbnbBookings {

    @Test
    void When_ImportAirbnbBooking_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      File importFile = new File("src/test/resources/import_airbnb.csv");
      MockMultipartFile mockFile =
          new MockMultipartFile(
              "file", "import_airbnb.csv", "text/csv", new FileInputStream(importFile));
      String resultString =
          mockMvc
              .perform(multipart("/api/booking/import/airbnb").file(mockFile))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<List<ImpBookingDto>>> typeReference = new TypeReference<>() {};
      ApiResponse<List<ImpBookingDto>> result = objectMapper.readValue(resultString, typeReference);
      List<ImpBookingDto> importedBookings = result.getData();
      assertEquals(EVENT_COUNT_AIRBNB_AFTER_IMPORT, importedBookings.size());
      assertEquals(
          EVENT_COUNT_AIRBNB_APARTMENT_1,
          importedBookings.stream()
              .filter(
                  booking ->
                      booking
                          .getApartment()
                          .getAirbnbId()
                          .equals(CREATED_IMPORT_APARTMENT_AIRBNB_ID_1))
              .count());
      assertEquals(
          EVENT_COUNT_AIRBNB_APARTMENT_2,
          importedBookings.stream()
              .filter(
                  booking ->
                      booking
                          .getApartment()
                          .getAirbnbId()
                          .equals(CREATED_IMPORT_APARTMENT_AIRBNB_ID_2))
              .count());
      assertEquals(
          EVENT_COUNT_AIRBNB_APARTMENT_3,
          importedBookings.stream()
              .filter(
                  booking ->
                      booking
                          .getApartment()
                          .getAirbnbId()
                          .equals(CREATED_IMPORT_APARTMENT_AIRBNB_ID_3))
              .count());
      importedBookings.forEach(impBookingDto -> assertNull(impBookingDto.getConflict()));
    }

    @Test
    void When_ImportAirbnbBookingSomeConflicts_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      testSetupHelper.resetImportConflicts(true);

      File importFile = new File("src/test/resources/import_airbnb_with_conflicts.csv");
      MockMultipartFile mockFile =
          new MockMultipartFile(
              "file",
              "import_airbnb_with_conflicts.csv",
              "text/csv",
              new FileInputStream(importFile));
      String resultString =
          mockMvc
              .perform(multipart("/api/booking/import/airbnb").file(mockFile))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<List<ImpBookingDto>>> typeReference = new TypeReference<>() {};
      ApiResponse<List<ImpBookingDto>> result = objectMapper.readValue(resultString, typeReference);
      List<ImpBookingDto> importedBookings = result.getData();
      assertEquals(EVENT_COUNT_AIRBNB_AFTER_IMPORT, importedBookings.size());
      assertEquals(
          EVENT_COUNT_AIRBNB_APARTMENT_1,
          importedBookings.stream()
              .filter(
                  booking ->
                      booking
                          .getApartment()
                          .getAirbnbId()
                          .equals(CREATED_IMPORT_APARTMENT_AIRBNB_ID_1))
              .count());
      assertEquals(
          EVENT_COUNT_AIRBNB_APARTMENT_2,
          importedBookings.stream()
              .filter(
                  booking ->
                      booking
                          .getApartment()
                          .getAirbnbId()
                          .equals(CREATED_IMPORT_APARTMENT_AIRBNB_ID_2))
              .count());
      assertEquals(
          EVENT_COUNT_AIRBNB_APARTMENT_3,
          importedBookings.stream()
              .filter(
                  booking ->
                      booking
                          .getApartment()
                          .getAirbnbId()
                          .equals(CREATED_IMPORT_APARTMENT_AIRBNB_ID_3))
              .count());

      ImpBooking conflictedImportBooking =
          impBookingRepository
              .findByNameAndCreatedByUsername(
                  CONFLICTED_IMPORT_EVENT_AIRBNB_NAME, AuthUtils.getUsername())
              .get(0);
      importedBookings.forEach(
          impBookingDto -> {
            if (impBookingDto.getName().equals(CONFLICTED_EVENT_AIRBNB_NAME)) {
              assertNotNull(impBookingDto.getConflict());
              assertEquals(ConflictType.EVENT_CONFLICT, impBookingDto.getConflict().getType());
              assertEquals(
                  testSetupHelper.getConflictEvent().getId(),
                  impBookingDto.getConflict().getConflictEntity().getId());
            } else if (impBookingDto.getName().equals(CONFLICTED_EVENT_AIRBNB_NAME_2)) {
              assertNotNull(impBookingDto.getConflict());
              assertEquals(ConflictType.ASSIGNMENT_CONFLICT, impBookingDto.getConflict().getType());
              assertEquals(
                  testSetupHelper.getConflictAssignment().getId(),
                  impBookingDto.getConflict().getConflictEntity().getId());
            } else if (impBookingDto.getName().equals(CONFLICTED_EVENT_AIRBNB_NAME_3)) {
              assertNotNull(impBookingDto.getConflict());
              assertEquals(
                  ConflictType.IMPORT_BOOKING_CONFLICT, impBookingDto.getConflict().getType());
              assertEquals(
                  impBookingDto.getConflict().getConflictEntity().getId(),
                  conflictedImportBooking.getId());
            } else {
              assertNull(impBookingDto.getConflict());
            }
          });
    }

    @Test
    void When_ExecuteImport_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      testSetupHelper.resetImportConflicts(true);
      mockMvc.perform(get("/api/booking/import/exists")).andExpect(status().isNotFound());

      File importFile = new File("src/test/resources/import_airbnb_with_conflicts.csv");
      MockMultipartFile mockFile =
          new MockMultipartFile(
              "file",
              "import_airbnb_with_conflicts.csv",
              "text/csv",
              new FileInputStream(importFile));
      String resultString =
          mockMvc
              .perform(multipart("/api/booking/import/airbnb").file(mockFile))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<List<ImpBookingDto>>> typeReference = new TypeReference<>() {};
      ApiResponse<List<ImpBookingDto>> result = objectMapper.readValue(resultString, typeReference);
      mockMvc.perform(get("/api/booking/import/exists")).andExpect(status().isOk());

      String executeResultString =
          mockMvc
              .perform(post("/api/booking/import/execute"))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<ImportResultDto>> typeReference2 = new TypeReference<>() {};
      ApiResponse<ImportResultDto> executeResult =
          objectMapper.readValue(executeResultString, typeReference2);
      ImportResultDto importResults = executeResult.getData();
      assertEquals(EVENT_COUNT_AIRBNB_AFTER_IMPORT - 3, importResults.getSuccessCount());
      assertEquals(3, importResults.getFailureCount());
    }

    @Test
    void When_ExecuteImport_WithStateConflict() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      testSetupHelper.resetImportConflicts(true);

      File importFile = new File("src/test/resources/import_airbnb_with_conflicts.csv");
      MockMultipartFile mockFile =
          new MockMultipartFile(
              "file",
              "import_airbnb_with_conflicts.csv",
              "text/csv",
              new FileInputStream(importFile));
      String resultString =
          mockMvc
              .perform(multipart("/api/booking/import/airbnb").file(mockFile))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<List<ImpBookingDto>>> typeReference = new TypeReference<>() {};
      ApiResponse<List<ImpBookingDto>> result = objectMapper.readValue(resultString, typeReference);

      Event conflictBooking2 =
          Event.builder()
              .name(CREATED_EVENT_CONFLICT_NAME_2)
              .type(EventType.BOOKING)
              .startDate(TestUtils.dateStrToInstant(CREATED_EVENT_AIRBNB_CONFLICT_START_DATE_2))
              .endDate(TestUtils.dateStrToInstant(CREATED_EVENT_AIRBNB_CONFLICT_END_DATE_2))
              .state(EventState.FINISHED)
              .apartment(testSetupHelper.getTestApartments().get(0))
              .build();

      eventRepository.save(conflictBooking2);

      String executeResultString =
          mockMvc
              .perform(post("/api/booking/import/execute"))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<ImportResultDto>> typeReference2 = new TypeReference<>() {};
      ApiResponse<ImportResultDto> executeResult =
          objectMapper.readValue(executeResultString, typeReference2);
      ImportResultDto importResults = executeResult.getData();
      assertEquals(EVENT_COUNT_AIRBNB_AFTER_IMPORT - 4, importResults.getSuccessCount());
      assertEquals(4, importResults.getFailureCount());

      ImpBooking conflictedImportBooking =
          impBookingRepository
              .findByNameAndCreatedByUsername(
                  CONFLICTED_EVENT_ON_CREATION_NAME, AuthUtils.getUsername())
              .get(0);
      assertEquals(
          CodeErrors.NEXT_OF_PENDING_CANNOT_BE_INPROGRESS_OR_FINISHED,
          conflictedImportBooking.getCreationError());
    }
  }

  @Nested
  @DisplayName("Import Booking bookings")
  class ImportBookingBookings {

    @Test
    void When_ImportBookingBooking_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      testSetupHelper.resetImportConflicts(true);

      File importFile = new File("src/test/resources/import_booking.csv");
      MockMultipartFile mockFile =
          new MockMultipartFile(
              "file", "import_booking.csv", "text/csv", new FileInputStream(importFile));
      String resultString =
          mockMvc
              .perform(multipart("/api/booking/import/booking").file(mockFile))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<List<ImpBookingDto>>> typeReference = new TypeReference<>() {};
      ApiResponse<List<ImpBookingDto>> result = objectMapper.readValue(resultString, typeReference);
      List<ImpBookingDto> importedBookings = result.getData();
      assertEquals(EVENT_COUNT_BOOKING_AFTER_IMPORT, importedBookings.size());
      assertEquals(
          EVENT_COUNT_BOOKING_APARTMENT_1,
          importedBookings.stream()
              .filter(
                  booking ->
                      booking
                          .getApartment()
                          .getBookingId()
                          .equals(CREATED_IMPORT_APARTMENT_BOOKING_ID_1))
              .count());
      assertEquals(
          EVENT_COUNT_BOOKING_APARTMENT_2,
          importedBookings.stream()
              .filter(
                  booking ->
                      booking
                          .getApartment()
                          .getBookingId()
                          .equals(CREATED_IMPORT_APARTMENT_BOOKING_ID_2))
              .count());
      assertEquals(
          EVENT_COUNT_BOOKING_APARTMENT_3,
          importedBookings.stream()
              .filter(
                  booking ->
                      booking
                          .getApartment()
                          .getBookingId()
                          .equals(CREATED_IMPORT_APARTMENT_BOOKING_ID_3))
              .count());
      importedBookings.forEach(impBookingDto -> assertNull(impBookingDto.getConflict()));
    }

    @Test
    void When_ImportBookingBookingSomeConflicts_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      testSetupHelper.resetImportConflicts(false);

      File importFile = new File("src/test/resources/import_booking_with_conflicts.csv");
      MockMultipartFile mockFile =
          new MockMultipartFile(
              "file",
              "import_booking_with_conflicts.csv",
              "text/csv",
              new FileInputStream(importFile));
      String resultString =
          mockMvc
              .perform(multipart("/api/booking/import/booking").file(mockFile))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<List<ImpBookingDto>>> typeReference = new TypeReference<>() {};
      ApiResponse<List<ImpBookingDto>> result = objectMapper.readValue(resultString, typeReference);
      List<ImpBookingDto> importedBookings = result.getData();
      assertEquals(EVENT_COUNT_BOOKING_AFTER_IMPORT, importedBookings.size());
      assertEquals(
          EVENT_COUNT_BOOKING_APARTMENT_1,
          importedBookings.stream()
              .filter(
                  booking ->
                      booking
                          .getApartment()
                          .getBookingId()
                          .equals(CREATED_IMPORT_APARTMENT_BOOKING_ID_1))
              .count());
      assertEquals(
          EVENT_COUNT_BOOKING_APARTMENT_2,
          importedBookings.stream()
              .filter(
                  booking ->
                      booking
                          .getApartment()
                          .getBookingId()
                          .equals(CREATED_IMPORT_APARTMENT_BOOKING_ID_2))
              .count());
      assertEquals(
          EVENT_COUNT_BOOKING_APARTMENT_3,
          importedBookings.stream()
              .filter(
                  booking ->
                      booking
                          .getApartment()
                          .getBookingId()
                          .equals(CREATED_IMPORT_APARTMENT_BOOKING_ID_3))
              .count());
      ImpBooking conflictedImportBooking =
          impBookingRepository
              .findByNameAndCreatedByUsername(
                  CONFLICTED_IMPORT_EVENT_BOOKING_NAME, AuthUtils.getUsername())
              .get(0);

      importedBookings.forEach(
          impBookingDto -> {
            if (impBookingDto.getName().equals(CONFLICTED_EVENT_BOOKING_NAME)) {
              assertNotNull(impBookingDto.getConflict());
              assertEquals(ConflictType.EVENT_CONFLICT, impBookingDto.getConflict().getType());
              assertEquals(
                  testSetupHelper.getConflictEvent().getId(),
                  impBookingDto.getConflict().getConflictEntity().getId());
            } else if (impBookingDto.getName().equals(CONFLICTED_EVENT_BOOKING_NAME_2)) {
              assertNotNull(impBookingDto.getConflict());
              assertEquals(ConflictType.ASSIGNMENT_CONFLICT, impBookingDto.getConflict().getType());
              assertEquals(
                  testSetupHelper.getConflictAssignment().getId(),
                  impBookingDto.getConflict().getConflictEntity().getId());
            } else if (impBookingDto.getName().equals(CONFLICTED_EVENT_BOOKING_NAME_3)) {
              assertNotNull(impBookingDto.getConflict());
              assertEquals(
                  ConflictType.IMPORT_BOOKING_CONFLICT, impBookingDto.getConflict().getType());
              assertEquals(
                  impBookingDto.getConflict().getConflictEntity().getId(),
                  conflictedImportBooking.getId());
            } else {
              assertNull(impBookingDto.getConflict());
            }
          });
    }
  }
}
