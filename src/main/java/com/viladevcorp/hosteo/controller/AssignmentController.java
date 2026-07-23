package com.viladevcorp.hosteo.controller;

import com.viladevcorp.hosteo.exceptions.*;
import com.viladevcorp.hosteo.model.Assignment;
import com.viladevcorp.hosteo.model.Page;
import com.viladevcorp.hosteo.model.PageMetadata;
import com.viladevcorp.hosteo.model.dto.AssignmentDto;
import com.viladevcorp.hosteo.model.dto.AssignmentUpdateError;
import com.viladevcorp.hosteo.model.forms.AssignmentCreateForm;
import com.viladevcorp.hosteo.model.forms.AssignmentSearchForm;
import com.viladevcorp.hosteo.model.forms.AssignmentUpdateForm;
import com.viladevcorp.hosteo.model.types.AssignmentState;
import com.viladevcorp.hosteo.service.AssignmentService;
import com.viladevcorp.hosteo.utils.ApiResponse;
import com.viladevcorp.hosteo.utils.CodeErrors;
import com.viladevcorp.hosteo.utils.ValidationUtils;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import javax.management.InstanceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PutMapping;

@Slf4j
@RestController
@RequestMapping("/api")
public class AssignmentController {

  private final AssignmentService assignmentService;

  @Autowired
  public AssignmentController(AssignmentService assignmentService) {
    this.assignmentService = assignmentService;
  }

