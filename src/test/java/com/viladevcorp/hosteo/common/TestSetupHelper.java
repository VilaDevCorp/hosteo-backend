package com.viladevcorp.hosteo.common;

import java.util.ArrayList;
import java.util.List;

import com.viladevcorp.hosteo.model.types.EventType;
import com.viladevcorp.hosteo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.viladevcorp.hosteo.model.Apartment;
import com.viladevcorp.hosteo.model.Assignment;
import com.viladevcorp.hosteo.model.Event;
import com.viladevcorp.hosteo.model.Task;
import com.viladevcorp.hosteo.model.Template;
import com.viladevcorp.hosteo.model.User;
import com.viladevcorp.hosteo.model.Worker;
import com.viladevcorp.hosteo.service.AuthService;

import lombok.Getter;
import lombok.Setter;

import static com.viladevcorp.hosteo.common.TestConstants.*;

@Component
@Getter
@Setter
public class TestSetupHelper {

  @Autowired AuthService authService;

  @Autowired ValidationCodeRepository validationCodeRepository;

  @Autowired WorkerRepository workerRepository;

  @Autowired UserRepository userRepository;

  @Autowired ApartmentRepository apartmentRepository;

  @Autowired EventRepository eventRepository;

  @Autowired TemplateRepository templateRepository;

  @Autowired TaskRepository taskRepository;

  @Autowired AssignmentRepository assignmentRepository;

  @Autowired ImpBookingRepository impBookingRepository;

  private List<User> testUsers;

  private List<Apartment> testApartments;

  private List<Event> testEvents;

  private List<Worker> testWorkers;

  private List<Template> testTemplates;

  private List<Task> testTasks;

  private List<Assignment> testAssignments;

  private Event conflictEvent;

  private Assignment conflictAssignment;

  public void createTestUsers() throws Exception {
    TestUtils.injectUserSession(null, userRepository);
    User us1 =
        authService.registerUser(
            ACTIVE_USER_EMAIL_1, ACTIVE_USER_USERNAME_1, ACTIVE_USER_PASSWORD_1);
    us1.setValidated(true);
    us1 = userRepository.save(us1);
    User us2 =
        authService.registerUser(
            ACTIVE_USER_EMAIL_2, ACTIVE_USER_USERNAME_2, ACTIVE_USER_PASSWORD_2);
    us2.setValidated(true);
    us2 = userRepository.save(us2);
    testUsers = List.of(us1, us2);
  }

  public void deleteAll() {
    impBookingRepository.deleteAll();
    assignmentRepository.deleteAll();
    taskRepository.deleteAll();
    templateRepository.deleteAll();
    eventRepository.deleteAll();
    apartmentRepository.deleteAll();
    workerRepository.deleteAll();
    validationCodeRepository.deleteAll();
    userRepository.deleteAll();
  }

  public void resetTestBase() throws Exception {
    deleteAll();
    createTestUsers();
  }

  public void createTestApartments() {
    if (apartmentRepository.count() > 0) {
      return;
    }
    TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

    Apartment apt1 =
        apartmentRepository.save(Apartment.builder().name(CREATED_APARTMENT_NAME_1).build());

    Apartment apt2 =
        apartmentRepository.save(Apartment.builder().name(CREATED_APARTMENT_NAME_2).build());

    Apartment apt3 =
        apartmentRepository.save(Apartment.builder().name(CREATED_APARTMENT_NAME_3).build());

    Apartment apt4 =
        apartmentRepository.save(Apartment.builder().name(CREATED_APARTMENT_NAME_4).build());

    testApartments = List.of(apt1, apt2, apt3, apt4);
  }

  public void resetTestApartments() {
    apartmentRepository.deleteAll();
    createTestApartments();
  }

  public void createTestWorkers() {
    if (workerRepository.count() > 0) {
      return;
    }
    TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

    Worker wk1 =
        workerRepository.save(
            Worker.builder()
                .name(CREATED_WORKER_NAME_1)
                .language(CREATED_WORKER_LANGUAGE_1)
                .visible(CREATED_WORKER_VISIBLE_1)
                .build());

    Worker wk2 =
        workerRepository.save(
            Worker.builder()
                .name(CREATED_WORKER_NAME_2)
                .language(CREATED_WORKER_LANGUAGE_2)
                .visible(CREATED_WORKER_VISIBLE_2)
                .build());

    Worker wk3 =
        workerRepository.save(
            Worker.builder()
                .name(CREATED_WORKER_NAME_3)
                .language(CREATED_WORKER_LANGUAGE_3)
                .visible(CREATED_WORKER_VISIBLE_3)
                .build());

    Worker wk4 =
        workerRepository.save(
            Worker.builder()
                .name(CREATED_WORKER_NAME_4)
                .language(CREATED_WORKER_LANGUAGE_4)
                .visible(CREATED_WORKER_VISIBLE_4)
                .build());

    testWorkers = List.of(wk1, wk2, wk3, wk4);
  }

