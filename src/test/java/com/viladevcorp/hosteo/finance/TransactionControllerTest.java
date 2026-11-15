package com.viladevcorp.hosteo.finance;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viladevcorp.hosteo.common.BaseControllerTest;
import com.viladevcorp.hosteo.common.TestUtils;
import com.viladevcorp.hosteo.model.Apartment;
import com.viladevcorp.hosteo.model.Assignment;
import com.viladevcorp.hosteo.model.Event;
import com.viladevcorp.hosteo.model.Page;
import com.viladevcorp.hosteo.model.Task;
import com.viladevcorp.hosteo.model.Transaction;
import com.viladevcorp.hosteo.model.Worker;
import com.viladevcorp.hosteo.model.dto.BalanceDto;
import com.viladevcorp.hosteo.model.dto.TransactionDto;
import com.viladevcorp.hosteo.model.forms.TransactionCreateForm;
import com.viladevcorp.hosteo.model.forms.TransactionSearchForm;
import com.viladevcorp.hosteo.model.forms.TransactionUpdateForm;
import com.viladevcorp.hosteo.model.types.AssignmentState;
import com.viladevcorp.hosteo.model.types.CategoryEnum;
import com.viladevcorp.hosteo.model.types.EventState;
import com.viladevcorp.hosteo.model.types.EventType;
import com.viladevcorp.hosteo.model.types.Language;
import com.viladevcorp.hosteo.model.types.TaskType;
import com.viladevcorp.hosteo.repository.ApartmentRepository;
import com.viladevcorp.hosteo.repository.AssignmentRepository;
import com.viladevcorp.hosteo.repository.EventRepository;
import com.viladevcorp.hosteo.repository.TaskRepository;
import com.viladevcorp.hosteo.repository.TransactionRepository;
import com.viladevcorp.hosteo.repository.UserRepository;
import com.viladevcorp.hosteo.repository.WorkerRepository;
import com.viladevcorp.hosteo.utils.ApiResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static com.viladevcorp.hosteo.common.TestConstants.*;

class TransactionControllerTest extends BaseControllerTest {

