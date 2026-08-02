package com.viladevcorp.hosteo.assignment;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.viladevcorp.hosteo.model.*;
import com.viladevcorp.hosteo.model.dto.TaskDto;
import com.viladevcorp.hosteo.model.forms.*;
import com.viladevcorp.hosteo.model.types.ApartmentState;
import com.viladevcorp.hosteo.model.types.EventState;
import com.viladevcorp.hosteo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viladevcorp.hosteo.common.BaseControllerTest;
import com.viladevcorp.hosteo.common.TestUtils;
import com.viladevcorp.hosteo.model.dto.AssignmentDto;
import com.viladevcorp.hosteo.model.types.AssignmentState;
import com.viladevcorp.hosteo.utils.ApiResponse;
import com.viladevcorp.hosteo.utils.CodeErrors;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Pageable;

import static com.viladevcorp.hosteo.common.TestConstants.*;

class AssignmentControllerTest extends BaseControllerTest {

  @Autowired private UserRepository userRepository;

  @Autowired private AssignmentRepository assignmentRepository;

  @Autowired private EventRepository eventRepository;

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private ApartmentRepository apartmentRepository;

  @Autowired private TaskRepository taskRepository;

  @BeforeEach
  void setup() throws Exception {
    testSetupHelper.resetAssignments();
  }

  @Nested
  @DisplayName("Create assignments")
  class CreateAssignments {

    @Test
    void When_CreateAssignment_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      Task assignmentTask = testSetupHelper.getTestTasks().get(NEW_ASSIGNMENT_TASK_POSITION);
      AssignmentCreateForm form = new AssignmentCreateForm();
      form.setTaskId(assignmentTask.getId());
      form.setEventId(testSetupHelper.getTestEvents().get(NEW_ASSIGNMENT_EVENT_POSITION).getId());
      form.setStartDate(TestUtils.dateStrToInstant(NEW_ASSIGNMENT_START_DATE));
      form.setEndDate(
          TestUtils.dateStrToInstant(NEW_ASSIGNMENT_START_DATE)
              .plusSeconds(assignmentTask.getDuration() * 60L));
      form.setWorkerId(
          testSetupHelper.getTestWorkers().get(NEW_ASSIGNMENT_WORKER_POSITION).getId());
      form.setState(NEW_ASSIGNMENT_STATE);

      String resultString =
          mockMvc
              .perform(
                  post("/api/assignment")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(form)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<AssignmentDto>> typeReference = new TypeReference<>() {};
      ApiResponse<AssignmentDto> result = objectMapper.readValue(resultString, typeReference);
      Assignment createdAssignment =
          assignmentRepository.findById(result.getData().getId()).orElse(null);
      assertNotNull(createdAssignment);
      assertEquals(
          testSetupHelper.getTestTasks().get(NEW_ASSIGNMENT_TASK_POSITION).getId(),
          createdAssignment.getTask().getId());
      assertEquals(
          testSetupHelper.getTestWorkers().get(NEW_ASSIGNMENT_WORKER_POSITION).getId(),
          createdAssignment.getWorker().getId());
      assertEquals(
          0,
          TestUtils.dateStrToInstant(NEW_ASSIGNMENT_START_DATE)
              .compareTo(createdAssignment.getStartDate()));
      assertEquals(NEW_ASSIGNMENT_STATE, createdAssignment.getState());
    }

    @Test
    void When_CreateAssignment_MissingTaskId_BadRequest() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      AssignmentCreateForm form = new AssignmentCreateForm();
      form.setStartDate(TestUtils.dateStrToInstant(NEW_ASSIGNMENT_START_DATE));
      form.setWorkerId(
          testSetupHelper.getTestWorkers().get(NEW_ASSIGNMENT_WORKER_POSITION).getId());

      form.setState(NEW_ASSIGNMENT_STATE);