  public void resetTestWorkers() {
    workerRepository.deleteAll();
    createTestWorkers();
  }

  public void createTestEvents() throws Exception {

    if (eventRepository.count() > 0) {
      return;
    }

    createTestApartments();

    Event ev1 =
        eventRepository.save(
            Event.builder()
                .name(CREATED_EVENT_NAME_1)
                .apartment(testApartments.get(CREATED_EVENT_APARTMENT_POSITION_1))
                .startDate(TestUtils.dateStrToInstant(CREATED_EVENT_START_DATE_1))
                .endDate(TestUtils.dateStrToInstant(CREATED_EVENT_END_DATE_1))
                .state(CREATED_EVENT_STATE_1)
                .type(CREATED_EVENT_TYPE_1)
                .build());

    Event ev2 =
        eventRepository.save(
            Event.builder()
                .name(CREATED_EVENT_NAME_2)
                .apartment(testApartments.get(CREATED_EVENT_APARTMENT_POSITION_2))
                .startDate(TestUtils.dateStrToInstant(CREATED_EVENT_START_DATE_2))
                .endDate(TestUtils.dateStrToInstant(CREATED_EVENT_END_DATE_2))
                .state(CREATED_EVENT_STATE_2)
                .type(CREATED_EVENT_TYPE_2)
                .build());

    Event ev3 =
        eventRepository.save(
            Event.builder()
                .name(CREATED_EVENT_NAME_3)
                .apartment(testApartments.get(CREATED_EVENT_APARTMENT_POSITION_3))
                .startDate(TestUtils.dateStrToInstant(CREATED_EVENT_START_DATE_3))
                .endDate(TestUtils.dateStrToInstant(CREATED_EVENT_END_DATE_3))
                .state(CREATED_EVENT_STATE_3)
                .type(CREATED_EVENT_TYPE_3)
                .build());

    Event ev4 =
        eventRepository.save(
            Event.builder()
                .name(CREATED_EVENT_NAME_4)
                .apartment(testApartments.get(CREATED_EVENT_APARTMENT_POSITION_4))
                .startDate(TestUtils.dateStrToInstant(CREATED_EVENT_START_DATE_4))
                .endDate(TestUtils.dateStrToInstant(CREATED_EVENT_END_DATE_4))
                .state(CREATED_EVENT_STATE_4)
                .type(CREATED_EVENT_TYPE_4)
                .build());

    Event ev5 =
        eventRepository.save(
            Event.builder()
                .name(CREATED_EVENT_NAME_5)
                .apartment(testApartments.get(CREATED_EVENT_APARTMENT_POSITION_5))
                .startDate(TestUtils.dateStrToInstant(CREATED_EVENT_START_DATE_5))
                .endDate(TestUtils.dateStrToInstant(CREATED_EVENT_END_DATE_5))
                .state(CREATED_EVENT_STATE_5)
                .type(CREATED_EVENT_TYPE_5)
                .build());

    testEvents = List.of(ev1, ev2, ev3, ev4, ev5);
  }

  public void resetTestEvents() throws Exception {
    eventRepository.deleteAll();
    apartmentRepository.deleteAll();
    createTestEvents();
  }

  public void createTestTemplates() {

    if (templateRepository.count() > 0) {
      return;
    }
    TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

    Template tmpl1 =
        Template.builder()
            .name(CREATED_TEMPLATE_NAME_1)
            .category(CREATED_TEMPLATE_CATEGORY_1)
            .duration(CREATED_TEMPLATE_DURATION_1)
            .type(CREATED_TEMPLATE_TYPE_1)
            .steps(new ArrayList<>())
            .build();
    tmpl1 = templateRepository.save(tmpl1);

    Template tmpl2 =
        Template.builder()
            .name(CREATED_TEMPLATE_NAME_2)
            .category(CREATED_TEMPLATE_CATEGORY_2)
            .duration(CREATED_TEMPLATE_DURATION_2)
            .type(CREATED_TEMPLATE_TYPE_2)
            .steps(new ArrayList<>())
            .build();
    tmpl2 = templateRepository.save(tmpl2);

    Template tmpl3 =
        Template.builder()
            .name(CREATED_TEMPLATE_NAME_3)
            .category(CREATED_TEMPLATE_CATEGORY_3)
            .duration(CREATED_TEMPLATE_DURATION_3)
            .type(CREATED_TEMPLATE_TYPE_3)
            .steps(new ArrayList<>())
            .build();

    tmpl3 = templateRepository.save(tmpl3);
    testTemplates = List.of(tmpl1, tmpl2, tmpl3);
  }

