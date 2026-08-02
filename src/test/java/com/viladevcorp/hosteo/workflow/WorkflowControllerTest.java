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
 import com.viladevcorp.hosteo.model.dto.EventSchedulerDto;
 import com.viladevcorp.hosteo.model.types.*;
 import com.viladevcorp.hosteo.repository.*;
 import com.viladevcorp.hosteo.utils.ApiResponse;

 import java.time.Clock;
 import java.time.Instant;
 import java.time.ZoneOffset;
 import java.util.ArrayList;
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

       // Create a fresh apartment whose state is not READY so the alert skip logic
       // (which exempts READY apartments with a next pending event) does not apply.
       Apartment apartment =
           apartmentRepository.save(
               Apartment.builder()
                   .name("Test Alert Apartment")
                   .state(ApartmentState.USED)
                   .build());

       // Create a mandatory task on this apartment — when the predecessor event has no
       // assignment for it, the alert engine sees it as "unassigned".
       taskRepository.save(
           Task.builder()
               .name("Test Mandatory Task")
               .type(TaskType.MANDATORY)
               .category(CategoryEnum.CLEANING)
               .duration(60)
               .apartment(apartment)
               .steps(new ArrayList<>())
               .build());

       // Predecessor event (FINISHED, in the past). It has zero assignments, so the
       // apartment's mandatory task remains unassigned from the predecessor's perspective.
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

       // PENDING event starting in 1 day — should trigger a RED alert
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

       // PENDING event starting in 3 days — should trigger a YELLOW alert
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
               .perform(get("/api/scheduler/" + START_OF_WEEK))
               .andExpect(status().isOk())
               .andReturn()
               .getResponse()
               .getContentAsString();

       TypeReference<ApiResponse<SchedulerInfo>> typeReference = new TypeReference<>() {};
       ApiResponse<SchedulerInfo> result = objectMapper.readValue(resultString, typeReference);

       SchedulerInfo info = result.getData();
       assertEquals(1, info.getRedAlertBookings().size());
       assertEquals(1, info.getYellowAlertBookings().size());

       // Verify red alert: the 1-day event should have DAYS_LEFT_2_UNASSIGNED
       UUID redAlertEventId = info.getRedAlertBookings().get(0);
       EventSchedulerDto redAlertEvent = info.getEventInfo().get(redAlertEventId);
       assertEquals(eventIn1Days.getId(), redAlertEvent.getId());
       assertEquals(eventIn1DaysName, redAlertEvent.getName());
       assertEquals(Alert.DAYS_LEFT_2_UNASSIGNED, redAlertEvent.getAlert());

       // Verify yellow alert: the 3-day event should have DAYS_LEFT_5_UNASSIGNED
       UUID yellowAlertEventId = info.getYellowAlertBookings().get(0);
       EventSchedulerDto yellowAlertEvent = info.getEventInfo().get(yellowAlertEventId);
       assertEquals(eventIn3Days.getId(), yellowAlertEvent.getId());
       assertEquals(eventIn3DaysName, yellowAlertEvent.getName());
       assertEquals(Alert.DAYS_LEFT_5_UNASSIGNED, yellowAlertEvent.getAlert());
     }
   }
 }