      mockMvc
          .perform(
              post("/api/assignment")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void When_CreateAssignment_MissingStartDate_BadRequest() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      AssignmentCreateForm form = new AssignmentCreateForm();
      form.setTaskId(testSetupHelper.getTestTasks().get(NEW_ASSIGNMENT_TASK_POSITION).getId());
      form.setWorkerId(
          testSetupHelper.getTestWorkers().get(NEW_ASSIGNMENT_WORKER_POSITION).getId());

      form.setState(NEW_ASSIGNMENT_STATE);

      mockMvc
          .perform(
              post("/api/assignment")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void When_CreateAssignment_MissingWorkerId_BadRequest() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      AssignmentCreateForm form = new AssignmentCreateForm();
      form.setTaskId(testSetupHelper.getTestTasks().get(NEW_ASSIGNMENT_TASK_POSITION).getId());

      form.setStartDate(TestUtils.dateStrToInstant(NEW_ASSIGNMENT_START_DATE));
      form.setState(NEW_ASSIGNMENT_STATE);

      mockMvc
          .perform(
              post("/api/assignment")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void When_CreateAssignment_MissingState_BadRequest() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      AssignmentCreateForm form = new AssignmentCreateForm();
      form.setTaskId(testSetupHelper.getTestTasks().get(NEW_ASSIGNMENT_TASK_POSITION).getId());
      form.setStartDate(TestUtils.dateStrToInstant(NEW_ASSIGNMENT_START_DATE));
      form.setWorkerId(
          testSetupHelper.getTestWorkers().get(NEW_ASSIGNMENT_WORKER_POSITION).getId());

      mockMvc
          .perform(
              post("/api/assignment")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void When_CreateAssignment_MissingEventId_BadRequest() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      AssignmentCreateForm form = new AssignmentCreateForm();
      form.setTaskId(testSetupHelper.getTestTasks().get(NEW_ASSIGNMENT_TASK_POSITION).getId());
      form.setStartDate(TestUtils.dateStrToInstant(NEW_ASSIGNMENT_START_DATE));
      form.setWorkerId(
          testSetupHelper.getTestWorkers().get(NEW_ASSIGNMENT_WORKER_POSITION).getId());
      form.setState(NEW_ASSIGNMENT_STATE);

      mockMvc
          .perform(
              post("/api/assignment")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void When_CreateAssignment_NonExistentTask_NotFound() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      Task assignmentTask = testSetupHelper.getTestTasks().get(NEW_ASSIGNMENT_TASK_POSITION);
      AssignmentCreateForm form = new AssignmentCreateForm();
      form.setTaskId(UUID.randomUUID());
      form.setEventId(testSetupHelper.getTestEvents().get(NEW_ASSIGNMENT_EVENT_POSITION).getId());
      form.setStartDate(TestUtils.dateStrToInstant(NEW_ASSIGNMENT_START_DATE));
      form.setEndDate(
          TestUtils.dateStrToInstant(NEW_ASSIGNMENT_START_DATE)
              .plusSeconds(assignmentTask.getDuration() * 60L));
      form.setWorkerId(
          testSetupHelper.getTestWorkers().get(NEW_ASSIGNMENT_WORKER_POSITION).getId());
      form.setState(NEW_ASSIGNMENT_STATE);

      mockMvc
          .perform(
              post("/api/assignment")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form)))
          .andExpect(status().isNotFound());
    }

    @Test
    void When_CreateAssignment_NonExistentWorker_NotFound() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      Task assignmentTask = testSetupHelper.getTestTasks().get(NEW_ASSIGNMENT_TASK_POSITION);
      AssignmentCreateForm form = new AssignmentCreateForm();
      form.setTaskId(assignmentTask.getId());
      Instant instantToConflictWorker = TestUtils.dateStrToInstant(CREATED_ASSIGNMENT_START_DATE_5);
      form.setEventId(testSetupHelper.getTestEvents().get(NEW_ASSIGNMENT_EVENT_POSITION).getId());
      form.setStartDate(instantToConflictWorker);
      form.setEndDate(instantToConflictWorker.plusSeconds(assignmentTask.getDuration() * 60L));
      form.setWorkerId(UUID.randomUUID());
      form.setState(NEW_ASSIGNMENT_STATE);

      mockMvc
          .perform(
              post("/api/assignment")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form)))
          .andExpect(status().isNotFound());
    }