  public void resetTestTemplates() {
    templateRepository.deleteAll();
    createTestTemplates();
  }

  public void createTestTasks() {
    if (taskRepository.count() > 0) {
      return;
    }
    createTestApartments();

    Task task1 =
        Task.builder()
            .name(CREATED_TASK_NAME_1)
            .type(CREATED_TASK_TYPE_1)
            .category(CREATED_TASK_CATEGORY_1)
            .duration(CREATED_TASK_DURATION_1)
            .apartment(testApartments.get(CREATED_TASK_APARTMENT_POSITION_1))
            .steps(new ArrayList<>())
            .build();
    task1 = taskRepository.save(task1);

    Task task2 =
        Task.builder()
            .name(CREATED_TASK_NAME_2)
            .type(CREATED_TASK_TYPE_2)
            .category(CREATED_TASK_CATEGORY_2)
            .duration(CREATED_TASK_DURATION_2)
            .apartment(testApartments.get(CREATED_TASK_APARTMENT_POSITION_2))
            .steps(new ArrayList<>())
            .build();
    task2 = taskRepository.save(task2);

    Task task3 =
        Task.builder()
            .name(CREATED_TASK_NAME_3)
            .type(CREATED_TASK_TYPE_3)
            .category(CREATED_TASK_CATEGORY_3)
            .duration(CREATED_TASK_DURATION_3)
            .apartment(testApartments.get(CREATED_TASK_APARTMENT_POSITION_3))
            .steps(new ArrayList<>())
            .build();
    task3 = taskRepository.save(task3);

    Task task4 =
        Task.builder()
            .name(CREATED_TASK_NAME_4)
            .type(CREATED_TASK_TYPE_4)
            .category(CREATED_TASK_CATEGORY_4)
            .duration(CREATED_TASK_DURATION_4)
            .apartment(testApartments.get(CREATED_TASK_APARTMENT_POSITION_4))
            .steps(new ArrayList<>())
            .build();
    task4 = taskRepository.save(task4);

    Task task5 =
        Task.builder()
            .name(CREATED_TASK_NAME_5)
            .type(CREATED_TASK_TYPE_5)
            .category(CREATED_TASK_CATEGORY_5)
            .duration(CREATED_TASK_DURATION_5)
            .apartment(testApartments.get(CREATED_TASK_APARTMENT_POSITION_5))
            .steps(new ArrayList<>())
            .build();
    task5 = taskRepository.save(task5);

    testTasks = List.of(task1, task2, task3, task4, task5);
  }

  public void resetTestTasks() {
    taskRepository.deleteAll();
    apartmentRepository.deleteAll();
    createTestTasks();
  }

