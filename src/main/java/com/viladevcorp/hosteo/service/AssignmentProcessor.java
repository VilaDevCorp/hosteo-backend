package com.viladevcorp.hosteo.service;

import com.viladevcorp.hosteo.exceptions.*;
import com.viladevcorp.hosteo.model.*;
import com.viladevcorp.hosteo.model.dto.AssignmentUpdateError;
import com.viladevcorp.hosteo.model.forms.AssignmentCreateForm;
import com.viladevcorp.hosteo.model.forms.AssignmentSearchForm;
import com.viladevcorp.hosteo.model.forms.AssignmentUpdateForm;
import com.viladevcorp.hosteo.model.types.AssignmentState;
import com.viladevcorp.hosteo.model.types.EventState;
import com.viladevcorp.hosteo.repository.AssignmentRepository;
import com.viladevcorp.hosteo.repository.EventRepository;
import com.viladevcorp.hosteo.repository.TaskRepository;
import com.viladevcorp.hosteo.utils.AuthUtils;
import com.viladevcorp.hosteo.utils.CodeErrors;
import com.viladevcorp.hosteo.utils.ServiceUtils;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AssignmentProcessor {

  private final AssignmentRepository assignmentRepository;
  private final WorkflowService workflowService;
  private final EventRepository eventRepository;

  @Autowired
  public AssignmentProcessor(
      AssignmentRepository assignmentRepository,
      WorkflowService workflowService,
      EventRepository eventRepository) {
    this.assignmentRepository = assignmentRepository;
    this.workflowService = workflowService;
    this.eventRepository = eventRepository;
  }

  public void validateAssignment(
      UUID assignmentId,
      UUID eventId,
      Instant startDate,
      Instant endDate,
      AssignmentState assignmentState,
      Task task,
      Worker worker)
      throws DuplicatedEventForTaskException,
          NotAvailableDatesException,
          CompleteTaskOnNotFinishedEventException,
          EntityNotFoundException,
          AssignmentStartsBeforeEventEnds,
          AssignmentEndsAfterNextEventStarts {

    UUID apartmentId = task.getApartment().getId();
    // Validate that apartment is available in the selected dates (not events nor
    // assignments)
    Pair<Event, Assignment> conflicts =
        ServiceUtils.getScheduleConflicts(
            eventRepository,
            assignmentRepository,
            apartmentId,
            startDate,
            endDate,
            null,
            assignmentId);
    if (conflicts.a != null || conflicts.b != null) {
      log.error(
          "[{}] - Apartment with id: {} is not available between {} and {}",
          "EventService.validateAssignment",
          apartmentId,
          startDate,
          endDate);
      throw new NotAvailableDatesException("Apartment is not available in the selected dates.");
    }
    // Validate that worker is available in the selected dates
    if (assignmentRepository.checkWorkerAvailability(
        AuthUtils.getUsername(), worker.getId(), startDate, endDate, assignmentId)) {
      log.error(
          "[AssignmentService.validateAssignment] - Worker {} is not available between {} and {}",
          worker.getId(),
          startDate,
          endDate);
      throw new NotAvailableDatesException("Worker is not available in the selected dates.");
    }

    // Get the event related to the assignment
    Optional<Event> eventOpt =
        eventRepository.findEventByIdWithAssignments(eventId, AuthUtils.getUsername());

    if (eventOpt.isEmpty()) {
      throw new EntityNotFoundException("Event not found with id: " + eventId);
    }

    Event event = eventOpt.get();

    // Validate that the event does not already have an assignment for the same task
    for (Assignment a : event.getAssignments()) {
      // If the assignment is not the same, and the task is the same, throw an exception
      // (duplicated assignment for task)
      if (!a.getId().equals(assignmentId) && a.getTask().getId().equals(task.getId())) {
        log.error(
            "[AssignmentService.validateAssignment] - Event ID {} already has an assignment for task ID {}",
            eventId,
            task.getId());
        throw new DuplicatedEventForTaskException(
            "This event already has an assignment for the specified task.");
      }
    }

    // Validate that the assignment startDate is not before the event endDate
    if (startDate.isBefore(event.getEndDate())) {
      log.error(
          "[AssignmentService.validateAssignment] - The assignment starts before the event ends. "
              + "Event ID {} ends at {} and assignment starts at {}",
          event.getId(),
          event.getEndDate(),
          startDate);
      throw new AssignmentStartsBeforeEventEnds(
          "An assignment cannot start before the event ends.");
    }

    // We get the next event of the apartment
    Optional<Event> nextEventOpt =
        eventRepository.findFirstEventAfterDateWithState(
            AuthUtils.getAuthUser().getId(), apartmentId, event.getStartDate(), null);
    // Validate that the assignment endDate is not after the next event startDate
    if (nextEventOpt.isPresent()) {
      if (endDate.isAfter(nextEventOpt.get().getStartDate()))
        log.error(
            "[AssignmentService.validateAssignment] - The assignment ends after the next event starts. "
                + "Event ID {} ends at {} and assignment starts at {}",
            event.getId(),
            event.getEndDate(),
            startDate);
      throw new AssignmentEndsAfterNextEventStarts(
          "An assignment cannot start before the next event starts");
    }

    // Validate the event is finished to complete the task
    if (event.getState() != EventState.FINISHED && assignmentState == AssignmentState.FINISHED) {
      log.error(
          "[AssignmentService.validateAssignment] - Event ID {} is not finished. Current state: {}",
          event.getId(),
          event.getState());
      throw new CompleteTaskOnNotFinishedEventException(
          "Cannot complete tasks to events that are not finished.");
    }
  }

  // Check whether the assignment can be created, updated or deleted based on the event state and
  // time
  public void checkWhetherEventAssignmentsCanBeAltered(Event event)
      throws AssignChangeLastFinishedEventWhenAnotherEventInProgress,
          ChangeInAssignmentsOfPastEventException,
          EntityNotFoundException {

    UUID apartmentId = event.getApartment().getId();
    // If the event is not finished, we can modify the assignments
    if (!event.getState().isFinished()) {
      return;
    }
    // We get the last finished event for the apartment
    Optional<Event> lastFinishedEventOpt =
        eventRepository.findFirstByCreatedByUsernameAndApartmentIdAndStateOrderByEndDateDesc(
            AuthUtils.getUsername(), apartmentId, EventState.FINISHED.toString());
    // If there is no last finished event, we can modify the assignments (the event to modify has to
    // be pending or in progress)
    if (lastFinishedEventOpt.isEmpty()) {
      return;
    }

    // If the event to modify is not the last finished one, we cannot modify the assignments
    if (!event.getId().equals(lastFinishedEventOpt.get().getId())) {
      log.error(
          "[AssignmentService.checkIfEventAssignmentsCanBeModified] - Cannot modify assignments for past events as "
              + "there are subsequent events and would affect the workflow");
      throw new ChangeInAssignmentsOfPastEventException(
          "Cannot modify assignments for a finished event with subsequent events.");
    }
    // If it is the last finished one, we have to check if the next one is in progress (if it is, we
    // cannot modify the last finished one either, as will corrupt the workflow, cause we cannot
    // reopen an event clean state if the next booking is already going on)

    // We get the event after the last finished one in IN PROGRESS state
    Optional<Event> eventInProgressAfterLastFinishedOpt =
        eventRepository.findFirstEventAfterDateWithState(
            AuthUtils.getAuthUser().getId(),
            apartmentId,
            event.getStartDate(),
            EventState.IN_PROGRESS.toString());
    // If no next event in progress after the last finished one, we are good to go
    if (eventInProgressAfterLastFinishedOpt.isPresent()) {
      log.error(
          "[AssignmentService.checkIfEventAssignmentsCanBeModified] - Cannot modify assignments for last finished event ID {} as new events have started after it.",
          lastFinishedEventOpt.get().getId());
      throw new AssignChangeLastFinishedEventWhenAnotherEventInProgress(
          "Cannot modify assignments for a finished event with subsequent events in progress.");
    }
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
  public Assignment executeUpdateAssignmentState(Assignment assignment, AssignmentState newState)
      throws EntityNotFoundException,
          DuplicatedEventForTaskException,
          NotAvailableDatesException,
          CompleteTaskOnNotFinishedEventException,
          ChangeInAssignmentsOfPastEventException,
          AssignChangeLastFinishedEventWhenAnotherEventInProgress,
          AssignmentEndsAfterNextEventStarts,
          AssignmentStartsBeforeEventEnds {

    Event event = assignment.getEvent();
    checkWhetherEventAssignmentsCanBeAltered(event);
    assignment.setState(newState);

    Assignment result = assignmentRepository.save(assignment);
    workflowService.calculateApartmentState(assignment.getTask().getApartment().getId());
    try {
      validateAssignment(
          assignment.getId(),
          event.getId(),
          assignment.getStartDate(),
          assignment.getEndDate(),
          newState,
          assignment.getTask(),
          assignment.getWorker());
    } catch (Exception e) {
      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
      throw e;
    }
    return result;
  }

  public Assignment getAssignmentById(UUID id) throws EntityNotFoundException {
    Optional<Assignment> result = assignmentRepository.findById(id, AuthUtils.getUsername());
    if (result.isEmpty()) {
      throw new EntityNotFoundException("Assignment not found with id: " + id);
    } else {
      return result.get();
    }
  }
}