  @Autowired private UserRepository userRepository;
  @Autowired private WorkerRepository workerRepository;
  @Autowired private ApartmentRepository apartmentRepository;
  @Autowired private TaskRepository taskRepository;
  @Autowired private EventRepository eventRepository;
  @Autowired private AssignmentRepository assignmentRepository;
  @Autowired private TransactionRepository transactionRepository;
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() throws Exception {
    testSetupHelper.resetTestBase();
    TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);
  }

  private Worker createWorker(String name) {
    return workerRepository.save(
        Worker.builder().name(name).language(Language.EN).visible(true).build());
  }

  private Assignment createFinishedEventAssignment(double taskCost) {
    Worker worker = createWorker("Worker " + UUID.randomUUID());
    Apartment apartment =
        apartmentRepository.save(Apartment.builder().name("Apt " + UUID.randomUUID()).build());
    Task task =
        taskRepository.save(
            Task.builder()
                .name("Task " + UUID.randomUUID())
                .type(TaskType.MANDATORY)
                .category(CategoryEnum.CLEANING)
                .duration(60)
                .cost(taskCost)
                .apartment(apartment)
                .steps(new ArrayList<>())
                .build());
    Event event =
        eventRepository.save(
            Event.builder()
                .name("Event " + UUID.randomUUID())
                .type(EventType.BOOKING)
                .state(EventState.FINISHED)
                .apartment(apartment)
                .startDate(Instant.parse("2026-05-01T10:00:00Z"))
                .endDate(Instant.parse("2026-05-03T10:00:00Z"))
                .build());
    Assignment assignment =
        assignmentRepository.save(
            Assignment.builder()
                .task(task)
                .event(event)
                .worker(worker)
                .startDate(Instant.parse("2026-05-04T10:00:00Z"))
                .endDate(Instant.parse("2026-05-04T12:00:00Z"))
                .state(AssignmentState.PENDING)
                .build());
    return assignment;
  }

  private Transaction createPayment(UUID workerId, double amount) throws Exception {
    TransactionCreateForm form = new TransactionCreateForm();
    form.setWorkerId(workerId);
    form.setAmount(amount);
    String response =
        mockMvc
            .perform(
                post("/api/transactions")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    ApiResponse<TransactionDto> result =
        objectMapper.readValue(response, new TypeReference<ApiResponse<TransactionDto>>() {});
    return transactionRepository.findById(result.getData().getId()).orElse(null);
  }

  @Test
  void When_CreatePayment_Then_AmountStoredNegative() throws Exception {
    Worker worker = createWorker("John Paid");
    Transaction transaction = createPayment(worker.getId(), 50);

    assertNotNull(transaction);
    assertEquals(-50.0, transaction.getAmount());
    assertNull(transaction.getAssignment());
    assertEquals(worker.getId(), transaction.getWorker().getId());
  }

  @Test
  void When_UpdatePayment_Then_AmountUpdated() throws Exception {
    Worker worker = createWorker("John Updated");
    Transaction transaction = createPayment(worker.getId(), 50);

    TransactionUpdateForm form = new TransactionUpdateForm();
    form.setAmount(80);
    mockMvc
        .perform(
            put("/api/transactions/" + transaction.getId())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(form)))
        .andExpect(status().isOk());

    Transaction updated = transactionRepository.findById(transaction.getId()).orElse(null);
    assertNotNull(updated);
    assertEquals(-80.0, updated.getAmount());
  }

  @Test
  void When_DeletePayment_Then_Removed() throws Exception {
    Worker worker = createWorker("John Deleted");
    Transaction transaction = createPayment(worker.getId(), 50);

    mockMvc
        .perform(delete("/api/transactions/" + transaction.getId()))
        .andExpect(status().isOk());

    assertTrue(transactionRepository.findById(transaction.getId()).isEmpty());
  }

  @Test
  void When_CompleteAssignment_Then_DebtCreated() throws Exception {
    Assignment assignment = createFinishedEventAssignment(100);
    mockMvc
        .perform(
            patch(
                "/api/assignment/"
                    + assignment.getId()
                    + "/state/"
                    + AssignmentState.FINISHED))
        .andExpect(status().isOk());

    List<Transaction> transactions = transactionRepository.findAll();
    assertEquals(1, transactions.size());
    assertEquals(100.0, transactions.get(0).getAmount());
    assertEquals(assignment.getId(), transactions.get(0).getAssignment().getId());
    assertEquals(assignment.getWorker().getId(), transactions.get(0).getWorker().getId());
  }

  @Test
  void When_SearchTransactions_Then_DebtIncludesTaskAndApartmentNames() throws Exception {
    Assignment assignment = createFinishedEventAssignment(100);
    mockMvc
        .perform(
            patch(
                "/api/assignment/"
                    + assignment.getId()
                    + "/state/"
                    + AssignmentState.FINISHED))
        .andExpect(status().isOk());

    TransactionSearchForm form = new TransactionSearchForm();
    form.setIsPayment(false);

    String response =
        mockMvc
            .perform(
                post("/api/transactions/search")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    ApiResponse<Page<TransactionDto>> result =
        objectMapper.readValue(
            response, new TypeReference<ApiResponse<Page<TransactionDto>>>() {});
    assertEquals(1, result.getData().getContent().size());

    TransactionDto dto = result.getData().getContent().get(0);
    assertEquals(assignment.getId(), dto.getAssignmentId());
    assertEquals(assignment.getTask().getName(), dto.getTaskName());
    assertEquals(assignment.getTask().getApartment().getName(), dto.getApartmentName());
  }

  @Test
  void When_SearchTransactions_Then_PaginationMetadataIsReturned() throws Exception {
    Worker worker = createWorker("Paged Worker");
    createPayment(worker.getId(), 10);
    createPayment(worker.getId(), 20);
    createPayment(worker.getId(), 30);

    TransactionSearchForm form = new TransactionSearchForm();
    form.setWorkerId(worker.getId());
    form.setPageNumber(0);
    form.setPageSize(2);

    String response =
        mockMvc
            .perform(
                post("/api/transactions/search")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    ApiResponse<Page<TransactionDto>> result =
        objectMapper.readValue(
            response, new TypeReference<ApiResponse<Page<TransactionDto>>>() {});
    assertEquals(2, result.getData().getContent().size());
    assertEquals(2, result.getData().getTotalPages());
    assertEquals(3, result.getData().getTotalRows());
  }

  @Test
  void When_SearchTransactions_Then_FiltersByDateRange() throws Exception {
    Worker worker = createWorker("Dated Worker");
    createPayment(worker.getId(), 40);

    TransactionSearchForm inRange = new TransactionSearchForm();
    inRange.setStartDate(Instant.now().minusSeconds(3600));
    inRange.setEndDate(Instant.now().plusSeconds(3600));
    assertEquals(1, searchTransactions(inRange).getData().getTotalRows());

    TransactionSearchForm outOfRange = new TransactionSearchForm();
    outOfRange.setStartDate(Instant.now().minusSeconds(7200));
    outOfRange.setEndDate(Instant.now().minusSeconds(3600));
    assertEquals(0, searchTransactions(outOfRange).getData().getTotalRows());
  }

  private ApiResponse<Page<TransactionDto>> searchTransactions(TransactionSearchForm form)
      throws Exception {
    String response =
        mockMvc
            .perform(
                post("/api/transactions/search")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(form)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return objectMapper.readValue(
        response, new TypeReference<ApiResponse<Page<TransactionDto>>>() {});
  }

  @Test
  void When_RevertCompletedAssignment_Then_DebtDeleted() throws Exception {
    Assignment assignment = createFinishedEventAssignment(100);
    mockMvc
        .perform(
            patch(
                "/api/assignment/"
                    + assignment.getId()
                    + "/state/"
                    + AssignmentState.FINISHED))
        .andExpect(status().isOk());
    assertEquals(1, transactionRepository.count());

    mockMvc
        .perform(
            patch(
                "/api/assignment/"
                    + assignment.getId()
                    + "/state/"
                    + AssignmentState.PENDING))
        .andExpect(status().isOk());
    assertEquals(0, transactionRepository.count());
  }

  @Test
  void When_CompleteZeroCostAssignment_Then_NoTransaction() throws Exception {
    Assignment assignment = createFinishedEventAssignment(0);
    mockMvc
        .perform(
            patch(
                "/api/assignment/"
                    + assignment.getId()
                    + "/state/"
                    + AssignmentState.FINISHED))
        .andExpect(status().isOk());
    assertEquals(0, transactionRepository.count());
  }

  @Test
  void When_GetBalances_Then_AllWorkersIncludedOrderedDesc() throws Exception {
    Worker workerA = createWorker("Balanced A");
    Worker workerB = createWorker("Balanced B");
    createPayment(workerA.getId(), 100);

    String response =
        mockMvc
            .perform(get("/api/transactions/balances"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    ApiResponse<List<BalanceDto>> result =
        objectMapper.readValue(
            response, new TypeReference<ApiResponse<List<BalanceDto>>>() {});
    List<BalanceDto> balances = result.getData();

    assertEquals(2, balances.size());
    BalanceDto first = balances.get(0);
    BalanceDto second = balances.get(1);
    assertTrue(first.getBalance() >= second.getBalance());

    double balanceA = balances.stream()
        .filter(b -> b.getWorker().getId().equals(workerA.getId()))
        .findFirst()
        .orElseThrow()
        .getBalance();
    double balanceB = balances.stream()
        .filter(b -> b.getWorker().getId().equals(workerB.getId()))
        .findFirst()
        .orElseThrow()
        .getBalance();
    assertEquals(-100.0, balanceA);
    assertEquals(0.0, balanceB);

    BalanceDto balanceOfA = balances.stream()
        .filter(b -> b.getWorker().getId().equals(workerA.getId()))
        .findFirst()
        .orElseThrow();
    assertEquals(workerA.getName(), balanceOfA.getWorker().getName());
    assertEquals(Language.EN, balanceOfA.getWorker().getLanguage());
    assertNull(balanceOfA.getWorker().getImageUrl());
  }

  @Test
  void When_UpdateDebt_Then_Conflict() throws Exception {
    Assignment assignment = createFinishedEventAssignment(100);
    mockMvc
        .perform(
            patch(
                "/api/assignment/"
                    + assignment.getId()
                    + "/state/"
                    + AssignmentState.FINISHED))
        .andExpect(status().isOk());
    Transaction debt = transactionRepository.findAll().get(0);

    TransactionUpdateForm form = new TransactionUpdateForm();
    form.setAmount(120);
    mockMvc
        .perform(
            put("/api/transactions/" + debt.getId())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(form)))
        .andExpect(status().isConflict());
  }

  @Test
  void When_DeleteDebt_Then_Conflict() throws Exception {
    Assignment assignment = createFinishedEventAssignment(100);
    mockMvc
        .perform(
            patch(
                "/api/assignment/"
                    + assignment.getId()
                    + "/state/"
                    + AssignmentState.FINISHED))
        .andExpect(status().isOk());
    Transaction debt = transactionRepository.findAll().get(0);

    mockMvc
        .perform(delete("/api/transactions/" + debt.getId()))
        .andExpect(status().isConflict());
  }
}