  public void createTestAssignments() throws Exception {

    if (assignmentRepository.count() > 0) {
      return;
    }

    createTestWorkers();
    createTestTasks();
    createTestEvents();

    Assignment assignment1 =
        Assignment.builder()
            .task(testTasks.get(CREATED_ASSIGNMENT_TASK_POSITION_1))
            .event(testEvents.get(CREATED_ASSIGNMENT_EVENT_POSITION_1))
            .startDate(TestUtils.dateStrToInstant(CREATED_ASSIGNMENT_START_DATE_1))
            .endDate(
                TestUtils.dateStrToInstant(CREATED_ASSIGNMENT_START_DATE_1)
                    .plusSeconds(
                        testTasks.get(CREATED_ASSIGNMENT_TASK_POSITION_1).getDuration() * 60L))
            .worker(testWorkers.get(CREATED_ASSIGNMENT_WORKER_POSITION_1))
            .state(CREATED_ASSIGNMENT_STATE_1)
            .build();
    assignment1 = assignmentRepository.save(assignment1);

    Assignment assignment2 =
        Assignment.builder()
            .task(testTasks.get(CREATED_ASSIGNMENT_TASK_POSITION_2))
            .event(testEvents.get(CREATED_ASSIGNMENT_EVENT_POSITION_2))
            .startDate(TestUtils.dateStrToInstant(CREATED_ASSIGNMENT_START_DATE_2))
            .endDate(
                TestUtils.dateStrToInstant(CREATED_ASSIGNMENT_START_DATE_2)
                    .plusSeconds(
                        testTasks.get(CREATED_ASSIGNMENT_TASK_POSITION_2).getDuration() * 60L))
            .worker(testWorkers.get(CREATED_ASSIGNMENT_WORKER_POSITION_2))
            .state(CREATED_ASSIGNMENT_STATE_2)
            .build();
    assignment2 = assignmentRepository.save(assignment2);

    Assignment assignment3 =
        Assignment.builder()
            .task(testTasks.get(CREATED_ASSIGNMENT_TASK_POSITION_3))
            .event(testEvents.get(CREATED_ASSIGNMENT_EVENT_POSITION_3))
            .startDate(TestUtils.dateStrToInstant(CREATED_ASSIGNMENT_START_DATE_3))
            .endDate(
                TestUtils.dateStrToInstant(CREATED_ASSIGNMENT_START_DATE_3)
                    .plusSeconds(
                        testTasks.get(CREATED_ASSIGNMENT_TASK_POSITION_3).getDuration() * 60L))
            .worker(testWorkers.get(CREATED_ASSIGNMENT_WORKER_POSITION_3))
            .state(CREATED_ASSIGNMENT_STATE_3)
            .build();
    assignment3 = assignmentRepository.save(assignment3);

    Assignment assignment4 =
        Assignment.builder()
            .task(testTasks.get(CREATED_ASSIGNMENT_TASK_POSITION_4))
            .event(testEvents.get(CREATED_ASSIGNMENT_EVENT_POSITION_4))
            .startDate(TestUtils.dateStrToInstant(CREATED_ASSIGNMENT_START_DATE_4))
            .endDate(
                TestUtils.dateStrToInstant(CREATED_ASSIGNMENT_START_DATE_4)
                    .plusSeconds(
                        testTasks.get(CREATED_ASSIGNMENT_TASK_POSITION_4).getDuration() * 60L))
            .worker(testWorkers.get(CREATED_ASSIGNMENT_WORKER_POSITION_4))
            .state(CREATED_ASSIGNMENT_STATE_4)
            .build();

    assignment4 = assignmentRepository.save(assignment4);

    Assignment assignment5 =
        Assignment.builder()
            .task(testTasks.get(CREATED_ASSIGNMENT_TASK_POSITION_5))
            .event(testEvents.get(CREATED_ASSIGNMENT_EVENT_POSITION_5))
            .startDate(TestUtils.dateStrToInstant(CREATED_ASSIGNMENT_START_DATE_5))
            .endDate(
                TestUtils.dateStrToInstant(CREATED_ASSIGNMENT_START_DATE_5)
                    .plusSeconds(
                        testTasks.get(CREATED_ASSIGNMENT_TASK_POSITION_5).getDuration() * 60L))
            .worker(testWorkers.get(CREATED_ASSIGNMENT_WORKER_POSITION_5))
            .state(CREATED_ASSIGNMENT_STATE_5)
            .build();
    assignment5 = assignmentRepository.save(assignment5);

    testAssignments = List.of(assignment1, assignment2, assignment3, assignment4, assignment5);
  }

  public void deleteTestAssignments() {
    assignmentRepository.deleteAll();
    taskRepository.deleteAll();
    workerRepository.deleteAll();
    eventRepository.deleteAll();
    apartmentRepository.deleteAll();
  }

  public void resetAssignments() throws Exception {
    deleteTestAssignments();
    createTestAssignments();
  }

  public void createTestImportApartments() {
    if (apartmentRepository.count() > 0) {
      return;
    }
    TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

    Apartment apt1 =
        apartmentRepository.save(
            Apartment.builder()
                .name(CREATED_IMPORT_APARTMENT_NAME_1)
                .airbnbId(CREATED_IMPORT_APARTMENT_AIRBNB_ID_1)
                .bookingId(CREATED_IMPORT_APARTMENT_BOOKING_ID_1)
                .build());

    Apartment apt2 =
        apartmentRepository.save(
            Apartment.builder()
                .name(CREATED_IMPORT_APARTMENT_NAME_2)
                .airbnbId(CREATED_IMPORT_APARTMENT_AIRBNB_ID_2)
                .bookingId(CREATED_IMPORT_APARTMENT_BOOKING_ID_2)
                .build());

    Apartment apt3 =
        apartmentRepository.save(
            Apartment.builder()
                .name(CREATED_IMPORT_APARTMENT_NAME_3)
                .airbnbId(CREATED_IMPORT_APARTMENT_AIRBNB_ID_3)
                .bookingId(CREATED_IMPORT_APARTMENT_BOOKING_ID_3)
                .build());

    testApartments = List.of(apt1, apt2, apt3);
  }