    @Test
    void When_CreateAssignment_AnotherUser_NotFound() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_2, userRepository);

      Task assignmentTask = testSetupHelper.getTestTasks().get(NEW_ASSIGNMENT_TASK_POSITION);
      AssignmentCreateForm form = new AssignmentCreateForm();
      form.setTaskId(assignmentTask.getId());
      form.setEventId(testSetupHelper.getTestEvents().get(NEW_ASSIGNMENT_EVENT_POSITION).getId());
      form.setStartDate(TestUtils.dateStrToInstant(NEW_ASSIGNMENT_START_DATE));
      form.setEndDate(
          TestUtils.dateStrToInstant(NEW_ASSIGNMENT_START_DATE)
              .plusSeconds(assignmentTask.getDuration() * 60L));
      form.setWorkerId(
          testSetupHelper.getTestWorkers().get(NEW_ASSIGNMENT_WORKER_POSITION).getId());
      form.setState(NEW_ASSIGNMENT_STATE);

      mockMvc
          .perform(
              post("/api/assignment")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form)))
          .andExpect(status().isNotFound());
    }

    @Test
    void When_CreateAssignment_DuplicateTask_Conflict() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      Instant startDate1 = TestUtils.dateStrToInstant(NEW_ASSIGNMENT_START_DATE);
      Instant endDate1 =
          startDate1.plusSeconds(
              testSetupHelper.getTestTasks().get(NEW_ASSIGNMENT_TASK_POSITION).getDuration() * 60L);
      Task assignmentTask = testSetupHelper.getTestTasks().get(NEW_ASSIGNMENT_TASK_POSITION);
      Worker assignmentWorker =
          testSetupHelper.getTestWorkers().get(NEW_ASSIGNMENT_WORKER_POSITION);
      Event assignmentEvent = testSetupHelper.getTestEvents().get(NEW_ASSIGNMENT_EVENT_POSITION);
      assignmentRepository.save(
          Assignment.builder()
              .task(assignmentTask)
              .event(assignmentEvent)
              .startDate(startDate1)
              .endDate(endDate1)
              .state(NEW_ASSIGNMENT_STATE)
              .worker(assignmentWorker)
              .build());

      AssignmentCreateForm form = new AssignmentCreateForm();
      form.setTaskId(assignmentTask.getId());
      form.setEventId(assignmentEvent.getId());
      form.setStartDate(startDate1.plusSeconds(24 * 60 * 60L));
      form.setEndDate(form.getStartDate().plusSeconds(assignmentTask.getDuration() * 60L));
      form.setWorkerId(assignmentWorker.getId());
      form.setState(NEW_ASSIGNMENT_STATE);

      String resultString =
          mockMvc
              .perform(
                  post("/api/assignment")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(form)))
              .andExpect(status().isConflict())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<AssignmentDto>> typeReference = new TypeReference<>() {};
      ApiResponse<AssignmentDto> result = objectMapper.readValue(resultString, typeReference);
      assertEquals(CodeErrors.DUPLICATED_EVENT_FOR_TASK, result.getErrorCode());
    }

    @Test
    void When_CreateAssignment_StartDateConflictsWithExistingAssignment_Conflict()
        throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      Task assignmentTask1 = testSetupHelper.getTestTasks().get(0);
      Task assignmentTask2 = testSetupHelper.getTestTasks().get(1);
      Worker assignmentWorker1 = testSetupHelper.getTestWorkers().get(0);
      Worker assignmentWorker2 = testSetupHelper.getTestWorkers().get(1);
      Event assignmentEvent = testSetupHelper.getTestEvents().get(NEW_ASSIGNMENT_EVENT_POSITION);

      Instant startDate = TestUtils.dateStrToInstant(NEW_ASSIGNMENT_START_DATE);
      Instant endDate = startDate.plusSeconds(assignmentTask1.getDuration() * 60L);

      assignmentRepository.save(
          Assignment.builder()
              .task(assignmentTask1)
              .event(assignmentEvent)
              .startDate(startDate)
              .endDate(endDate)
              .worker(assignmentWorker1)
              .state(NEW_ASSIGNMENT_STATE)
              .build());

      AssignmentCreateForm form = new AssignmentCreateForm();
      form.setTaskId(assignmentTask2.getId());
      form.setEventId(assignmentEvent.getId());
      form.setStartDate(startDate);
      form.setEndDate(endDate);
      form.setWorkerId(assignmentWorker2.getId());
      form.setState(NEW_ASSIGNMENT_STATE);

      String resultString =
          mockMvc
              .perform(
                  post("/api/assignment")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(form)))
              .andExpect(status().isConflict())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<AssignmentDto>> typeReference = new TypeReference<>() {};
      ApiResponse<AssignmentDto> result = objectMapper.readValue(resultString, typeReference);
      assertEquals(CodeErrors.NOT_AVAILABLE_DATES, result.getErrorCode());
    }

    @Test
    void When_CreateAssignment_WorkerNotAvailable_Conflict() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      Task assignmentTask = testSetupHelper.getTestTasks().get(NEW_ASSIGNMENT_TASK_POSITION);
      AssignmentCreateForm form = new AssignmentCreateForm();
      form.setTaskId(assignmentTask.getId());
      form.setEventId(testSetupHelper.getTestEvents().get(NEW_ASSIGNMENT_EVENT_POSITION).getId());
      form.setStartDate(TestUtils.dateStrToInstant(CREATED_ASSIGNMENT_START_DATE_5));
      form.setEndDate(
          TestUtils.dateStrToInstant(CREATED_ASSIGNMENT_START_DATE_5)
              .plusSeconds(assignmentTask.getDuration() * 60L));
      form.setWorkerId(
          testSetupHelper.getTestWorkers().get(CREATED_ASSIGNMENT_WORKER_POSITION_5).getId());
      form.setState(NEW_ASSIGNMENT_STATE);

      String resultString =
          mockMvc
              .perform(
                  post("/api/assignment")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(form)))
              .andExpect(status().isConflict())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<AssignmentDto>> typeReference = new TypeReference<>() {};
      ApiResponse<AssignmentDto> result = objectMapper.readValue(resultString, typeReference);
      assertEquals(CodeErrors.NOT_AVAILABLE_DATES, result.getErrorCode());
    }

    @Test
    void When_CreateAssignment_EventNotFinished_Conflict() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      Task assignmentTask = testSetupHelper.getTestTasks().get(NEW_ASSIGNMENT_TASK_POSITION);
      AssignmentCreateForm form = new AssignmentCreateForm();
      form.setTaskId(assignmentTask.getId());
      form.setEventId(testSetupHelper.getTestEvents().get(NEW_ASSIGNMENT_EVENT_POSITION).getId());
      form.setStartDate(TestUtils.dateStrToInstant(NEW_ASSIGNMENT_START_DATE));
      form.setEndDate(
          TestUtils.dateStrToInstant(NEW_ASSIGNMENT_START_DATE)
              .plusSeconds(assignmentTask.getDuration() * 60L));
      form.setWorkerId(
          testSetupHelper.getTestWorkers().get(NEW_ASSIGNMENT_WORKER_POSITION).getId());
      form.setState(AssignmentState.FINISHED);

      String resultString =
          mockMvc
              .perform(
                  post("/api/assignment")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(form)))
              .andExpect(status().isConflict())
              .andReturn()
              .getResponse()
              .getContentAsString();

      TypeReference<ApiResponse<AssignmentDto>> typeReference = new TypeReference<>() {};
      ApiResponse<AssignmentDto> result = objectMapper.readValue(resultString, typeReference);
      assertEquals(CodeErrors.COMPLETE_TASK_ON_NOT_FINISHED_EVENT, result.getErrorCode());
    }
  }

  @Nested
  @DisplayName("Update assignments")
  class UpdateAssignments {

    @Test
    void When_UpdateAssignment_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      Assignment assignmentToUpdate = testSetupHelper.getTestAssignments().get(0);
      AssignmentUpdateForm form = new AssignmentUpdateForm();
      form.setId(assignmentToUpdate.getId());
      Instant startDate = TestUtils.dateStrToInstant(UPDATED_ASSIGNMENT_START_DATE);
      Instant endDate = startDate.plusSeconds(assignmentToUpdate.getTask().getDuration() * 60L);
      form.setStartDate(startDate);
      form.setEndDate(endDate);
      form.setWorkerId(testSetupHelper.getTestWorkers().get(1).getId());
      form.setState(UPDATED_ASSIGNMENT_STATE);

      mockMvc
          .perform(
              put("/api/assignment")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form)))
          .andExpect(status().isOk());

      Assignment updated =
          assignmentRepository.findById(testSetupHelper.getTestAssignments().get(0).getId()).get();
      assertEquals(UPDATED_ASSIGNMENT_STATE, updated.getState());
      assertEquals(testSetupHelper.getTestWorkers().get(1).getId(), updated.getWorker().getId());
      assertEquals(
          0,
          TestUtils.dateStrToInstant(UPDATED_ASSIGNMENT_START_DATE)
              .compareTo(updated.getStartDate()));
    }

    @Test
    void When_UpdateAssignmentState_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      Assignment assignmentToUpdate = testSetupHelper.getTestAssignments().get(0);
      mockMvc
          .perform(
              patch(
                      "/api/assignment/"
                          + assignmentToUpdate.getId()
                          + "/state/"
                          + UPDATED_ASSIGNMENT_STATE)
                  .contentType("application/json"))
          .andExpect(status().isOk());

      Assignment updated =
          assignmentRepository.findById(testSetupHelper.getTestAssignments().get(0).getId()).get();
      assertEquals(UPDATED_ASSIGNMENT_STATE, updated.getState());
    }

    @Test
    void When_UpdateAssignmentStatePassEvents_Conflict() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      Event event3 = testSetupHelper.getTestEvents().get(2);
      event3.setState(EventState.IN_PROGRESS);
      eventRepository.save(event3);

      Assignment assignmentToUpdate = testSetupHelper.getTestAssignments().get(0);

      String result =
          mockMvc
              .perform(
                  patch(
                          "/api/assignment/"
                              + assignmentToUpdate.getId()
                              + "/state/"
                              + AssignmentState.PENDING)
                      .contentType("application/json"))
              .andExpect(status().isConflict())
              .andReturn()
              .getResponse()
              .getContentAsString();
      TypeReference<ApiResponse<AssignmentDto>> typeReference = new TypeReference<>() {};
      ApiResponse<AssignmentDto> apiResponse = objectMapper.readValue(result, typeReference);
      assertEquals(
          CodeErrors.ASSIGN_CHANGE_LAST_FINISHED_EVENT_ANOTHER_EVENT_IN_PROGRESS,
          apiResponse.getErrorCode());

      event3.setState(EventState.FINISHED);
      eventRepository.save(event3);

      result =
          mockMvc
              .perform(
                  patch(
                          "/api/assignment/"
                              + assignmentToUpdate.getId()
                              + "/state/"
                              + AssignmentState.PENDING)
                      .contentType("application/json"))
              .andExpect(status().isConflict())
              .andReturn()
              .getResponse()
              .getContentAsString();
      typeReference = new TypeReference<>() {};
      apiResponse = objectMapper.readValue(result, typeReference);
      assertEquals(CodeErrors.CHANGE_IN_ASSIGNMENTS_OF_PAST_EVENT, apiResponse.getErrorCode());
    }

    @Test
    void When_UpdateAssignment_NonExistentId_NotFound() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      AssignmentUpdateForm form = new AssignmentUpdateForm();
      form.setId(UUID.randomUUID());
      form.setStartDate(TestUtils.dateStrToInstant(UPDATED_ASSIGNMENT_START_DATE));
      form.setEndDate(
          TestUtils.dateStrToInstant(UPDATED_ASSIGNMENT_START_DATE).plusSeconds(60 * 30L));
      form.setWorkerId(testSetupHelper.getTestWorkers().get(1).getId());
      form.setState(UPDATED_ASSIGNMENT_STATE);

      mockMvc
          .perform(
              put("/api/assignment")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form)))
          .andExpect(status().isNotFound());
    }

    @Test
    void When_UpdateAssignment_AnotherUser_NotFound() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_2, userRepository);

      Assignment assignmentToUpdate = testSetupHelper.getTestAssignments().get(0);
      AssignmentUpdateForm form = new AssignmentUpdateForm();
      form.setId(assignmentToUpdate.getId());
      Instant startDate = TestUtils.dateStrToInstant(UPDATED_ASSIGNMENT_START_DATE);
      Instant endDate = startDate.plusSeconds(assignmentToUpdate.getTask().getDuration() * 60L);
      form.setStartDate(startDate);
      form.setEndDate(endDate);

      form.setWorkerId(testSetupHelper.getTestWorkers().get(1).getId());
      form.setState(UPDATED_ASSIGNMENT_STATE);

      mockMvc
          .perform(
              put("/api/assignment")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form)))
          .andExpect(status().isNotFound());
    }

    @Test
    void When_UpdateAssignment_MissingId_BadRequest() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      AssignmentUpdateForm form = new AssignmentUpdateForm();
      form.setStartDate(TestUtils.dateStrToInstant(UPDATED_ASSIGNMENT_START_DATE));
      form.setWorkerId(testSetupHelper.getTestWorkers().get(1).getId());
      form.setState(UPDATED_ASSIGNMENT_STATE);

      mockMvc
          .perform(
              put("/api/assignment")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void When_UpdateAssignment_MissingStartDate_BadRequest() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      AssignmentUpdateForm form = new AssignmentUpdateForm();
      form.setId(testSetupHelper.getTestAssignments().get(0).getId());
      form.setWorkerId(testSetupHelper.getTestWorkers().get(1).getId());
      form.setState(UPDATED_ASSIGNMENT_STATE);

      mockMvc
          .perform(
              put("/api/assignment")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void When_UpdateAssignment_MissingEndDate_BadRequest() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      AssignmentUpdateForm form = new AssignmentUpdateForm();
      form.setId(testSetupHelper.getTestAssignments().get(0).getId());
      form.setStartDate(TestUtils.dateStrToInstant(UPDATED_ASSIGNMENT_START_DATE));
      form.setWorkerId(testSetupHelper.getTestWorkers().get(1).getId());
      form.setState(UPDATED_ASSIGNMENT_STATE);

      mockMvc
          .perform(
              put("/api/assignment")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void When_UpdateAssignment_MissingWorkerId_BadRequest() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      AssignmentUpdateForm form = new AssignmentUpdateForm();
      form.setId(testSetupHelper.getTestAssignments().get(0).getId());
      form.setStartDate(TestUtils.dateStrToInstant(UPDATED_ASSIGNMENT_START_DATE));
      form.setState(UPDATED_ASSIGNMENT_STATE);

      mockMvc
          .perform(
              put("/api/assignment")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void When_UpdateAssignment_MissingState_BadRequest() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      AssignmentUpdateForm form = new AssignmentUpdateForm();
      form.setId(testSetupHelper.getTestAssignments().get(0).getId());
      form.setStartDate(TestUtils.dateStrToInstant(UPDATED_ASSIGNMENT_START_DATE));
      form.setWorkerId(testSetupHelper.getTestWorkers().get(1).getId());

      mockMvc
          .perform(
              put("/api/assignment")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void When_UpdateAssignment_NonExistentWorker_NotFound() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      Assignment assignmentToUpdate = testSetupHelper.getTestAssignments().get(0);

      AssignmentUpdateForm form = new AssignmentUpdateForm();
      form.setId(assignmentToUpdate.getId());
      form.setStartDate(TestUtils.dateStrToInstant(UPDATED_ASSIGNMENT_START_DATE));
      form.setEndDate(
          TestUtils.dateStrToInstant(UPDATED_ASSIGNMENT_START_DATE)
              .plusSeconds(assignmentToUpdate.getTask().getDuration() * 60L));
      form.setWorkerId(UUID.randomUUID());
      form.setState(UPDATED_ASSIGNMENT_STATE);

      mockMvc
          .perform(
              put("/api/assignment")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form)))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("Get assignment")
  class GetAssignment {

    @Test
    void When_GetAssignment_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      String result =
          mockMvc
              .perform(
                  get("/api/assignment/" + testSetupHelper.getTestAssignments().get(0).getId())
                      .contentType("application/json"))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();
      TypeReference<ApiResponse<AssignmentDto>> typeReference = new TypeReference<>() {};
      ApiResponse<AssignmentDto> apiResponse = objectMapper.readValue(result, typeReference);
      AssignmentDto fetchedAssignment = apiResponse.getData();
      assertNotNull(fetchedAssignment);

      assertEquals(CREATED_ASSIGNMENT_STATE_1, fetchedAssignment.getState());
    }

    @Test
    void When_GetAssignment_NonExistentId_NotFound() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      mockMvc
          .perform(get("/api/assignment/" + UUID.randomUUID()).contentType("application/json"))
          .andExpect(status().isNotFound());
    }

    @Test
    void When_GetAssignment_AnotherUser_NotFound() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_2, userRepository);
      mockMvc
          .perform(
              get("/api/assignment/" + testSetupHelper.getTestAssignments().get(0).getId())
                  .contentType("application/json"))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("Search assignments")
  class SearchAssignments {
    @Test
    void When_SearchAllAssignments_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      AssignmentSearchForm searchFormObj = new AssignmentSearchForm();
      searchFormObj.setPageSize(0);
      String resultString =
          mockMvc
              .perform(
                  post("/api/assignment/search")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(searchFormObj)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();
      ApiResponse<Page<AssignmentDto>> result = null;
      TypeReference<ApiResponse<Page<AssignmentDto>>> typeReference = new TypeReference<>() {};

      try {
        result = objectMapper.readValue(resultString, typeReference);
      } catch (Exception e) {
        fail("Error parsing response");
      }
      Page<AssignmentDto> returnedPage = result.getData();
      List<AssignmentDto> assignments = returnedPage.getContent();
      assertEquals(5, assignments.size());
    }

    @Test
    void When_SearchAllAssignmentsWithPagination_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      AssignmentSearchForm searchFormObj = new AssignmentSearchForm();
      searchFormObj.setPageNumber(0);
      searchFormObj.setPageSize(2);
      String resultString =
          mockMvc
              .perform(
                  post("/api/assignment/search")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(searchFormObj)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();
      ApiResponse<Page<AssignmentDto>> result = null;
      TypeReference<ApiResponse<Page<AssignmentDto>>> typeReference = new TypeReference<>() {};

      try {
        result = objectMapper.readValue(resultString, typeReference);
      } catch (Exception e) {
        fail("Error parsing response");
      }
      Page<AssignmentDto> returnedPage = result.getData();
      List<AssignmentDto> assignments = returnedPage.getContent();
      assertEquals(2, assignments.size());
      assertEquals(3, returnedPage.getTotalPages());
      assertEquals(5, returnedPage.getTotalRows());
    }

    @Test
    void When_SearchNoAssignments_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_2, userRepository);

      AssignmentSearchForm searchFormObj = new AssignmentSearchForm();
      searchFormObj.setPageNumber(-1);
      String resultString =
          mockMvc
              .perform(
                  post("/api/assignment/search")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(searchFormObj)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();
      ApiResponse<Page<AssignmentDto>> result = null;
      TypeReference<ApiResponse<Page<AssignmentDto>>> typeReference = new TypeReference<>() {};

      try {
        result = objectMapper.readValue(resultString, typeReference);
      } catch (Exception e) {
        fail("Error parsing response");
      }
      Page<AssignmentDto> returnedPage = result.getData();
      List<AssignmentDto> assignments = returnedPage.getContent();
      assertEquals(0, assignments.size());
    }

    @Test
    void When_SearchAssignmentsByName_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      // Search for assignments with name containing "maintenance"
      AssignmentSearchForm searchFormObj = new AssignmentSearchForm();
      searchFormObj.setTaskName("maintenance");
      searchFormObj.setPageNumber(-1);
      String resultString =
          mockMvc
              .perform(
                  post("/api/assignment/search")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(searchFormObj)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();
      ApiResponse<Page<AssignmentDto>> result = null;
      TypeReference<ApiResponse<Page<AssignmentDto>>> typeReference = new TypeReference<>() {};

      try {
        result = objectMapper.readValue(resultString, typeReference);
      } catch (Exception e) {
        fail("Error parsing response");
      }
      Page<AssignmentDto> returnedPage = result.getData();
      List<AssignmentDto> assignments = returnedPage.getContent();
      assertEquals(3, assignments.size());
      for (AssignmentDto assignment : assignments) {
        assertTrue(assignment.getTask().getName().toLowerCase().contains("maintenance"));
      }
    }
  }

  @Nested
  @DisplayName("Delete assignment")
  class DeleteAssignment {

    @Test
    void When_DeleteAssignment_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      mockMvc
          .perform(
              delete("/api/assignment/" + testSetupHelper.getTestAssignments().get(0).getId())
                  .contentType("application/json"))
          .andExpect(status().isOk());
      boolean exists =
          assignmentRepository.existsById(testSetupHelper.getTestAssignments().get(0).getId());
      assertFalse(exists);
    }

    @Test
    void When_DeleteAssignment_NonExistentId_NotFound() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      mockMvc
          .perform(delete("/api/assignment/" + UUID.randomUUID()).contentType("application/json"))
          .andExpect(status().isNotFound());
    }

    @Test
    void When_DeleteAssignment_AnotherUser_NotFound() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_2, userRepository);
      mockMvc
          .perform(
              delete("/api/assignment/" + testSetupHelper.getTestAssignments().get(0).getId())
                  .contentType("application/json"))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("Workflow tests")
  class WorkflowTests {
    @Test
    void When_CompleteAllAssignments_ApartmentReady_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      Assignment assignmentToComplete = testSetupHelper.getTestAssignments().get(2);

      Event event =
          eventRepository
              .findFirstEventBeforeDateWithState(
                  testSetupHelper.getTestUsers().get(0).getId(),
                  assignmentToComplete.getTask().getApartment().getId(),
                  assignmentToComplete.getStartDate(),
                  null)
              .orElseThrow(EntityNotFoundException::new);
      event.setState(EventState.FINISHED);

      eventRepository.save(event);
      Apartment relatedApartment = assignmentToComplete.getTask().getApartment();
      apartmentRepository.save(relatedApartment);
      mockMvc
          .perform(
              patch(
                      "/api/assignment/"
                          + assignmentToComplete.getId()
                          + "/state"
                          + "/"
                          + AssignmentState.FINISHED)
                  .contentType("application/json"))
          .andExpect(status().isOk());

      assignmentToComplete = testSetupHelper.getTestAssignments().get(3);

      mockMvc
          .perform(
              patch(
                      "/api/assignment/"
                          + assignmentToComplete.getId()
                          + "/state"
                          + "/"
                          + AssignmentState.FINISHED)
                  .contentType("application/json"))
          .andExpect(status().isOk());

      assignmentToComplete = testSetupHelper.getTestAssignments().get(4);

      mockMvc
          .perform(
              patch(
                      "/api/assignment/"
                          + assignmentToComplete.getId()
                          + "/state"
                          + "/"
                          + AssignmentState.FINISHED)
                  .contentType("application/json"))
          .andExpect(status().isOk());

      relatedApartment =
          apartmentRepository
              .findById(relatedApartment.getId())
              .orElseThrow(EntityNotFoundException::new);

      assertEquals(ApartmentState.READY, relatedApartment.getState());
    }

    @Test
    void WhenAddOrDeleteTask_ApartmentStateRecalculated() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      // We create a task on an apartment with all tasks finished (so ready, but creating one should
      // set the state to USED
      Apartment relatedApartment = testSetupHelper.getTestApartments().get(0);
      TaskCreateForm form = new TaskCreateForm();
      form.setName(NEW_TASK_NAME_1);
      form.setCategory(NEW_TASK_CATEGORY_1);
      form.setType(NEW_TASK_TYPE_1);
      form.setDuration(NEW_TASK_DURATION_1);
      form.setApartmentId(relatedApartment.getId());
      form.setSteps(NEW_TASK_STEPS_1);

      String resultString =
          mockMvc
              .perform(
                  post("/api/task")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(form)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();
      TypeReference<ApiResponse<TaskDto>> typeReference = new TypeReference<>() {};
      ApiResponse<TaskDto> result = objectMapper.readValue(resultString, typeReference);
      relatedApartment =
          apartmentRepository
              .findById(relatedApartment.getId())
              .orElseThrow(EntityNotFoundException::new);
      // We check that the apartment is USED
      assertEquals(ApartmentState.USED, relatedApartment.getState());

      // Now we delete the task, so the apartment will go back to READY
      mockMvc
          .perform(delete("/api/task/" + result.getData().getId()).contentType("application/json"))
          .andExpect(status().isOk());
      relatedApartment =
          apartmentRepository
              .findById(relatedApartment.getId())
              .orElseThrow(EntityNotFoundException::new);
      assertEquals(ApartmentState.READY, relatedApartment.getState());
    }

    @Test
    void When_UpdatedAssignmentState_ApartmentStateRecalculated() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      Assignment assignmentToUpdate = testSetupHelper.getTestAssignments().get(0);
      // We set the assignment back to PENDING, so the state of the apartment should go back to USED
      mockMvc
          .perform(
              patch(
                      "/api/assignment/"
                          + assignmentToUpdate.getId()
                          + "/state/"
                          + AssignmentState.PENDING)
                  .contentType("application/json"))
          .andExpect(status().isOk());
      Apartment apartment =
          apartmentRepository
              .findById(assignmentToUpdate.getTask().getApartment().getId())
              .orElseThrow(EntityNotFoundException::new);
      assertEquals(ApartmentState.USED, apartment.getState());

      // Now we update back the assignment state to FINISHED, so the apartment should go back to
      // READY
      mockMvc
          .perform(
              patch(
                      "/api/assignment/"
                          + assignmentToUpdate.getId()
                          + "/state/"
                          + AssignmentState.FINISHED)
                  .contentType("application/json"))
          .andExpect(status().isOk());
      apartment =
          apartmentRepository
              .findById(assignmentToUpdate.getTask().getApartment().getId())
              .orElseThrow(EntityNotFoundException::new);
      assertEquals(ApartmentState.READY, apartment.getState());
    }

    @Test
    void When_CreateDeleteAssignment_ApartmentStateRecalculated() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      // We delete one of the completed assignments so the apartment should change to USED
      Assignment assignment = testSetupHelper.getTestAssignments().get(0);
      mockMvc
          .perform(delete("/api/assignment/" + assignment.getId()).contentType("application/json"))
          .andExpect(status().isOk());
      Apartment apartment =
          apartmentRepository
              .findById(assignment.getTask().getApartment().getId())
              .orElseThrow(EntityNotFoundException::new);
      assertEquals(ApartmentState.USED, apartment.getState());

      // We create back the completed assignment for the event, so the apartment is going to go back
      // to READY
      AssignmentCreateForm form = new AssignmentCreateForm();
      form.setTaskId(
          testSetupHelper.getTestTasks().get(CREATED_ASSIGNMENT_TASK_POSITION_1).getId());
      form.setEventId(testSetupHelper.getTestEvents().get(0).getId());
      form.setStartDate(TestUtils.dateStrToInstant(CREATED_ASSIGNMENT_START_DATE_1));
      form.setEndDate(
          TestUtils.dateStrToInstant(CREATED_ASSIGNMENT_START_DATE_1)
              .plusSeconds(
                  testSetupHelper
                          .getTestTasks()
                          .get(CREATED_ASSIGNMENT_TASK_POSITION_1)
                          .getDuration()
                      * 60L));
      form.setWorkerId(
          testSetupHelper.getTestWorkers().get(CREATED_ASSIGNMENT_WORKER_POSITION_1).getId());
      form.setState(CREATED_ASSIGNMENT_STATE_1);
      mockMvc
          .perform(
              post("/api/assignment")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form)))
          .andExpect(status().isOk());
      apartment =
          apartmentRepository
              .findById(assignment.getTask().getApartment().getId())
              .orElseThrow(EntityNotFoundException::new);
      assertEquals(ApartmentState.READY, apartment.getState());
    }
  }
}
