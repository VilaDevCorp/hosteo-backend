package com.viladevcorp.hosteo.event;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.viladevcorp.hosteo.model.Apartment;
import com.viladevcorp.hosteo.model.types.EventType;
import com.viladevcorp.hosteo.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viladevcorp.hosteo.common.BaseControllerTest;
import com.viladevcorp.hosteo.common.TestSetupHelper;
import com.viladevcorp.hosteo.common.TestUtils;
import com.viladevcorp.hosteo.model.Event;
import com.viladevcorp.hosteo.model.dto.EventWithAssignmentsDto;
import com.viladevcorp.hosteo.model.dto.EventDto;
import com.viladevcorp.hosteo.model.Page;
import com.viladevcorp.hosteo.model.forms.EventCreateForm;
import com.viladevcorp.hosteo.model.forms.EventSearchForm;
import com.viladevcorp.hosteo.model.forms.EventUpdateForm;
import com.viladevcorp.hosteo.model.types.EventState;
import com.viladevcorp.hosteo.repository.ApartmentRepository;
import com.viladevcorp.hosteo.repository.EventRepository;
import com.viladevcorp.hosteo.repository.UserRepository;
import com.viladevcorp.hosteo.utils.ApiResponse;
import com.viladevcorp.hosteo.utils.CodeErrors;

import jakarta.persistence.EntityNotFoundException;

import static com.viladevcorp.hosteo.common.TestConstants.*;

class EventControllerTest extends BaseControllerTest {

  @Autowired private UserRepository userRepository;

  @Autowired private ApartmentRepository apartmentRepository;

  @Autowired private EventRepository eventRepository;

  @Autowired private TaskRepository taskRepository;

  @Autowired private MockMvc mockMvc;

  @Autowired TestSetupHelper testSetupHelper;

  @Autowired private ObjectMapper objectMapper;

  @BeforeEach
  void initialize() throws Exception {
    testSetupHelper.resetTestEvents();
  }

  @Nested
  @DisplayName("Create events")
  class CreateEvents {

    @Test
    void When_CreateEvent_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      Instant startDate = TestUtils.dateStrToInstant(NEW_EVENT_START_DATE);
      Instant endDate = TestUtils.dateStrToInstant(NEW_EVENT_END_DATE);
      EventCreateForm form = new EventCreateForm();
      form.setApartmentId(testSetupHelper.getTestApartments().get(0).getId());
      form.setName(NEW_EVENT_NAME);
      form.setStartDate(startDate);
      form.setEndDate(endDate);
      form.setType(NEW_EVENT_TYPE_1);

      String resultString =
          mockMvc
              .perform(
                  post("/api/event")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(form)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<EventDto>> typeReference = new TypeReference<>() {};
      ApiResponse<EventDto> result = objectMapper.readValue(resultString, typeReference);
      EventDto returnedEvent = result.getData();

      assertNotNull(returnedEvent);
      assertEquals(NEW_EVENT_NAME, returnedEvent.getName());
      assertEquals(NEW_EVENT_STATE, returnedEvent.getState());
      assertEquals(NEW_EVENT_SOURCE, returnedEvent.getSource());
      assertEquals(0, returnedEvent.getStartDate().compareTo(startDate));
      assertEquals(0, returnedEvent.getEndDate().compareTo(endDate));
    }

    @Test
    void When_CreateEventMissingName_BadRequest() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      Instant startDate = TestUtils.dateStrToInstant(NEW_EVENT_START_DATE);
      Instant endDate = TestUtils.dateStrToInstant(NEW_EVENT_END_DATE);

      EventCreateForm form = new EventCreateForm();
      form.setApartmentId(testSetupHelper.getTestApartments().get(0).getId());
      form.setStartDate(startDate);
      form.setEndDate(endDate);

      mockMvc
          .perform(
              post("/api/event")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void When_CreateEventNotOwned_NotFound() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_2, userRepository);
      Instant startDate = TestUtils.dateStrToInstant(NEW_EVENT_START_DATE);
      Instant endDate = TestUtils.dateStrToInstant(NEW_EVENT_END_DATE);