  public void resetImportApartments() {
    assignmentRepository.deleteAll();
    eventRepository.deleteAll();
    apartmentRepository.deleteAll();
    impBookingRepository.deleteAll();
    createTestImportApartments();
  }

  public void createImportConflicts(boolean airbnb) throws Exception {
    TestUtils.injectUserSession(ACTIVE_USER_USERNAME_1, userRepository);

    Task taskConflict =
        Task.builder()
            .name(CREATED_TASK_NAME_1)
            .apartment(testApartments.get(0))
            .type(CREATED_TASK_TYPE_1)
            .category(CREATED_TASK_CATEGORY_1)
            .duration(CREATED_TASK_DURATION_1)
            .build();
    taskConflict = taskRepository.save(taskConflict);
    if (airbnb) {
      // Create airbnb conflicts before import
      Event conflictedEvent =
          Event.builder()
              .name(CREATED_EVENT_CONFLICT_NAME_1)
              .state(CREATED_EVENT_CONFLICT_STATE_1)
              .type(EventType.BOOKING)
              .apartment(testApartments.get(CREATED_EVENT_CONFLICT_APARTMENT_POSITION_1))
              .startDate(TestUtils.dateStrToInstant(CREATED_EVENT_AIRBNB_CONFLICT_START_DATE_1))
              .endDate(TestUtils.dateStrToInstant(CREATED_EVENT_AIRBNB_CONFLICT_END_DATE_1))
              .build();

      setConflictEvent(eventRepository.save(conflictedEvent));

      Assignment conflictedAssignment =
          Assignment.builder()
              .task(taskConflict)
              .startDate(
                  TestUtils.dateStrToInstant(CREATED_ASSIGNMENT_AIRBNB_CONFLICT_START_DATE_1))
              .endDate(
                  TestUtils.dateStrToInstant(CREATED_ASSIGNMENT_AIRBNB_CONFLICT_START_DATE_1)
                      .plusSeconds(taskConflict.getDuration() * 60L))
              .state(CREATED_ASSIGNMENT_CONFLICT_STATE_1)
              .worker(getTestWorkers().get(CREATED_ASSIGNMENT_CONFLICT_WORKER_POSITION_1))
              .build();

      setConflictAssignment(assignmentRepository.save(conflictedAssignment));
    } else {
      // Create booking conflicts before import
      Event conflictedEvent =
          Event.builder()
              .name(CREATED_EVENT_CONFLICT_NAME_1)
              .state(CREATED_EVENT_CONFLICT_STATE_1)
              .type(EventType.BOOKING)
              .apartment(testApartments.get(CREATED_EVENT_CONFLICT_APARTMENT_POSITION_1))
              .startDate(TestUtils.dateStrToInstant(CREATED_EVENT_BOOKING_CONFLICT_START_DATE_1))
              .endDate(TestUtils.dateStrToInstant(CREATED_EVENT_BOOKING_CONFLICT_END_DATE_1))
              .build();

      setConflictEvent(eventRepository.save(conflictedEvent));

      Assignment conflictedAssignment =
          Assignment.builder()
              .task(taskConflict)
              .startDate(
                  TestUtils.dateStrToInstant(CREATED_ASSIGNMENT_BOOKING_CONFLICT_START_DATE_1))
              .endDate(
                  TestUtils.dateStrToInstant(CREATED_ASSIGNMENT_BOOKING_CONFLICT_START_DATE_1)
                      .plusSeconds(taskConflict.getDuration() * 60L))
              .state(CREATED_ASSIGNMENT_CONFLICT_STATE_1)
              .worker(getTestWorkers().get(CREATED_ASSIGNMENT_CONFLICT_WORKER_POSITION_1))
              .build();

      setConflictAssignment(assignmentRepository.save(conflictedAssignment));
    }
  }

  public void resetImportConflicts(boolean airbnb) throws Exception {
    assignmentRepository.deleteAll();
    eventRepository.deleteAll();
    workerRepository.deleteAll();
    taskRepository.deleteAll();
    apartmentRepository.deleteAll();
    createTestWorkers();
    createTestImportApartments();
    createImportConflicts(airbnb);
  }
}
