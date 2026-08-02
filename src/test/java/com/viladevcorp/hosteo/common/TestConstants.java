package com.viladevcorp.hosteo.common;

import java.util.List;

import com.viladevcorp.hosteo.model.Address;
import com.viladevcorp.hosteo.model.types.*;

public class TestConstants {

  // Users
  public static final String ACTIVE_USER_EMAIL_1 = "test@gmail.com";
  public static final String ACTIVE_USER_USERNAME_1 = "test";
  public static final String ACTIVE_USER_PASSWORD_1 = "12test34";

  public static final String ACTIVE_USER_EMAIL_2 = "test2@gmail.com";
  public static final String ACTIVE_USER_USERNAME_2 = "test2";
  public static final String ACTIVE_USER_PASSWORD_2 = "12test34";

  public static final String NEW_USER_EMAIL = "test3@gmail.com";
  public static final String NEW_USER_USERNAME = "test3";
  public static final String NEW_USER_PASSWORD = "1234test";

  // Apartments
  public static final String CREATED_APARTMENT_NAME_1 = "Created apartment";

  public static final String CREATED_APARTMENT_NAME_2 = "Created loft 2";

  public static final String CREATED_APARTMENT_NAME_3 = "Created loft 3";

  public static final String CREATED_APARTMENT_NAME_4 = "Created apartment 4";

  public static final String NEW_APARTMENT_NAME_1 = "My Apartment 1";
  public static final String NEW_APARTMENT_AIRBNB_ID_1 = "airbnb-1";
  public static final String NEW_APARTMENT_BOOKING_ID_1 = "booking-1";
  public static final boolean NEW_APARTMENT_VISIBLE_1 = true;
  public static final Address NEW_APARTMENT_ADDRESS_1 =
      Address.builder()
          .street("123 Main St")
          .number("25")
          .apartmentNumber("Apt 5")
          .city("Sample City")
          .country("Sample Country")
          .zipCode("12345")
          .build();

  public static final String UPDATED_APARTMENT_NAME = "Updated apartment name";
  public static final String UPDATED_APARTMENT_AIRBNB_ID = "updated-airbnb-id";
  public static final String UPDATED_APARTMENT_BOOKING_ID = "updated-booking-id";
  public static final boolean UPDATED_APARTMENT_VISIBLE = false;
  public static final Address UPDATED_APARTMENT_ADDRESS =
      Address.builder()
          .street("Updated Street")
          .number("99B")
          .apartmentNumber("Apt 10")
          .city("Updated City")
          .country("Updated Country")
          .zipCode("99999")
          .build();

  // Workers
  public static final String CREATED_WORKER_NAME_1 = "John 1";
  public static final Language CREATED_WORKER_LANGUAGE_1 = Language.EN;
  public static final boolean CREATED_WORKER_VISIBLE_1 = true;

  public static final String CREATED_WORKER_NAME_2 = "John 2";
  public static final Language CREATED_WORKER_LANGUAGE_2 = Language.UK;
  public static final boolean CREATED_WORKER_VISIBLE_2 = true;

  public static final String CREATED_WORKER_NAME_3 = "Peter 3";
  public static final Language CREATED_WORKER_LANGUAGE_3 = Language.FR;
  public static final boolean CREATED_WORKER_VISIBLE_3 = false;

  public static final String CREATED_WORKER_NAME_4 = "Peter 4";
  public static final Language CREATED_WORKER_LANGUAGE_4 = Language.IT;
  public static final boolean CREATED_WORKER_VISIBLE_4 = true;

  public static final String NEW_WORKER_NAME_1 = "Created worker";
  public static final Language NEW_WORKER_LANGUAGE_1 = Language.UK;
  public static final boolean NEW_WORKER_VISIBLE_1 = true;

  public static final String UPDATED_WORKER_NAME = "Updated worker name";
  public static final Language UPDATED_WORKER_LANGUAGE = Language.FR;
  public static final boolean UPDATED_WORKER_VISIBLE = false;

  // Events
  public static final String CREATED_EVENT_NAME_1 = "Test Event 1";
  public static final String CREATED_EVENT_START_DATE_1 = "2025-11-21 15:30:00";
  public static final String CREATED_EVENT_END_DATE_1 = "2025-11-25 10:00:00";
  public static final EventState CREATED_EVENT_STATE_1 = EventState.FINISHED;
  public static final EventType CREATED_EVENT_TYPE_1 = EventType.BOOKING;
  public static final int CREATED_EVENT_APARTMENT_POSITION_1 = 0;

