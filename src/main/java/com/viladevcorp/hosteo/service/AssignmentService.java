package com.viladevcorp.hosteo.service;

import com.viladevcorp.hosteo.exceptions.*;
import com.viladevcorp.hosteo.model.*;
import com.viladevcorp.hosteo.model.dto.AssignmentUpdateError;
import com.viladevcorp.hosteo.model.forms.AssignmentCreateForm;
import com.viladevcorp.hosteo.model.forms.AssignmentSearchForm;
import com.viladevcorp.hosteo.model.forms.AssignmentUpdateForm;
import com.viladevcorp.hosteo.model.types.AssignmentState;
import com.viladevcorp.hosteo.repository.AssignmentRepository;
import com.viladevcorp.hosteo.repository.EventRepository;
import com.viladevcorp.hosteo.repository.TaskRepository;
import com.viladevcorp.hosteo.utils.AuthUtils;
import com.viladevcorp.hosteo.utils.CodeErrors;
import com.viladevcorp.hosteo.utils.ServiceUtils;

import java.util.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AssignmentService {

  private final AssignmentRepository assignmentRepository;
  private final WorkflowService workflowService;
  private final WorkerService workerService;
  private final EventRepository eventRepository;
  private final TaskRepository taskRepository;
  private final AssignmentProcessor assignmentProcessor;

  @Autowired
  public AssignmentService(
      AssignmentRepository assignmentRepository,
      WorkflowService workflowService,
      WorkerService workerService,
      EventRepository eventRepository,
      TaskRepository taskRepository,
      AssignmentProcessor assignmentProcessor) {
    this.assignmentRepository = assignmentRepository;
    this.workflowService = workflowService;
    this.workerService = workerService;
    this.eventRepository = eventRepository;
    this.taskRepository = taskRepository;
    this.assignmentProcessor = assignmentProcessor;
  }

  public Assignment createAssignment(AssignmentCreateForm form)
      throws EntityNotFoundException,
          DuplicatedEventForTaskException,
          NotAvailableDatesException,
          CompleteTaskOnNotFinishedEventException,
          ChangeInAssignmentsOfPastEventException,
          AssignChangeLastFinishedEventWhenAnotherEventInProgress,
          AssignmentEndsAfterNextEventStarts,
          AssignmentStartsBeforeEventEnds {
    Optional<Task> taskOpt = taskRepository.findById(form.getTaskId(), AuthUtils.getUsername());
    if (taskOpt.isEmpty()) {
      throw new EntityNotFoundException("Task not found with id: " + form.getTaskId());
    }
    Task task = taskOpt.get();

    Optional<Event> eventOpt = eventRepository.findById(form.getEventId(), AuthUtils.getUsername());
    if (eventOpt.isEmpty()) {
      throw new EntityNotFoundException("Event not found with id: " + form.getEventId());
    }
    Event event = eventOpt.get();
    assignmentProcessor.checkWhetherEventAssignmentsCanBeAltered(event);

    Worker worker = workerService.getWorkerById(form.getWorkerId());

    assignmentProcessor.validateAssignment(
        null,
        form.getEventId(),
        form.getStartDate(),
        form.getEndDate(),
        form.getState(),
        task,
        worker);

    Assignment assignment =
        Assignment.builder()
            .task(task)
            .event(event)
            .startDate(form.getStartDate())
            .endDate(form.getEndDate())
            .worker(worker)
            .state(form.getState())
            .build();
    assignment = assignmentRepository.save(assignment);
    event.getAssignments().add(assignment);
    workflowService.calculateApartmentState(task.getApartment().getId());
    return assignment;
  }

  public Assignment updateAssignment(AssignmentUpdateForm form)
      throws EntityNotFoundException,
          DuplicatedEventForTaskException,
          NotAvailableDatesException,
          CompleteTaskOnNotFinishedEventException,
          ChangeInAssignmentsOfPastEventException,
          AssignChangeLastFinishedEventWhenAnotherEventInProgress,
          AssignmentEndsAfterNextEventStarts,
          AssignmentStartsBeforeEventEnds {
    Assignment assignment = assignmentProcessor.getAssignmentById(form.getId());

    Event event = assignment.getEvent();
    assignmentProcessor.checkWhetherEventAssignmentsCanBeAltered(event);

    Worker worker = workerService.getWorkerById(form.getWorkerId());
    Task task = assignment.getTask();
    assignmentProcessor.validateAssignment(
        assignment.getId(),
        event.getId(),
        form.getStartDate(),
        form.getEndDate(),
        form.getState(),
        task,
        worker);
    BeanUtils.copyProperties(form, assignment, "id");
    assignment.setWorker(worker);
    Assignment result = assignmentRepository.save(assignment);
    workflowService.calculateApartmentState(task.getApartment().getId());
    return result;
  }

  public Assignment updateAssignmentState(Assignment assignment, AssignmentState newState)
      throws EntityNotFoundException,
          DuplicatedEventForTaskException,
          NotAvailableDatesException,
          CompleteTaskOnNotFinishedEventException,
          ChangeInAssignmentsOfPastEventException,
          AssignChangeLastFinishedEventWhenAnotherEventInProgress,
          AssignmentEndsAfterNextEventStarts,
          AssignmentStartsBeforeEventEnds {

    return assignmentProcessor.executeUpdateAssignmentState(assignment, newState);
  }

  public List<AssignmentUpdateError> updateBulkAssignmentsState(
      Set<UUID> assignmentIds, AssignmentState newState) {

    List<AssignmentUpdateError> errors = new ArrayList<>();

    // Retrieve the assignments from DB
    List<Assignment> assignments = null;
    assignments =
        assignmentRepository.findInIdsAndCreatedByUsername(assignmentIds, AuthUtils.getUsername());

    // Now process the assignments
    for (Assignment assignment : assignments) {
      try {
        // Call the method that starts a new transaction for each assignment.
        assignmentProcessor.executeUpdateAssignmentState(assignment, newState);
      } catch (EntityNotFoundException e) {
        errors.add(new AssignmentUpdateError(assignment, e.getMessage()));
      } catch (DuplicatedEventForTaskException e) {
        errors.add(new AssignmentUpdateError(assignment, CodeErrors.DUPLICATED_EVENT_FOR_TASK));
      } catch (NotAvailableDatesException e) {
        errors.add(new AssignmentUpdateError(assignment, CodeErrors.NOT_AVAILABLE_DATES));
      } catch (CompleteTaskOnNotFinishedEventException e) {
        errors.add(
            new AssignmentUpdateError(assignment, CodeErrors.COMPLETE_TASK_ON_NOT_FINISHED_EVENT));
      } catch (ChangeInAssignmentsOfPastEventException e) {
        errors.add(
            new AssignmentUpdateError(assignment, CodeErrors.CHANGE_IN_ASSIGNMENTS_OF_PAST_EVENT));
      } catch (AssignChangeLastFinishedEventWhenAnotherEventInProgress e) {
        errors.add(
            new AssignmentUpdateError(
                assignment,
                CodeErrors.ASSIGN_CHANGE_LAST_FINISHED_EVENT_ANOTHER_EVENT_IN_PROGRESS));
      } catch (AssignmentEndsAfterNextEventStarts e) {
        errors.add(
            new AssignmentUpdateError(
                assignment, CodeErrors.ASSIGNMENT_ENDS_AFTER_NEXT_EVENT_STARTS));
      } catch (AssignmentStartsBeforeEventEnds e) {
        errors.add(
            new AssignmentUpdateError(assignment, CodeErrors.ASSIGNMENT_STARTS_BEFORE_EVENT_ENDS));
      } catch (Exception e) {
        // Catch any other exception to prevent the main loop from stopping.
        errors.add(new AssignmentUpdateError(assignment, e.getMessage()));
      }
    }
    return errors;
  }

  public Assignment getAssignmentById(UUID id) throws EntityNotFoundException {
    Optional<Assignment> result = assignmentRepository.findById(id, AuthUtils.getUsername());
    if (result.isEmpty()) {
      throw new EntityNotFoundException("Assignment not found with id: " + id);
    } else {
      return result.get();
    }
  }

  public List<Assignment> findAssignments(AssignmentSearchForm form) {
    String taskName =
        form.getTaskName() == null || form.getTaskName().isEmpty()
            ? null
            : "%" + form.getTaskName().toLowerCase() + "%";
    PageRequest pageRequest =
        ServiceUtils.createPageRequest(form.getPageNumber(), form.getPageSize());
    return assignmentRepository.advancedSearch(
        AuthUtils.getUsername(), taskName, form.getState(), pageRequest);
  }

  public PageMetadata getAssignmentsMetadata(AssignmentSearchForm form) {
    String taskName =
        form.getTaskName() == null || form.getTaskName().isEmpty()
            ? null
            : "%" + form.getTaskName().toLowerCase() + "%";
    int totalRows =
        assignmentRepository.advancedCount(AuthUtils.getUsername(), taskName, form.getState());
    int totalPages = ServiceUtils.calculateTotalPages(form.getPageSize(), totalRows);
    return new PageMetadata(totalPages, totalRows);
  }

  public void deleteAssignment(UUID id)
      throws EntityNotFoundException,
          ChangeInAssignmentsOfPastEventException,
          AssignChangeLastFinishedEventWhenAnotherEventInProgress {
    Assignment assignment = assignmentProcessor.getAssignmentById(id);
    Event event = assignment.getEvent();
    assignmentProcessor.checkWhetherEventAssignmentsCanBeAltered(event);
    Apartment apartment = assignment.getTask().getApartment();
    assignmentRepository.delete(assignment);
    event.getAssignments().remove(assignment);
    workflowService.calculateApartmentState(apartment.getId());
  }
}
