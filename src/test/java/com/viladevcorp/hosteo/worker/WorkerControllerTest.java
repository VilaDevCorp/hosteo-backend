package com.viladevcorp.hosteo.worker;

import static com.viladevcorp.hosteo.common.TestConstants.*;

import java.util.List;
import java.util.UUID;

import com.viladevcorp.hosteo.model.dto.WorkerDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viladevcorp.hosteo.model.Worker;
import com.viladevcorp.hosteo.common.BaseControllerTest;
import com.viladevcorp.hosteo.common.TestUtils;
import com.viladevcorp.hosteo.model.Page;
import com.viladevcorp.hosteo.model.forms.WorkerCreateForm;
import com.viladevcorp.hosteo.model.forms.WorkerSearchForm;
import com.viladevcorp.hosteo.model.forms.WorkerUpdateForm;
import com.viladevcorp.hosteo.repository.UserRepository;
import com.viladevcorp.hosteo.repository.WorkerRepository;
import com.viladevcorp.hosteo.utils.ApiResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WorkerControllerTest extends BaseControllerTest {

  @Autowired private UserRepository userRepository;
  @Autowired private WorkerRepository workerRepository;
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @BeforeEach
  void setup() {
    TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
    testSetupHelper.resetTestWorkers();
  }

  @AfterEach
  void clean() {
    workerRepository.deleteAll();
  }

  @Nested
  @DisplayName("Create workers")
  class CreateWorkers {
    @Test
    void When_CreateWorker_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      WorkerCreateForm form = new WorkerCreateForm();

      form.setName(NEW_WORKER_NAME_1);
      form.setLanguage(NEW_WORKER_LANGUAGE_1);
      form.setVisible(NEW_WORKER_VISIBLE_1);

      String resultString =
          mockMvc
              .perform(
                  post("/api/worker")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(form)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();
      ApiResponse<WorkerDto> result = null;
      TypeReference<ApiResponse<WorkerDto>> typeReference =
          new TypeReference<ApiResponse<WorkerDto>>() {};

      try {
        result = objectMapper.readValue(resultString, typeReference);
      } catch (Exception e) {
        fail("Error parsing response");
      }
      WorkerDto returnedWorker = result.getData();
      assertEquals(NEW_WORKER_NAME_1, returnedWorker.getName());
      assertEquals(NEW_WORKER_LANGUAGE_1, returnedWorker.getLanguage());
      assertEquals(NEW_WORKER_VISIBLE_1, returnedWorker.isVisible());
    }

    @Test
    void When_LeavingBlankSurname_BadRequest() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      WorkerCreateForm form = new WorkerCreateForm();
      // Name is not set
      form.setVisible(NEW_WORKER_VISIBLE_1);

      mockMvc
          .perform(
              post("/api/worker")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("Get worker")
  class GetWorker {
    @Test
    void When_GetWorker_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      String resultString =
          mockMvc
              .perform(
                  get("/api/worker/" + testSetupHelper.getTestWorkers().get(0).getId().toString()))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();
      ApiResponse<WorkerDto> result = null;
      TypeReference<ApiResponse<WorkerDto>> typeReference =
          new TypeReference<ApiResponse<WorkerDto>>() {};

      try {
        result = objectMapper.readValue(resultString, typeReference);
      } catch (Exception e) {
        fail("Error parsing response");
      }
      WorkerDto returnedWorker = result.getData();
      assertEquals(CREATED_WORKER_NAME_1, returnedWorker.getName());
    }

    @Test
    void When_GetWorkerNotOwned_NotFound() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_2, userRepository);
      mockMvc
          .perform(get("/api/worker/" + testSetupHelper.getTestWorkers().get(0).getId().toString()))
          .andExpect(status().isNotFound());
    }

    @Test
    void When_GetWorkerNotExist_NotFound() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      mockMvc.perform(get("/api/worker/" + UUID.randomUUID())).andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("Update workers")
  class UpdateWorkers {
    @Test
    void When_UpdateWorker_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      WorkerUpdateForm form = new WorkerUpdateForm();
      Worker workerToUpdate =
          workerRepository.findById(testSetupHelper.getTestWorkers().get(0).getId()).orElse(null);
      BeanUtils.copyProperties(workerToUpdate, form);
      form.setName(UPDATED_WORKER_NAME);
      form.setLanguage(UPDATED_WORKER_LANGUAGE);
      form.setVisible(UPDATED_WORKER_VISIBLE);

      mockMvc
          .perform(
              patch("/api/worker")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form)))
          .andExpect(status().isOk());
      Worker workerUpdated =
          workerRepository.findById(testSetupHelper.getTestWorkers().get(0).getId()).orElse(null);
      assertEquals(UPDATED_WORKER_NAME, workerUpdated.getName());
      assertEquals(UPDATED_WORKER_LANGUAGE, workerUpdated.getLanguage());
      assertEquals(UPDATED_WORKER_VISIBLE, workerUpdated.isVisible());
    }

    @Test
    void When_UpdateWorkerNotOwned_NotFound() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_2, userRepository);
      WorkerUpdateForm form = new WorkerUpdateForm();
      Worker workerToUpdate =
          workerRepository.findById(testSetupHelper.getTestWorkers().get(0).getId()).orElse(null);
      BeanUtils.copyProperties(workerToUpdate, form);
      form.setName(UPDATED_WORKER_NAME);

      mockMvc
          .perform(
              patch("/api/worker")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form)))
          .andExpect(status().isNotFound());
    }

    @Test
    void When_UpdateWorkerNotExist_NotFound() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      WorkerUpdateForm form = new WorkerUpdateForm();
      form.setId(UUID.fromString(UUID.randomUUID().toString()));
      form.setName(UPDATED_WORKER_NAME);

      mockMvc
          .perform(
              patch("/api/worker")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form)))
          .andExpect(status().isNotFound());
    }

    @Test
    void When_NameIsEmptyInForm_BadRequest() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      WorkerUpdateForm form = new WorkerUpdateForm();
      Worker workerToUpdate =
          workerRepository.findById(testSetupHelper.getTestWorkers().get(0).getId()).orElse(null);
      BeanUtils.copyProperties(workerToUpdate, form);
      form.setName("");

      mockMvc
          .perform(
              patch("/api/worker")
                  .contentType("application/json")
                  .content(objectMapper.writeValueAsString(form)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("Search workers")
  class SearchWorkers {
    @Test
    void When_SearchAllWorkers_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      WorkerSearchForm searchFormObj = new WorkerSearchForm();
      searchFormObj.setPageNumber(0);
      String resultString =
          mockMvc
              .perform(
                  post("/api/worker/search")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(searchFormObj)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();
      ApiResponse<Page<WorkerDto>> result = null;
      TypeReference<ApiResponse<Page<WorkerDto>>> typeReference =
          new TypeReference<ApiResponse<Page<WorkerDto>>>() {};

      try {
        result = objectMapper.readValue(resultString, typeReference);
      } catch (Exception e) {
        fail("Error parsing response");
      }
      Page<WorkerDto> returnedPage = result.getData();
      List<WorkerDto> workers = returnedPage.getContent();
      assertEquals(4, workers.size());
    }

    @Test
    void When_SearchAllWorkersWithPagination_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      WorkerSearchForm searchFormObj = new WorkerSearchForm();
      searchFormObj.setPageNumber(0);
      searchFormObj.setPageSize(2);
      String resultString =
          mockMvc
              .perform(
                  post("/api/worker/search")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(searchFormObj)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();
      ApiResponse<Page<WorkerDto>> result = null;
      TypeReference<ApiResponse<Page<WorkerDto>>> typeReference =
          new TypeReference<ApiResponse<Page<WorkerDto>>>() {};

      try {
        result = objectMapper.readValue(resultString, typeReference);
      } catch (Exception e) {
        fail("Error parsing response");
      }
      Page<WorkerDto> returnedPage = result.getData();
      List<WorkerDto> workers = returnedPage.getContent();
      assertEquals(2, workers.size());
      assertEquals(2, returnedPage.getTotalPages());
      assertEquals(4, returnedPage.getTotalRows());
    }

    @Test
    void When_SearchNoWorkers_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_2, userRepository);

      WorkerSearchForm searchFormObj = new WorkerSearchForm();
      searchFormObj.setPageNumber(-1);
      String resultString =
          mockMvc
              .perform(
                  post("/api/worker/search")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(searchFormObj)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();
      ApiResponse<Page<WorkerDto>> result = null;
      TypeReference<ApiResponse<Page<WorkerDto>>> typeReference =
          new TypeReference<ApiResponse<Page<WorkerDto>>>() {};

      try {
        result = objectMapper.readValue(resultString, typeReference);
      } catch (Exception e) {
        fail("Error parsing response");
      }
      Page<WorkerDto> returnedPage = result.getData();
      List<WorkerDto> workers = returnedPage.getContent();
      assertEquals(0, workers.size());
    }

    @Test
    void When_SearchWorkersByName_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

      // Search for workers with name containing "john"
      WorkerSearchForm searchFormObj = new WorkerSearchForm();
      searchFormObj.setName("john");
      searchFormObj.setPageNumber(-1);
      String resultString =
          mockMvc
              .perform(
                  post("/api/worker/search")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(searchFormObj)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();
      ApiResponse<Page<WorkerDto>> result = null;
      TypeReference<ApiResponse<Page<WorkerDto>>> typeReference =
          new TypeReference<ApiResponse<Page<WorkerDto>>>() {};

      try {
        result = objectMapper.readValue(resultString, typeReference);
      } catch (Exception e) {
        fail("Error parsing response");
      }
      Page<WorkerDto> returnedPage = result.getData();
      List<WorkerDto> workers = returnedPage.getContent();
      assertEquals(2, workers.size());
      for (WorkerDto worker : workers) {
        assertTrue(worker.getName().toLowerCase().contains("john"));
      }
    }

    @Test
    void When_SearchWorkersByVisible_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      WorkerSearchForm searchFormObj = new WorkerSearchForm();
      searchFormObj.setVisible(true);
      searchFormObj.setPageNumber(-1);
      String resultString =
          mockMvc
              .perform(
                  post("/api/worker/search")
                      .contentType("application/json")
                      .content(objectMapper.writeValueAsString(searchFormObj)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();
      ApiResponse<Page<WorkerDto>> result = null;
      TypeReference<ApiResponse<Page<WorkerDto>>> typeReference =
          new TypeReference<ApiResponse<Page<WorkerDto>>>() {};
      try {
        result = objectMapper.readValue(resultString, typeReference);
      } catch (Exception e) {
        fail("Error parsing response");
      }
      Page<WorkerDto> returnedPage = result.getData();
      List<WorkerDto> workers = returnedPage.getContent();
      assertEquals(3, workers.size());
      for (WorkerDto worker : workers) {
        assertTrue(worker.isVisible());
      }
    }
  }

  @Nested
  @DisplayName("Delete workers")
  class DeleteWorkers {
    @Test
    void When_DeleteWorker_Ok() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      mockMvc
          .perform(
              org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(
                  "/api/worker/" + testSetupHelper.getTestWorkers().get(0).getId().toString()))
          .andExpect(status().isOk());
      boolean exists = workerRepository.existsById(testSetupHelper.getTestWorkers().get(0).getId());
      assertFalse(exists, "Worker was not deleted");
    }

    @Test
    void When_DeleteWorkerNotOwned_NotFound() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_2, userRepository);
      mockMvc
          .perform(
              org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(
                  "/api/worker/" + testSetupHelper.getTestWorkers().get(0).getId().toString()))
          .andExpect(status().isNotFound());
    }

    @Test
    void When_DeleteWorkerNotExist_NotFound() throws Exception {
      TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
      mockMvc
          .perform(
              org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(
                  "/api/worker/" + UUID.randomUUID()))
          .andExpect(status().isNotFound());
    }
  }
}