  public static final String CREATED_EVENT_NAME_2 = "Test Event 2";
  public static final String CREATED_EVENT_START_DATE_2 = "2025-12-01 14:00:00";
  public static final String CREATED_EVENT_END_DATE_2 = "2025-12-05 11:00:00";
  public static final EventState CREATED_EVENT_STATE_2 = EventState.IN_PROGRESS;
  public static final EventType CREATED_EVENT_TYPE_2 = EventType.BOOKING;
  public static final int CREATED_EVENT_APARTMENT_POSITION_2 = 1;

  public static final String CREATED_EVENT_NAME_3 = "Test Event 3";
  public static final String CREATED_EVENT_START_DATE_3 = "2026-01-10 16:00:00";
  public static final String CREATED_EVENT_END_DATE_3 = "2026-01-15 09:00:00";
  public static final EventState CREATED_EVENT_STATE_3 = EventState.PENDING;
  public static final EventType CREATED_EVENT_TYPE_3 = EventType.BOOKING;
  public static final int CREATED_EVENT_APARTMENT_POSITION_3 = 0;

  public static final String CREATED_EVENT_NAME_4 = "Test Event 4";
  public static final String CREATED_EVENT_START_DATE_4 = "2026-02-20 13:00:00";
  public static final String CREATED_EVENT_END_DATE_4 = "2026-02-25 12:00:00";
  public static final EventState CREATED_EVENT_STATE_4 = EventState.PENDING;
  public static final EventType CREATED_EVENT_TYPE_4 = EventType.BOOKING;

  public static final int CREATED_EVENT_APARTMENT_POSITION_4 = 1;

  public static final String CREATED_EVENT_NAME_5 = "Test Event 5";
  public static final String CREATED_EVENT_START_DATE_5 = "2026-01-20 16:00:00";
  public static final String CREATED_EVENT_END_DATE_5 = "2026-01-25 09:00:00";
  public static final EventState CREATED_EVENT_STATE_5 = EventState.PENDING;
  public static final EventType CREATED_EVENT_TYPE_5 = EventType.BOOKING;

  public static final int CREATED_EVENT_APARTMENT_POSITION_5 = 0;

  public static final String NEW_EVENT_NAME = "New Event";
  public static final String NEW_EVENT_START_DATE = "2026-03-01 14:00:00";
  public static final String NEW_EVENT_END_DATE = "2026-03-05 11:00:00";
  public static final EventType NEW_EVENT_TYPE_1 = EventType.BOOKING;
  public static final EventState NEW_EVENT_STATE = EventState.PENDING;
  public static final EventSource NEW_EVENT_SOURCE = EventSource.NONE;
  public static final int NEW_EVENT_APARTMENT_POSITION = 0;

  public static final String UPDATED_EVENT_NAME = "Updated Test Event";
  public static final EventSource UPDATED_EVENT_SOURCE = EventSource.BOOKING;
  public static final EventState UPDATED_EVENT_STATE = EventState.FINISHED;
  public static final String UPDATED_EVENT_START_DATE = "2025-11-21 16:30:00";
  public static final String UPDATED_EVENT_END_DATE = "2025-11-25 15:00:00";
  public static final EventType UPDATED_EVENT_TYPE = EventType.MAINTENANCE;
  public static final int UPDATED_EVENT_APARTMENT_POSITION = 1;

  // Templates
  public static final String CREATED_TEMPLATE_NAME_1 = "Test Template 1 cleaning";
  public static final TaskType CREATED_TEMPLATE_TYPE_1 = TaskType.MANDATORY;
  public static final CategoryEnum CREATED_TEMPLATE_CATEGORY_1 = CategoryEnum.CLEANING;
  public static final int CREATED_TEMPLATE_DURATION_1 = 120;

  public static final String CREATED_TEMPLATE_NAME_2 = "Test Template 2 maintenance";
  public static final TaskType CREATED_TEMPLATE_TYPE_2 = TaskType.EXTRA;
  public static final CategoryEnum CREATED_TEMPLATE_CATEGORY_2 = CategoryEnum.MAINTENANCE;
  public static final int CREATED_TEMPLATE_DURATION_2 = 180;