      EventCreateForm form = new EventCreateForm();
      form.setApartmentId(testSetupHelper.getTestApartments().get(0).getId());
      form.setName(NEW_EVENT_NAME);
      form.setStartDate(startDate);
      form.setEndDate(endDate);
      form.setType(NEW_EVENT_TYPE_1);

      mockMvc
          .perform(
              post("/api/event")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form)))
          .andExpect(status().isNotFound());
    }

    @Test
    void When_CreateEventApartmentNotFound_NotFound() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      Instant startDate = TestUtils.dateStrToInstant(NEW_EVENT_START_DATE);
      Instant endDate = TestUtils.dateStrToInstant(NEW_EVENT_END_DATE);

      EventCreateForm form = new EventCreateForm();
      form.setApartmentId(UUID.randomUUID());
      form.setName(NEW_EVENT_NAME);
      form.setStartDate(startDate);
      form.setEndDate(endDate);
      form.setType(NEW_EVENT_TYPE_1);

      mockMvc
          .perform(
              post("/api/event")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form)))
          .andExpect(status().isNotFound());
    }

    @Test
    void When_CreateEventApartmentNotAvailable_Conflict() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      // Dates overlapping with an existing event
      Instant startDate = TestUtils.dateStrToInstant(CREATED_EVENT_START_DATE_1);
      Instant endDate = TestUtils.dateStrToInstant(CREATED_EVENT_END_DATE_1);

      EventCreateForm form = new EventCreateForm();
      form.setApartmentId(testSetupHelper.getTestApartments().get(0).getId());
      form.setName(NEW_EVENT_NAME);
      form.setStartDate(startDate);
      form.setEndDate(endDate);
      form.setType(NEW_EVENT_TYPE_1);

      mockMvc
          .perform(
              post("/api/event")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form)))
          .andExpect(status().isConflict());

      startDate = startDate.plusSeconds(24 * 60 * 60);
      endDate = endDate.plusSeconds(24 * 60 * 60);

      form.setStartDate(startDate);
      form.setEndDate(endDate);

      String resultString =
          mockMvc
              .perform(
                  post("/api/event")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(form)))
              .andExpect(status().isConflict())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<EventDto>> typeReference = new TypeReference<>() {};
      ApiResponse<EventDto> result = objectMapper.readValue(resultString, typeReference);

      assertEquals(CodeErrors.NOT_AVAILABLE_DATES, result.getErrorCode());
    }
  }

  @Nested
  @DisplayName("Update events")
  class UpdateEvents {

    @Test
    void When_UpdateEvent_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      Instant startDate = TestUtils.dateStrToInstant(UPDATED_EVENT_START_DATE);
      Instant endDate = TestUtils.dateStrToInstant(UPDATED_EVENT_END_DATE);
      EventUpdateForm form = new EventUpdateForm();
      form.setId(testSetupHelper.getTestEvents().get(UPDATED_EVENT_APARTMENT_POSITION).getId());
      form.setName(UPDATED_EVENT_NAME);
      form.setStartDate(startDate);
      form.setEndDate(endDate);
      form.setState(UPDATED_EVENT_STATE);
      form.setSource(UPDATED_EVENT_SOURCE);

      mockMvc
          .perform(
              put("/api/event")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form)))
          .andExpect(status().isOk());

      Event returnedEvent =
          eventRepository
              .findById(
                  testSetupHelper.getTestEvents().get(UPDATED_EVENT_APARTMENT_POSITION).getId())
              .orElse(null);

      assertNotNull(returnedEvent);
      assertEquals(UPDATED_EVENT_NAME, returnedEvent.getName());
      assertEquals(UPDATED_EVENT_STATE, returnedEvent.getState());
      assertEquals(UPDATED_EVENT_SOURCE, returnedEvent.getSource());
      assertEquals(0, returnedEvent.getStartDate().compareTo(startDate));
      assertEquals(0, returnedEvent.getEndDate().compareTo(endDate));
    }

    @Test
    void When_UpdateEventNotOwned_NotFound() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_2, userRepository);
      Instant startDate = TestUtils.dateStrToInstant(CREATED_EVENT_START_DATE_1);
      Instant endDate = TestUtils.dateStrToInstant(CREATED_EVENT_END_DATE_1);

      EventUpdateForm form = new EventUpdateForm();
      form.setId(testSetupHelper.getTestEvents().get(UPDATED_EVENT_APARTMENT_POSITION).getId());
      form.setName(UPDATED_EVENT_NAME);
      form.setStartDate(startDate);
      form.setEndDate(endDate);

      form.setState(UPDATED_EVENT_STATE);
      form.setSource(UPDATED_EVENT_SOURCE);

      mockMvc
          .perform(
              put("/api/event")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form)))
          .andExpect(status().isNotFound());
    }

    @Test
    void When_UpdateEventNotAvailable_Conflict() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      Instant startDate = TestUtils.dateStrToInstant(CREATED_EVENT_START_DATE_1);
      Instant endDate = TestUtils.dateStrToInstant(CREATED_EVENT_END_DATE_1);

      EventUpdateForm form = new EventUpdateForm();
      form.setId(testSetupHelper.getTestEvents().get(2).getId());
      form.setName(UPDATED_EVENT_NAME);
      form.setStartDate(startDate);
      form.setEndDate(endDate);

      form.setState(UPDATED_EVENT_STATE);
      form.setSource(UPDATED_EVENT_SOURCE);

      String resultString =
          mockMvc
              .perform(
                  put("/api/event")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(form)))
              .andExpect(status().isConflict())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<EventDto>> typeReference = new TypeReference<>() {};
      ApiResponse<EventDto> result = objectMapper.readValue(resultString, typeReference);
      assertEquals(CodeErrors.NOT_AVAILABLE_DATES, result.getErrorCode());
    }
  }

  @Nested
  @DisplayName("Get event")
  class GetEvent {

    @Test
    void When_GetEvent_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      String resultString =
          mockMvc
              .perform(
                  get("/api/event/" + testSetupHelper.getTestEvents().get(0).getId().toString()))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<EventWithAssignmentsDto>> typeReference = new TypeReference<>() {};
      ApiResponse<EventWithAssignmentsDto> result =
          objectMapper.readValue(resultString, typeReference);
      EventWithAssignmentsDto returnedEvent = result.getData();

      assertNotNull(returnedEvent);
      assertEquals(CREATED_EVENT_NAME_1, returnedEvent.getName());
    }

    @Test
    void When_GetEventNotOwned_NotFound() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_2, userRepository);
      mockMvc
          .perform(get("/api/event/" + testSetupHelper.getTestEvents().get(0).getId().toString()))
          .andExpect(status().isNotFound());
    }

    @Test
    void When_GetEventNotExist_NotFound() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      mockMvc.perform(get("/api/event/" + UUID.randomUUID())).andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("Search events")
  class SearchEvents {

    @Test
    void When_SearchAllEvents_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      EventSearchForm searchFormObj = new EventSearchForm();
      searchFormObj.setPageSize(0);
      String resultString =
          mockMvc
              .perform(
                  post("/api/event/search")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(searchFormObj)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();
      ApiResponse<Page<EventDto>> result = null;
      TypeReference<ApiResponse<Page<EventDto>>> typeReference = new TypeReference<>() {};

      try {
        result = objectMapper.readValue(resultString, typeReference);
      } catch (Exception e) {
        fail("Error parsing response");
      }
      Page<EventDto> returnedPage = result.getData();
      List<EventDto> events = returnedPage.getContent();
      assertEquals(5, events.size());
    }

    @Test
    void When_SearchAllEventsWithPagination_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      EventSearchForm searchFormObj = new EventSearchForm();
      searchFormObj.setPageNumber(0);
      searchFormObj.setPageSize(2);
      String resultString =
          mockMvc
              .perform(
                  post("/api/event/search")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(searchFormObj)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();
      ApiResponse<Page<EventDto>> result = null;
      TypeReference<ApiResponse<Page<EventDto>>> typeReference = new TypeReference<>() {};

      try {
        result = objectMapper.readValue(resultString, typeReference);
      } catch (Exception e) {
        fail("Error parsing response");
      }
      Page<EventDto> returnedPage = result.getData();
      List<EventDto> events = returnedPage.getContent();
      assertEquals(2, events.size());
      assertEquals(3, returnedPage.getTotalPages());
      assertEquals(5, returnedPage.getTotalRows());
    }

    @Test
    void When_SearchNoEvents_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_2, userRepository);

      EventSearchForm searchFormObj = new EventSearchForm();
      searchFormObj.setPageNumber(-1);
      String resultString =
          mockMvc
              .perform(
                  post("/api/event/search")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(searchFormObj)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();
      ApiResponse<Page<EventDto>> result = null;
      TypeReference<ApiResponse<Page<EventDto>>> typeReference = new TypeReference<>() {};

      try {
        result = objectMapper.readValue(resultString, typeReference);
      } catch (Exception e) {
        fail("Error parsing response");
      }
      Page<EventDto> returnedPage = result.getData();
      List<EventDto> events = returnedPage.getContent();
      assertEquals(0, events.size());
    }

    @Test
    void When_SearchEventsByState_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      // Search for READY apartments
      EventSearchForm searchFormObj = new EventSearchForm();
      searchFormObj.setStates(Set.of(EventState.PENDING.toString()));
      searchFormObj.setPageSize(0);
      String resultString =
          mockMvc
              .perform(
                  post("/api/event/search")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(searchFormObj)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();
      ApiResponse<Page<EventDto>> result = null;
      TypeReference<ApiResponse<Page<EventDto>>> typeReference = new TypeReference<>() {};

      try {
        result = objectMapper.readValue(resultString, typeReference);
      } catch (Exception e) {
        fail("Error parsing response");
      }
      Page<EventDto> returnedPage = result.getData();
      List<EventDto> events = returnedPage.getContent();
      assertEquals(3, events.size());
      for (EventDto event : events) {
        assertEquals(EventState.PENDING, event.getState());
      }
    }

    @Test
    void When_SearchEventsByApartment_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      // Search for apartments with name containing "loft"
      EventSearchForm searchFormObj = new EventSearchForm();
      searchFormObj.setApartmentName("loft");
      searchFormObj.setPageSize(0);
      String resultString =
          mockMvc
              .perform(
                  post("/api/event/search")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(searchFormObj)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();
      ApiResponse<Page<EventDto>> result = null;
      TypeReference<ApiResponse<Page<EventDto>>> typeReference = new TypeReference<>() {};

      try {
        result = objectMapper.readValue(resultString, typeReference);
      } catch (Exception e) {
        fail("Error parsing response");
      }
      Page<EventDto> returnedPage = result.getData();
      List<EventDto> events = returnedPage.getContent();
      assertEquals(2, events.size());
    }

    @Test
    void When_SearchEventsByDateRange_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      // Search for events within a date range
      EventSearchForm searchFormObj = new EventSearchForm();
      Instant startDate = Instant.parse("2025-11-20T00:00:00Z");
      Instant endDate = Instant.parse("2025-12-02T00:00:00Z");
      searchFormObj.setStartDate(startDate);
      searchFormObj.setEndDate(endDate);
      searchFormObj.setPageSize(0);
      String resultString =
          mockMvc
              .perform(
                  post("/api/event/search")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(searchFormObj)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();
      ApiResponse<Page<EventDto>> result = null;
      TypeReference<ApiResponse<Page<EventDto>>> typeReference = new TypeReference<>() {};

      try {
        result = objectMapper.readValue(resultString, typeReference);
      } catch (Exception e) {
        fail("Error parsing response");
      }
      Page<EventDto> returnedPage = result.getData();
      List<EventDto> events = returnedPage.getContent();
      assertEquals(2, events.size());
      for (EventDto event : events) {
        assertTrue(
            !event.getStartDate().isBefore(startDate) && !event.getStartDate().isAfter(endDate));
      }
    }
  }

  @Nested
  @DisplayName("Delete events")
  class DeleteEvents {
    private UUID forDeletionEventId;

    @BeforeEach
    void setup() {
      // Create a event to be deleted
      Instant startDate = Instant.now().plusSeconds(5 * 24 * 60 * 60);
      Instant endDate = Instant.now().plusSeconds(10 * 24 * 60 * 60);

      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      Event event =
          Event.builder()
              .apartment(
                  apartmentRepository
                      .findById(testSetupHelper.getTestApartments().get(0).getId())
                      .orElseThrow())
              .name("Event To Be Deleted")
              .startDate(startDate)
              .endDate(endDate)
              .state(EventState.PENDING)
              .type(EventType.BOOKING)
              .build();
      event = eventRepository.save(event);
      forDeletionEventId = event.getId();
    }

    @Test
    void When_DeleteEvent_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      mockMvc
          .perform(
              org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(
                  "/api/event/" + forDeletionEventId.toString()))
          .andExpect(status().isOk());
      boolean exists = eventRepository.existsById(forDeletionEventId);
      assertFalse(exists, "Event was not deleted");
    }

    @Test
    void When_DeleteEventNotOwned_NotFound() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_2, userRepository);
      mockMvc
          .perform(
              org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(
                  "/api/event/" + forDeletionEventId.toString()))
          .andExpect(status().isNotFound());
    }

    @Test
    void When_DeleteEventNotExist_NotFound() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      mockMvc
          .perform(
              org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(
                  "/api/event/" + UUID.randomUUID()))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("Workflow events")
  class WorkflowEvents {

    @BeforeEach
    void setup() throws Exception {
      testSetupHelper.resetAssignments();
    }

    @Test
    void When_ChangeEventStateToInProgress_ApartmentIsOccupied() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      String resultString =
          mockMvc
              .perform(
                  patch(
                          "/api/event/"
                              + testSetupHelper.getTestEvents().get(2).getId()
                              + "/state/"
                              + EventState.IN_PROGRESS)
                      .contentType("application/json"))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<EventDto>> typeReference = new TypeReference<>() {};
      ApiResponse<EventDto> result = objectMapper.readValue(resultString, typeReference);
      EventDto returnedEvent = result.getData();

      Apartment apartment =
          apartmentRepository
              .findById(returnedEvent.getApartment().getId())
              .orElseThrow(EntityNotFoundException::new);
      assertTrue(apartment.getState().isOccupied());
    }

    @Test
    void When_ChangeEventStateToCompleted_ApartmentIsUsed() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      String resultString =
          mockMvc
              .perform(
                  patch(
                          "/api/event/"
                              + testSetupHelper.getTestEvents().get(1).getId()
                              + "/state/"
                              + EventState.FINISHED)
                      .contentType("application/json"))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<EventDto>> typeReference = new TypeReference<>() {};
      ApiResponse<EventDto> result = objectMapper.readValue(resultString, typeReference);
      EventDto returnedEvent = result.getData();

      Apartment apartment =
          apartmentRepository
              .findById(returnedEvent.getApartment().getId())
              .orElseThrow(EntityNotFoundException::new);
      assertTrue(apartment.getState().isUsed());
    }

    @Test
    void When_UpdateEventToFinished_ApartmentIsUsed() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      Event eventToUpdate = testSetupHelper.getTestEvents().get(1);

      EventUpdateForm form = new EventUpdateForm();
      BeanUtils.copyProperties(eventToUpdate, form);
      form.setState(EventState.FINISHED);

      mockMvc
          .perform(
              put("/api/event")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form)))
          .andExpect(status().isOk());

      Apartment apartment =
          apartmentRepository
              .findById(eventToUpdate.getApartment().getId())
              .orElseThrow(EntityNotFoundException::new);
      assertTrue(apartment.getState().isUsed());
    }

    @Test
    void When_UpdateEventToFinishedOnlyState_ApartmentIsUsed() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      String resultString =
          mockMvc
              .perform(
                  patch(
                          "/api/event/"
                              + testSetupHelper.getTestEvents().get(2).getId()
                              + "/state/"
                              + EventState.FINISHED)
                      .contentType("application/json"))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<EventDto>> typeReference = new TypeReference<>() {};
      ApiResponse<EventDto> result = objectMapper.readValue(resultString, typeReference);
      EventDto returnedEvent = result.getData();

      Apartment apartment =
          apartmentRepository
              .findById(returnedEvent.getApartment().getId())
              .orElseThrow(EntityNotFoundException::new);
      assertTrue(apartment.getState().isUsed());
    }

    @Test
    void When_CreateInProgressEvent_ApartmentIsOccupied() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      Instant startDate =
          TestUtils.dateStrToInstant(CREATED_EVENT_START_DATE_3).minusSeconds(24 * 60 * 60);
      Instant endDate =
          TestUtils.dateStrToInstant(CREATED_EVENT_START_DATE_3).minusSeconds(12 * 60 * 60);
      EventCreateForm form = new EventCreateForm();
      form.setApartmentId(testSetupHelper.getTestApartments().get(0).getId());
      form.setName(NEW_EVENT_NAME);
      form.setType(NEW_EVENT_TYPE_1);
      form.setStartDate(startDate);
      form.setEndDate(endDate);
      form.setState(EventState.IN_PROGRESS);

      String resultString =
          mockMvc
              .perform(
                  post("/api/event")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(form)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<EventDto>> typeReference = new TypeReference<>() {};
      ApiResponse<EventDto> result = objectMapper.readValue(resultString, typeReference);
      EventDto returnedEvent = result.getData();

      Apartment apartment =
          apartmentRepository
              .findById(returnedEvent.getApartment().getId())
              .orElseThrow(EntityNotFoundException::new);
      assertTrue(apartment.getState().isOccupied());
    }

    @Test
    void When_CreatePendingEventBeforeFinished_Conflict() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      Instant startDate =
          TestUtils.dateStrToInstant(CREATED_EVENT_START_DATE_1).minusSeconds(24 * 60 * 60);
      Instant endDate = startDate.plusSeconds(5 * 60 * 60);
      EventCreateForm form = new EventCreateForm();
      form.setApartmentId(
          testSetupHelper.getTestApartments().get(CREATED_EVENT_APARTMENT_POSITION_1).getId());
      form.setName(NEW_EVENT_NAME);
      form.setStartDate(startDate);
      form.setEndDate(endDate);
      form.setType(EventType.BOOKING);
      form.setState(EventState.PENDING);
      String resultString =
          mockMvc
              .perform(
                  post("/api/event")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(form)))
              .andExpect(status().isConflict())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<EventDto>> typeReference = new TypeReference<>() {};
      ApiResponse<EventDto> result = objectMapper.readValue(resultString, typeReference);
      assertEquals(
          CodeErrors.NEXT_OF_PENDING_CANNOT_BE_INPROGRESS_OR_FINISHED, result.getErrorCode());
    }

    @Test
    void When_CreatePendingEventBeforeInProgress_Conflict() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      Instant startDate =
          TestUtils.dateStrToInstant(CREATED_EVENT_START_DATE_2).minusSeconds(24 * 60 * 60);
      Instant endDate = startDate.plusSeconds(5 * 60 * 60);
      EventCreateForm form = new EventCreateForm();
      form.setApartmentId(
          testSetupHelper.getTestApartments().get(CREATED_EVENT_APARTMENT_POSITION_2).getId());
      form.setName(NEW_EVENT_NAME);
      form.setType(EventType.BOOKING);
      form.setStartDate(startDate);
      form.setEndDate(endDate);
      form.setState(EventState.PENDING);
      String resultString =
          mockMvc
              .perform(
                  post("/api/event")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(form)))
              .andExpect(status().isConflict())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<EventDto>> typeReference = new TypeReference<>() {};
      ApiResponse<EventDto> result = objectMapper.readValue(resultString, typeReference);
      assertEquals(
          CodeErrors.NEXT_OF_PENDING_CANNOT_BE_INPROGRESS_OR_FINISHED, result.getErrorCode());
    }

    @Test
    void When_CreateInProgressWithPreviousPending_Conflict() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      // Create IN_PROGRESS event after the PENDING event (event 3)
      Instant startDate =
          TestUtils.dateStrToInstant(CREATED_EVENT_END_DATE_3).plusSeconds(2 * 24 * 60 * 60);
      Instant endDate = startDate.plusSeconds(24 * 60 * 60);
      EventCreateForm form = new EventCreateForm();
      form.setApartmentId(
          testSetupHelper.getTestApartments().get(CREATED_EVENT_APARTMENT_POSITION_3).getId());
      form.setName(NEW_EVENT_NAME);
      form.setType(EventType.BOOKING);
      form.setStartDate(startDate);
      form.setEndDate(endDate);
      form.setState(EventState.IN_PROGRESS);

      String resultString =
          mockMvc
              .perform(
                  post("/api/event")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(form)))
              .andExpect(status().isConflict())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<EventDto>> typeReference = new TypeReference<>() {};
      ApiResponse<EventDto> result = objectMapper.readValue(resultString, typeReference);
      assertEquals(
          CodeErrors.PREV_OF_INPROGRESS_CANNOT_BE_PENDING_OR_INPROGRESS, result.getErrorCode());
    }

    @Test
    void When_CreateInProgressWithPreviousInProgress_Conflict() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      // Create IN_PROGRESS event after the IN_PROGRESS event (event 2)
      Instant startDate =
          TestUtils.dateStrToInstant(CREATED_EVENT_START_DATE_2).plusSeconds(10 * 24 * 60 * 60);
      Instant endDate = startDate.plusSeconds(3 * 24 * 60 * 60);
      EventCreateForm form = new EventCreateForm();
      form.setApartmentId(
          testSetupHelper.getTestApartments().get(CREATED_EVENT_APARTMENT_POSITION_2).getId());
      form.setName("Test InProgress After InProgress");
      form.setType(EventType.BOOKING);
      form.setStartDate(startDate);
      form.setEndDate(endDate);
      form.setState(EventState.IN_PROGRESS);

      String resultString =
          mockMvc
              .perform(
                  post("/api/event")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(form)))
              .andExpect(status().isConflict())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<EventDto>> typeReference = new TypeReference<>() {};
      ApiResponse<EventDto> result = objectMapper.readValue(resultString, typeReference);
      assertEquals(
          CodeErrors.PREV_OF_INPROGRESS_CANNOT_BE_PENDING_OR_INPROGRESS, result.getErrorCode());
    }

    @Test
    void When_CreateInProgressWithNextInProgress_Conflict() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      // Create IN_PROGRESS event before the IN_PROGRESS event (event 2)
      Instant startDate =
          TestUtils.dateStrToInstant(CREATED_EVENT_START_DATE_2).minusSeconds(10 * 24 * 60 * 60);
      Instant endDate = startDate.plusSeconds(3 * 24 * 60 * 60);
      EventCreateForm form = new EventCreateForm();
      form.setApartmentId(
          testSetupHelper.getTestApartments().get(CREATED_EVENT_APARTMENT_POSITION_2).getId());
      form.setName("Test InProgress Before InProgress");
      form.setStartDate(startDate);
      form.setEndDate(endDate);
      form.setState(EventState.IN_PROGRESS);
      form.setType(EventType.BOOKING);

      String resultString =
          mockMvc
              .perform(
                  post("/api/event")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(form)))
              .andExpect(status().isConflict())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<EventDto>> typeReference = new TypeReference<>() {};
      ApiResponse<EventDto> result = objectMapper.readValue(resultString, typeReference);
      assertEquals(
          CodeErrors.NEXT_OF_INPROGRESS_CANNOT_BE_FINISHED_OR_INPROGRESS, result.getErrorCode());
    }

    @Test
    void When_CreateInProgressWithNextFinished_Conflict() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      // Create IN_PROGRESS event before the FINISHED event (event 1)
      Instant startDate =
          TestUtils.dateStrToInstant(CREATED_EVENT_START_DATE_1).minusSeconds(10 * 24 * 60 * 60);
      Instant endDate = startDate.plusSeconds(3 * 24 * 60 * 60);
      EventCreateForm form = new EventCreateForm();
      form.setApartmentId(
          testSetupHelper.getTestApartments().get(CREATED_EVENT_APARTMENT_POSITION_1).getId());
      form.setName("Test InProgress Before Finished");
      form.setType(EventType.BOOKING);
      form.setStartDate(startDate);
      form.setEndDate(endDate);
      form.setState(EventState.IN_PROGRESS);

      String resultString =
          mockMvc
              .perform(
                  post("/api/event")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(form)))
              .andExpect(status().isConflict())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<EventDto>> typeReference = new TypeReference<>() {};
      ApiResponse<EventDto> result = objectMapper.readValue(resultString, typeReference);
      assertEquals(
          CodeErrors.NEXT_OF_INPROGRESS_CANNOT_BE_FINISHED_OR_INPROGRESS, result.getErrorCode());
    }

    @Test
    void When_CreateFinishedWithPreviousPending_Conflict() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      // Create FINISHED event after the PENDING event (event 3)
      Instant startDate =
          TestUtils.dateStrToInstant(CREATED_EVENT_END_DATE_3).plusSeconds(2 * 24 * 60 * 60);
      Instant endDate = startDate.plusSeconds(24 * 60 * 60);
      EventCreateForm form = new EventCreateForm();
      form.setApartmentId(
          testSetupHelper.getTestApartments().get(CREATED_EVENT_APARTMENT_POSITION_3).getId());
      form.setName("Test Finished After Pending");
      form.setType(EventType.BOOKING);
      form.setStartDate(startDate);
      form.setEndDate(endDate);
      form.setState(EventState.FINISHED);

      String resultString =
          mockMvc
              .perform(
                  post("/api/event")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(form)))
              .andExpect(status().isConflict())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<EventDto>> typeReference = new TypeReference<>() {};
      ApiResponse<EventDto> result = objectMapper.readValue(resultString, typeReference);
      assertEquals(
          CodeErrors.PREV_OF_FINISHED_CANNOT_BE_PENDING_OR_INPROGRESS, result.getErrorCode());
    }

    @Test
    void When_CreateFinishedWithPreviousInProgress_Conflict() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      // Create FINISHED event after the IN_PROGRESS event (event 2)
      Instant startDate =
          TestUtils.dateStrToInstant(CREATED_EVENT_START_DATE_2).plusSeconds(10 * 24 * 60 * 60);
      Instant endDate = startDate.plusSeconds(3 * 24 * 60 * 60);
      EventCreateForm form = new EventCreateForm();
      form.setApartmentId(
          testSetupHelper.getTestApartments().get(CREATED_EVENT_APARTMENT_POSITION_2).getId());
      form.setName("Test Finished After InProgress");
      form.setType(EventType.BOOKING);
      form.setStartDate(startDate);
      form.setEndDate(endDate);
      form.setState(EventState.FINISHED);

      String resultString =
          mockMvc
              .perform(
                  post("/api/event")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(form)))
              .andExpect(status().isConflict())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<EventDto>> typeReference = new TypeReference<>() {};
      ApiResponse<EventDto> result = objectMapper.readValue(resultString, typeReference);
      assertEquals(
          CodeErrors.PREV_OF_FINISHED_CANNOT_BE_PENDING_OR_INPROGRESS, result.getErrorCode());
    }

    @Test
    void When_UpdateEventToInProgressWithPreviousInProgress_Conflict() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      Event eventToUpdate = testSetupHelper.getTestEvents().get(3); // PENDING event

      EventUpdateForm form = new EventUpdateForm();
      BeanUtils.copyProperties(eventToUpdate, form);
      form.setState(EventState.IN_PROGRESS);

      String resultString =
          mockMvc
              .perform(
                  put("/api/event")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(form)))
              .andExpect(status().isConflict())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<EventDto>> typeReference = new TypeReference<>() {};
      ApiResponse<EventDto> result = objectMapper.readValue(resultString, typeReference);
      assertEquals(
          CodeErrors.PREV_OF_INPROGRESS_CANNOT_BE_PENDING_OR_INPROGRESS, result.getErrorCode());
    }

    @Test
    void When_DeleteInProgressEvent_ApartmentBecomesReady() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      Event eventToDelete = testSetupHelper.getTestEvents().get(1); // IN_PROGRESS event

      mockMvc
          .perform(
              org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(
                  "/api/event/" + eventToDelete.getId().toString()))
          .andExpect(status().isOk());
      Apartment apartment =
          apartmentRepository
              .findById(eventToDelete.getApartment().getId())
              .orElseThrow(EntityNotFoundException::new);
      assertTrue(apartment.getState().isReady());
    }
  }
}