  private ResponseEntity<ApiResponse<AssignmentDto>> handleAssignmentOperation(
      Callable<Assignment> operation) {
    try {
      Assignment assignment = operation.call();
      log.info("[AssignmentController] - Operation successful, returning assignment.");
      return ResponseEntity.ok().body(new ApiResponse<>(new AssignmentDto(assignment)));
    } catch (InstanceNotFoundException e) {
      log.error("[AssignmentController] - InstanceNotFoundException: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ApiResponse<>(null, e.getMessage()));
    } catch (DuplicatedEventForTaskException e) {
      log.error("[AssignmentController] - DuplicatedEventForTaskException: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.DUPLICATED_EVENT_FOR_TASK, e.getMessage()));
    } catch (NotAvailableDatesException e) {
      log.error("[AssignmentController] - NotAvailableDatesException: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.NOT_AVAILABLE_DATES, e.getMessage()));
    } catch (CompleteTaskOnNotFinishedEventException e) {
      log.error(
          "[AssignmentController] - CompleteTaskOnNotFinishedEventException: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.COMPLETE_TASK_ON_NOT_FINISHED_EVENT, e.getMessage()));
    } catch (ChangeInAssignmentsOfPastEventException e) {
      log.error(
          "[AssignmentController] - ChangeInAssignmentsOfPastEventException: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.CHANGE_IN_ASSIGNMENTS_OF_PAST_EVENT, e.getMessage()));
    } catch (AssignChangeLastFinishedEventWhenAnotherEventInProgress e) {
      log.error(
          "[AssignmentController] - AssignChangeLastFinishedEventWhenAnotherEventInProgress: {}",
          e.getMessage());
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(
                  CodeErrors.ASSIGN_CHANGE_LAST_FINISHED_EVENT_ANOTHER_EVENT_IN_PROGRESS,
                  e.getMessage()));
    } catch (AssignmentEndsAfterNextEventStarts e) {
      log.error("[AssignmentController] - AssignmentEndsAfterNextEventStarts: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(
                  CodeErrors.ASSIGNMENT_ENDS_AFTER_NEXT_EVENT_STARTS, e.getMessage()));
    } catch (AssignmentStartsBeforeEventEnds e) {
      log.error("[AssignmentController] - AssignmentStartsBeforeEventEnds: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.ASSIGNMENT_STARTS_BEFORE_EVENT_ENDS, e.getMessage()));
    } catch (Exception e) {
      log.error("[AssignmentController] - An unexpected error occurred: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ApiResponse<>(null, "An unexpected error occurred."));
    }
  }

  @PostMapping("/assignment")
  public ResponseEntity<ApiResponse<AssignmentDto>> createAssignment(
      @Valid @RequestBody AssignmentCreateForm form, BindingResult bindingResult) {
    log.info("[AssignmentController.createAssignment] - Creating assignment");

    ResponseEntity<ApiResponse<AssignmentDto>> validationResponse =
        ValidationUtils.handleFormValidation(bindingResult);
    if (validationResponse != null) {
      return validationResponse;
    }

    return handleAssignmentOperation(() -> assignmentService.createAssignment(form));
  }

  @PutMapping("/assignment")
  public ResponseEntity<ApiResponse<AssignmentDto>> updateAssignment(
      @Valid @RequestBody AssignmentUpdateForm form, BindingResult bindingResult) {
    log.info("[AssignmentController.updateAssignment] - Updating assignment");

    ResponseEntity<ApiResponse<AssignmentDto>> validationResponse =
        ValidationUtils.handleFormValidation(bindingResult);
    if (validationResponse != null) {
      return validationResponse;
    }

    return handleAssignmentOperation(() -> assignmentService.updateAssignment(form));
  }

  @PatchMapping("/assignment/{id}/state/{state}")
  public ResponseEntity<ApiResponse<AssignmentDto>> updateAssignmentState(
      @PathVariable UUID id, @PathVariable AssignmentState state) {

    log.info("[AssignmentController.updateAssignmentState] - Updating assignment");

    return handleAssignmentOperation(
        () -> {
          Assignment assignment = assignmentService.getAssignmentById(id);
          return assignmentService.updateAssignmentState(assignment, state);
        });
  }

  @PatchMapping("/assignments/state/{state}")
  public ResponseEntity<ApiResponse<List<AssignmentUpdateError>>> updateBulkAssignmentState(
      @RequestBody Set<UUID> assignmentIds, @PathVariable AssignmentState state) {
    log.info("[AssignmentController.updateBulkAssignmentState] - Updating assignments");
    List<AssignmentUpdateError> result =
        assignmentService.updateBulkAssignmentsState(assignmentIds, state);
    log.info("[AssignmentController.updateBulkAssignmentState] - Assignments updated successfully");
    return ResponseEntity.ok().body(new ApiResponse<>(result));
  }

  @GetMapping("/assignment/{id}")
  public ResponseEntity<ApiResponse<AssignmentDto>> getAssignment(@PathVariable UUID id) {
    log.info("[AssignmentController.getAssignment] - Fetching assignment with id: {}", id);

    return handleAssignmentOperation(() -> assignmentService.getAssignmentById(id));
  }

  @PostMapping("/assignment/search")
  public ResponseEntity<ApiResponse<Page<AssignmentDto>>> searchAssignments(
      @RequestBody AssignmentSearchForm form) {
    log.info("[AssignmentController.searchAssignments] - Searching assignments");

    List<Assignment> assignments = assignmentService.findAssignments(form);
    PageMetadata pageMetadata = assignmentService.getAssignmentsMetadata(form);
    Page<AssignmentDto> page =
        new Page<>(
            assignments.stream().map(AssignmentDto::new).toList(),
            pageMetadata.getTotalPages(),
            pageMetadata.getTotalRows());

    log.info("[AssignmentController.searchAssignments] - Found {} assignments", assignments.size());
    return ResponseEntity.ok().body(new ApiResponse<>(page));
  }

  @DeleteMapping("/assignment/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteAssignment(@PathVariable UUID id) {
    log.info("[AssignmentController.deleteAssignment] - Deleting assignment with id: {}", id);
    return handleAssignmentDeletion(
        () -> {
          assignmentService.deleteAssignment(id);
          return null;
        });
  }

  private ResponseEntity<ApiResponse<Void>> handleAssignmentDeletion(Callable<Void> operation) {
    try {
      operation.call();
      log.info("[AssignmentController] - Deletion successful.");
      return ResponseEntity.ok().body(new ApiResponse<>(null, "Assignment deleted successfully."));
    } catch (InstanceNotFoundException e) {
      log.error("[AssignmentController] - InstanceNotFoundException: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ApiResponse<>(null, e.getMessage()));
    } catch (ChangeInAssignmentsOfPastEventException e) {
      log.error(
          "[AssignmentController] - ChangeInAssignmentsOfPastEventException: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.CHANGE_IN_ASSIGNMENTS_OF_PAST_EVENT, e.getMessage()));
    } catch (AssignChangeLastFinishedEventWhenAnotherEventInProgress e) {
      log.error(
          "[AssignmentController] - AssignChangeLastFinishedEventWhenAnotherEventInProgress: {}",
          e.getMessage());
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(
                  CodeErrors.ASSIGN_CHANGE_LAST_FINISHED_EVENT_ANOTHER_EVENT_IN_PROGRESS,
                  e.getMessage()));
    } catch (Exception e) {
      log.error(
          "[AssignmentController] - An unexpected error occurred during deletion: {}",
          e.getMessage(),
          e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ApiResponse<>(null, "An unexpected error occurred."));
    }
  }
}