  public static final String CREATED_TEMPLATE_NAME_3 = "Test Template 3 maintenance";
  public static final TaskType CREATED_TEMPLATE_TYPE_3 = TaskType.EXTRA;
  public static final CategoryEnum CREATED_TEMPLATE_CATEGORY_3 = CategoryEnum.INSPECTION;
  public static final int CREATED_TEMPLATE_DURATION_3 = 90;

  public static final String NEW_TEMPLATE_NAME_1 = "New Template 1";
  public static final TaskType NEW_TEMPLATE_TYPE_1 = TaskType.MANDATORY;
  public static final CategoryEnum NEW_TEMPLATE_CATEGORY_1 = CategoryEnum.REPAIR;
  public static final int NEW_TEMPLATE_DURATION_1 = 60;
  public static final List<String> NEW_TEMPLATE_STEPS_1 = List.of("Step 1", "Step 2", "Step 3");

  public static final String UPDATED_TEMPLATE_NAME_1 = "Updated Template 1";
  public static final TaskType UPDATED_TEMPLATE_TYPE_1 = TaskType.EXTRA;
  public static final CategoryEnum UPDATED_TEMPLATE_CATEGORY_1 = CategoryEnum.INSPECTION;
  public static final int UPDATED_TEMPLATE_DURATION_1 = 150;
  public static final List<String> UPDATED_TEMPLATE_STEPS_1 =
      List.of("Updated Step 1", "Updated Step 2");

  // Tasks
  public static final String CREATED_TASK_NAME_1 = "Test Task 1 cleaning";
  public static final TaskType CREATED_TASK_TYPE_1 = TaskType.MANDATORY;
  public static final CategoryEnum CREATED_TASK_CATEGORY_1 = CategoryEnum.CLEANING;
  public static final int CREATED_TASK_DURATION_1 = 120;
  public static final int CREATED_TASK_APARTMENT_POSITION_1 = 0;

  public static final String CREATED_TASK_NAME_2 = "Test Task 2 maintenance";
  public static final TaskType CREATED_TASK_TYPE_2 = TaskType.EXTRA;
  public static final CategoryEnum CREATED_TASK_CATEGORY_2 = CategoryEnum.MAINTENANCE;
  public static final int CREATED_TASK_DURATION_2 = 180;
  public static final int CREATED_TASK_APARTMENT_POSITION_2 = 0;

  public static final String CREATED_TASK_NAME_3 = "Test Task 3 maintenance";
  public static final TaskType CREATED_TASK_TYPE_3 = TaskType.EXTRA;
  public static final CategoryEnum CREATED_TASK_CATEGORY_3 = CategoryEnum.INSPECTION;
  public static final int CREATED_TASK_DURATION_3 = 90;
  public static final int CREATED_TASK_APARTMENT_POSITION_3 = 1;

  public static final String CREATED_TASK_NAME_4 = "Test Task 4 cleaning";
  public static final TaskType CREATED_TASK_TYPE_4 = TaskType.MANDATORY;
  public static final CategoryEnum CREATED_TASK_CATEGORY_4 = CategoryEnum.CLEANING;
  public static final int CREATED_TASK_DURATION_4 = 60;
  public static final int CREATED_TASK_APARTMENT_POSITION_4 = 1;

  public static final String CREATED_TASK_NAME_5 = "Test Task 5 maintenance (extra)";
  public static final TaskType CREATED_TASK_TYPE_5 = TaskType.EXTRA;
  public static final CategoryEnum CREATED_TASK_CATEGORY_5 = CategoryEnum.MAINTENANCE;
  public static final int CREATED_TASK_DURATION_5 = 45;
  public static final int CREATED_TASK_APARTMENT_POSITION_5 = 1;

  public static final String NEW_TASK_NAME_1 = "New Task 1";
  public static final TaskType NEW_TASK_TYPE_1 = TaskType.MANDATORY;
  public static final CategoryEnum NEW_TASK_CATEGORY_1 = CategoryEnum.CLEANING;
  public static final int NEW_TASK_DURATION_1 = 60;
  public static final List<String> NEW_TASK_STEPS_1 = List.of("Step 1", "Step 2", "Step 3");
  public static final int NEW_TASK_APARTMENT_POSITION_1 = 0;

