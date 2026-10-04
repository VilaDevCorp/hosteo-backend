package com.viladevcorp.hosteo.workflow;

import static com.viladevcorp.hosteo.common.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viladevcorp.hosteo.common.BaseControllerTest;
import com.viladevcorp.hosteo.common.TestUtils;
import com.viladevcorp.hosteo.model.*;
import com.viladevcorp.hosteo.model.dto.AlertInfo;
import com.viladevcorp.hosteo.model.dto.AlertItem;
import com.viladevcorp.hosteo.model.dto.EventSchedulerDto;
import com.viladevcorp.hosteo.model.types.*;
import com.viladevcorp.hosteo.repository.*;
import com.viladevcorp.hosteo.utils.ApiResponse;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;

class WorkflowControllerTest extends BaseControllerTest {

  @TestConfiguration
  static class FixedClockTestConfig {

    @Bean
    @Primary
    Clock testClock() {
      return Clock.fixed(Instant.parse(NOW), ZoneOffset.UTC);
    }
  }

  @BeforeEach
  void setup() throws Exception {
    testSetupHelper.resetAssignments();
  }

  @Autowired private UserRepository userRepository;

  @Autowired private EventRepository eventRepository;

  @Autowired private TaskRepository taskRepository;

  @Autowired private ApartmentRepository apartmentRepository;

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  private static final String START_OF_WEEK = "01-12-2025";
  private static final String NOW = "2025-12-04T10:00:00Z";

  @Nested
  @DisplayName("Get scheduler info")
  class Scheduler {

    @Test
    void When_GetSchedulerInfo_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      Instant now = Instant.parse(NOW);

      Apartment apartment =
          apartmentRepository.save(
              Apartment.builder().name("Test Alert Apartment").state(ApartmentState.USED).build());

      taskRepository.save(
          Task.builder()
              .name("Test Mandatory Task")
              .type(TaskType.MANDATORY)
              .category(CategoryEnum.CLEANING)
              .duration(60)
              .apartment(apartment)
              .steps(new ArrayList<>())
              .build());

      eventRepository.save(
          Event.builder()
              .name("Predecessor Event")
              .apartment(apartment)
              .startDate(now.minusSeconds(10 * 24 * 3600))
              .endDate(now.minusSeconds(8 * 24 * 3600))
              .state(EventState.FINISHED)
              .type(EventType.BOOKING)
              .build());

      // Events inside the scheduler week (01-12-2025 → 08-12-2025)
      Event eventIn1Days =
          eventRepository.save(
              Event.builder()
                  .name("Test Event 1 Day")
                  .apartment(apartment)
                  .startDate(now.plusSeconds(24 * 3600))
                  .endDate(now.plusSeconds(2 * 24 * 3600))
                  .state(EventState.PENDING)
                  .type(EventType.BOOKING)
                  .build());

      Event eventIn3Days =
          eventRepository.save(
              Event.builder()
                  .name("Test Event 3 Days")
                  .apartment(apartment)
                  .startDate(now.plusSeconds(3 * 24 * 3600))
                  .endDate(now.plusSeconds(4 * 24 * 3600))
                  .state(EventState.PENDING)
                  .type(EventType.BOOKING)
                  .build());

      String resultString =
          mockMvc
              .perform(get("/api/scheduler/" + START_OF_WEEK))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<SchedulerInfo>> typeReference = new TypeReference<>() {};
      ApiResponse<SchedulerInfo> result = objectMapper.readValue(resultString, typeReference);

      SchedulerInfo info = result.getData();
      assertNotNull(info);
      // The scheduler renders calendar events within the requested week.
      Set<EventSchedulerDto> events = info.getEvents();
      assertTrue(events.contains(eventIn1Days));
      assertTrue(events.contains(eventIn3Days));
    }
  }

  @Nested
  @DisplayName("Get alerts")
  class Alerts {

    @Test
    void When_GetAlerts_ReturnRedFirstThenYellowByStartDateAsc() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      Instant now = Instant.parse(NOW);

      Apartment apartment =
          apartmentRepository.save(
              Apartment.builder().name("Test Alert Apartment").state(ApartmentState.USED).build());

      taskRepository.save(
          Task.builder()
              .name("Test Mandatory Task")
              .type(TaskType.MANDATORY)
              .category(CategoryEnum.CLEANING)
              .duration(60)
              .apartment(apartment)
              .steps(new ArrayList<>())
              .build());

      // Previous FINISHED event that dirtied the apartment — the predecessor of the 1-day event.
      Event finishedPredecessor =
          eventRepository.save(
              Event.builder()
                  .name("Predecessor Event")
                  .apartment(apartment)
                  .startDate(now.minusSeconds(10 * 24 * 3600))
                  .endDate(now.minusSeconds(8 * 24 * 3600))
                  .state(EventState.FINISHED)
                  .type(EventType.BOOKING)
                  .build());

      String eventIn1DaysName = "Test Event 1 Day";
      String eventIn3DaysName = "Test Event 3 Days";

      // PENDING event starting in 1 day — RED alert, predecessor is the finished event.
      Event eventIn1Days =
          eventRepository.save(
              Event.builder()
                  .name(eventIn1DaysName)
                  .apartment(apartment)
                  .startDate(now.plusSeconds(24 * 3600))
                  .endDate(now.plusSeconds(2 * 24 * 3600))
                  .state(EventState.PENDING)
                  .type(EventType.BOOKING)
                  .build());

      // PENDING event starting in 3 days — YELLOW alert, predecessor is the 1-day event.
      Event eventIn3Days =
          eventRepository.save(
              Event.builder()
                  .name(eventIn3DaysName)
                  .apartment(apartment)
                  .startDate(now.plusSeconds(3 * 24 * 3600))
                  .endDate(now.plusSeconds(4 * 24 * 3600))
                  .state(EventState.PENDING)
                  .type(EventType.BOOKING)
                  .build());

      String resultString =
          mockMvc
              .perform(get("/api/alerts"))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<AlertInfo>> typeReference = new TypeReference<>() {};
      ApiResponse<AlertInfo> result = objectMapper.readValue(resultString, typeReference);

      AlertInfo alertInfo = result.getData();
      assertEquals(1, alertInfo.getNRedAlerts());
      assertEquals(1, alertInfo.getNYellowAlerts());

      List<AlertItem> alerts = alertInfo.getAlerts();
      assertEquals(2, alerts.size());

      AlertItem red = alerts.get(0);
      AlertItem yellow = alerts.get(1);

      assertEquals(Alert.DAYS_LEFT_2_UNASSIGNED, red.getAlertType());
      assertEquals(eventIn1Days.getId(), red.getEvent().getId());
      assertEquals(eventIn1DaysName, red.getEvent().getName());
      assertEquals(finishedPredecessor.getId(), red.getPrevEvent().getId());

      assertEquals(Alert.DAYS_LEFT_5_UNASSIGNED, yellow.getAlertType());
      assertEquals(eventIn3Days.getId(), yellow.getEvent().getId());
      assertEquals(eventIn3DaysName, yellow.getEvent().getName());
      assertEquals(eventIn1Days.getId(), yellow.getPrevEvent().getId());
    }
  }
}