  public static final String UPDATED_TASK_NAME_1 = "Updated Task 1";
  public static final TaskType UPDATED_TASK_TYPE_1 = TaskType.EXTRA;
  public static final CategoryEnum UPDATED_TASK_CATEGORY_1 = CategoryEnum.INSPECTION;
  public static final int UPDATED_TASK_DURATION_1 = 150;
  public static final List<String> UPDATED_TASK_STEPS_1 =
      List.of("Updated Step 1", "Updated Step 2");

  // Assignments
  public static final AssignmentState CREATED_ASSIGNMENT_STATE_1 = AssignmentState.FINISHED;
  public static final String CREATED_ASSIGNMENT_START_DATE_1 = "2025-11-26 10:00:00";
  public static final int CREATED_ASSIGNMENT_TASK_POSITION_1 = 0;
  public static final int CREATED_ASSIGNMENT_WORKER_POSITION_1 = 0;
  public static final int CREATED_ASSIGNMENT_EVENT_POSITION_1 = 0;

  public static final AssignmentState CREATED_ASSIGNMENT_STATE_2 = AssignmentState.FINISHED;
  public static final String CREATED_ASSIGNMENT_START_DATE_2 = "2025-11-26 14:00:00";
  public static final int CREATED_ASSIGNMENT_TASK_POSITION_2 = 1;
  public static final int CREATED_ASSIGNMENT_WORKER_POSITION_2 = 0;
  public static final int CREATED_ASSIGNMENT_EVENT_POSITION_2 = 0;

  public static final AssignmentState CREATED_ASSIGNMENT_STATE_3 = AssignmentState.PENDING;
  public static final String CREATED_ASSIGNMENT_START_DATE_3 = "2025-12-06 12:00:00";
  public static final int CREATED_ASSIGNMENT_TASK_POSITION_3 = 2;
  public static final int CREATED_ASSIGNMENT_WORKER_POSITION_3 = 1;
  public static final int CREATED_ASSIGNMENT_EVENT_POSITION_3 = 1;

  public static final AssignmentState CREATED_ASSIGNMENT_STATE_4 = AssignmentState.PENDING;
  public static final String CREATED_ASSIGNMENT_START_DATE_4 = "2025-12-06 16:00:00";
  public static final int CREATED_ASSIGNMENT_TASK_POSITION_4 = 4;
  public static final int CREATED_ASSIGNMENT_WORKER_POSITION_4 = 1;
  public static final int CREATED_ASSIGNMENT_EVENT_POSITION_4 = 1;

  public static final AssignmentState CREATED_ASSIGNMENT_STATE_5 = AssignmentState.PENDING;
  public static final String CREATED_ASSIGNMENT_START_DATE_5 = "2026-01-16 16:00:00";
  public static final int CREATED_ASSIGNMENT_TASK_POSITION_5 = 3;
  public static final int CREATED_ASSIGNMENT_WORKER_POSITION_5 = 0;
  public static final int CREATED_ASSIGNMENT_EVENT_POSITION_5 = 1;

  public static final AssignmentState NEW_ASSIGNMENT_STATE = AssignmentState.PENDING;
  public static final String NEW_ASSIGNMENT_START_DATE = "2026-01-15 10:00:00";
  public static final int NEW_ASSIGNMENT_TASK_POSITION = 0;
  public static final int NEW_ASSIGNMENT_WORKER_POSITION = 0;
  public static final int NEW_ASSIGNMENT_EVENT_POSITION = 2;

  public static final String NEW_ASSIGNMENT_START_DATE_BEFORE_ENDING_EVENT_START_DATE =
      "2026-01-09 09:00:00";

  public static final AssignmentState UPDATED_ASSIGNMENT_STATE = AssignmentState.FINISHED;
  public static final String UPDATED_ASSIGNMENT_START_DATE = "2025-11-26 18:00:00";

  // Apartments for import tests
  public static final String CREATED_IMPORT_APARTMENT_NAME_1 = "Studio Old town";
  public static final String CREATED_IMPORT_APARTMENT_AIRBNB_ID_1 =
      "Studio Old town w Balcony only 5 min Danube river";
  public static final String CREATED_IMPORT_APARTMENT_BOOKING_ID_1 =
      "Studio Old town w Balcony only 5 min Danube river";
  public static final int EVENT_COUNT_AIRBNB_APARTMENT_1 = 6;
  public static final int EVENT_COUNT_BOOKING_APARTMENT_1 = 2;

  public static final String CREATED_IMPORT_APARTMENT_NAME_2 = "Atlas studio";
  public static final String CREATED_IMPORT_APARTMENT_AIRBNB_ID_2 =
      "Atlas studio with shared bathroom";
  public static final String CREATED_IMPORT_APARTMENT_BOOKING_ID_2 =
      "Atlas studio with shared bathroom";
  public static final int EVENT_COUNT_AIRBNB_APARTMENT_2 = 2;
  public static final int EVENT_COUNT_BOOKING_APARTMENT_2 = 2;

  public static final String CREATED_IMPORT_APARTMENT_NAME_3 = "Fuji room";
  public static final String CREATED_IMPORT_APARTMENT_AIRBNB_ID_3 =
      "Fuji room with shared bathroom in the old town";
  public static final String CREATED_IMPORT_APARTMENT_BOOKING_ID_3 =
      "Fuji room with shared bathroom in the old town";
  public static final int EVENT_COUNT_AIRBNB_APARTMENT_3 = 1;
  public static final int EVENT_COUNT_BOOKING_APARTMENT_3 = 3;

  public static final int EVENT_COUNT_AIRBNB_AFTER_IMPORT = 9;
  public static final int EVENT_COUNT_BOOKING_AFTER_IMPORT = 7;

  public static final String CREATED_EVENT_CONFLICT_NAME_1 = "Test Event 1";
  public static final String CREATED_EVENT_AIRBNB_CONFLICT_START_DATE_1 = "2025-10-13 15:30:00";
  public static final String CREATED_EVENT_AIRBNB_CONFLICT_END_DATE_1 = "2025-10-16 10:00:00";
  public static final String CREATED_EVENT_BOOKING_CONFLICT_START_DATE_1 = "2025-10-02 15:30:00";
  public static final String CREATED_EVENT_BOOKING_CONFLICT_END_DATE_1 = "2025-10-04 10:00:00";

  public static final EventState CREATED_EVENT_CONFLICT_STATE_1 = EventState.PENDING;
  public static final int CREATED_EVENT_CONFLICT_APARTMENT_POSITION_1 = 0;
  public static final String CONFLICTED_EVENT_AIRBNB_NAME = "Julie Smithson";
  public static final String CONFLICTED_EVENT_BOOKING_NAME = "Tatyana Toichkina";

  public static final AssignmentState CREATED_ASSIGNMENT_CONFLICT_STATE_1 = AssignmentState.PENDING;
  public static final String CREATED_ASSIGNMENT_AIRBNB_CONFLICT_START_DATE_1 =
      "2025-10-17 18:00:00";
  public static final String CREATED_ASSIGNMENT_BOOKING_CONFLICT_START_DATE_1 =
      "2025-10-05 18:00:00";
  public static final int CREATED_ASSIGNMENT_CONFLICT_WORKER_POSITION_1 = 0;
  public static final String CONFLICTED_EVENT_AIRBNB_NAME_2 = "Carlene Howard";
  public static final String CONFLICTED_EVENT_BOOKING_NAME_2 = "Tickner Oscar";

  public static final String CONFLICTED_EVENT_AIRBNB_NAME_3 = "Carolina Damiani";
  public static final String CONFLICTED_EVENT_BOOKING_NAME_3 = "ernesto valverde";

  public static final String CONFLICTED_IMPORT_EVENT_AIRBNB_NAME = "Anna Janning";
  public static final String CONFLICTED_IMPORT_EVENT_BOOKING_NAME = "Lucie Říhová";

  public static final String CREATED_EVENT_CONFLICT_NAME_2 = "Test Event 2";
  public static final String CREATED_EVENT_AIRBNB_CONFLICT_START_DATE_2 = "2025-10-07 15:30:00";
  public static final String CREATED_EVENT_AIRBNB_CONFLICT_END_DATE_2 = "2025-10-08 10:00:00";
  public static final String CONFLICTED_EVENT_ON_CREATION_NAME = "Alexandra Heinen";
}
