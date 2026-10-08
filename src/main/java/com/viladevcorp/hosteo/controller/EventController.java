package com.viladevcorp.hosteo.controller;

import com.viladevcorp.hosteo.exceptions.*;
import com.viladevcorp.hosteo.model.Event;
import com.viladevcorp.hosteo.model.Page;
import com.viladevcorp.hosteo.model.PageMetadata;
import com.viladevcorp.hosteo.model.dto.*;
import com.viladevcorp.hosteo.model.forms.EventCreateForm;
import com.viladevcorp.hosteo.model.forms.EventSearchForm;
import com.viladevcorp.hosteo.model.forms.EventUpdateForm;
import com.viladevcorp.hosteo.model.types.EventState;
import com.viladevcorp.hosteo.service.EventService;
import com.viladevcorp.hosteo.utils.ApiResponse;
import com.viladevcorp.hosteo.utils.CodeErrors;
import com.viladevcorp.hosteo.utils.ValidationUtils;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import jakarta.persistence.EntityNotFoundException;
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
public class EventController {

  private final EventService eventService;

  @Autowired
  public EventController(EventService eventService) {
    this.eventService = eventService;
  }

  @PostMapping("/event")
  public ResponseEntity<ApiResponse<EventDto>> createEvent(
      @Valid @RequestBody EventCreateForm form, BindingResult bindingResult) {
    log.info("[EventController.createEvent] - Creating event");

    ResponseEntity<ApiResponse<EventDto>> validationResponse =
        ValidationUtils.handleFormValidation(bindingResult);
    if (validationResponse != null) {
      return validationResponse;
    }

    try {
      Event event = eventService.createEvent(form);
      log.info("[EventController.createEvent] - Event created successfully");
      return ResponseEntity.ok().body(new ApiResponse<>(new EventDto(event)));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ApiResponse<>(null, "Apartment not found"));
    } catch (NotAvailableDatesException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.NOT_AVAILABLE_DATES, e.getMessage()));
    } catch (EntityFrozenException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.ENTITY_FROZEN, e.getMessage()));
    } catch (NextOfPendingCannotBeInprogressOrFinished e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(
                  CodeErrors.NEXT_OF_PENDING_CANNOT_BE_INPROGRESS_OR_FINISHED, e.getMessage()));
    } catch (PrevOfInProgressCannotBePendingOrInProgress e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(
                  CodeErrors.PREV_OF_INPROGRESS_CANNOT_BE_PENDING_OR_INPROGRESS, e.getMessage()));
    } catch (NextOfInProgressCannotBeFinishedOrInProgress e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(
                  CodeErrors.NEXT_OF_INPROGRESS_CANNOT_BE_FINISHED_OR_INPROGRESS, e.getMessage()));
    } catch (PrevOfFinishedCannotBeNotPendingOrInProgress e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(
                  CodeErrors.PREV_OF_FINISHED_CANNOT_BE_PENDING_OR_INPROGRESS, e.getMessage()));
    }
  }

  @PutMapping("/event")
  public ResponseEntity<ApiResponse<EventDto>> updateEvent(
      @Valid @RequestBody EventUpdateForm form, BindingResult bindingResult) {
    log.info("[EventController.updateEvent] - Updating event");

    ResponseEntity<ApiResponse<EventDto>> validationResponse =
        ValidationUtils.handleFormValidation(bindingResult);
    if (validationResponse != null) {
      return validationResponse;
    }

    try {
      Event event = eventService.updateEvent(form);
      log.info("[EventController.updateEvent] - Event updated successfully");
      return ResponseEntity.ok().body(new ApiResponse<>(new EventDto(event)));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ApiResponse<>(null, e.getMessage()));
    } catch (NotAvailableDatesException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.NOT_AVAILABLE_DATES, e.getMessage()));
    } catch (EntityFrozenException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.ENTITY_FROZEN, e.getMessage()));
    } catch (NextOfPendingCannotBeInprogressOrFinished e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(
                  CodeErrors.NEXT_OF_PENDING_CANNOT_BE_INPROGRESS_OR_FINISHED, e.getMessage()));
    } catch (PrevOfInProgressCannotBePendingOrInProgress e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(
                  CodeErrors.PREV_OF_INPROGRESS_CANNOT_BE_PENDING_OR_INPROGRESS, e.getMessage()));
    } catch (NextOfInProgressCannotBeFinishedOrInProgress e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(
                  CodeErrors.NEXT_OF_INPROGRESS_CANNOT_BE_FINISHED_OR_INPROGRESS, e.getMessage()));
    } catch (PrevOfFinishedCannotBeNotPendingOrInProgress e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(
                  CodeErrors.PREV_OF_FINISHED_CANNOT_BE_PENDING_OR_INPROGRESS, e.getMessage()));
    }
  }

  @PatchMapping("/event/{id}/state/{state}")
  public ResponseEntity<ApiResponse<EventDto>> updateEventState(
      @PathVariable UUID id, @PathVariable EventState state) {
    log.info("[EventController.updateEventState] - Updating event state with id: {}", id);
    try {
      Event event = eventService.updateEventState(id, state);
      log.info("[EventController.updateEventState] - Event state updated successfully");
      return ResponseEntity.ok().body(new ApiResponse<>(new EventDto(event)));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ApiResponse<>(null, e.getMessage()));
    } catch (EntityFrozenException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.ENTITY_FROZEN, e.getMessage()));
    } catch (NextOfPendingCannotBeInprogressOrFinished e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(
                  CodeErrors.NEXT_OF_PENDING_CANNOT_BE_INPROGRESS_OR_FINISHED, e.getMessage()));
    } catch (PrevOfInProgressCannotBePendingOrInProgress e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(
                  CodeErrors.PREV_OF_INPROGRESS_CANNOT_BE_PENDING_OR_INPROGRESS, e.getMessage()));
    } catch (NextOfInProgressCannotBeFinishedOrInProgress e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(
                  CodeErrors.NEXT_OF_INPROGRESS_CANNOT_BE_FINISHED_OR_INPROGRESS, e.getMessage()));
    } catch (PrevOfFinishedCannotBeNotPendingOrInProgress e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(
              new ApiResponse<>(
                  CodeErrors.PREV_OF_FINISHED_CANNOT_BE_PENDING_OR_INPROGRESS, e.getMessage()));
    }
  }

  @PatchMapping("/events/state/{state}")
  public ResponseEntity<ApiResponse<List<EventOperationError>>> updateEventsState(
      @RequestBody Set<UUID> eventIds, @PathVariable EventState state) {
    log.info("[EventController.updateEventsState] - Updating events state with ids: {}", eventIds);
    List<EventOperationError> updateBulkErrors = eventService.updateBulkEventState(eventIds, state);
    log.info("[EventController.updateEventsState] - Events state updated  successfully");
    return ResponseEntity.ok().body(new ApiResponse<>(updateBulkErrors));
  }

  @DeleteMapping("/events")
  public ResponseEntity<ApiResponse<List<EventOperationError>>> deleteBulkEvents(
      @RequestBody Set<UUID> eventIds) {
    log.info("[EventController.deleteBulkEvents] - Deleting events with ids: {}", eventIds);
    List<EventOperationError> deleteBulkErrors = eventService.deleteBulkEvents(eventIds);
    log.info("[EventController.deleteBulkEvents] - Events deleted successfully");
    return ResponseEntity.ok().body(new ApiResponse<>(deleteBulkErrors));
  }

  @GetMapping("/event/{id}")
  public ResponseEntity<ApiResponse<EventWithAssignmentsAndNextEventDto>> getEvent(
      @PathVariable UUID id) {
    log.info("[EventController.getEvent] - Fetching event with id: {}", id);
    try {
      EventWithAssignmentsAndNextEventDto event =
          eventService.getEventByIdWithAssigmentsAndNextEvent(id);
      log.info("[EventController.getEvent] - Event found successfully");
      return ResponseEntity.ok().body(new ApiResponse<>(event));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ApiResponse<>(null, e.getMessage()));
    }
  }

  @PostMapping("/event/search")
  public ResponseEntity<ApiResponse<Page<EventDto>>> searchEvents(
      @RequestBody EventSearchForm form) {
    log.info("[EventController.searchEvents] - Searching events");

    List<Event> events = eventService.findEvents(form);
    PageMetadata pageMetadata = eventService.getEventsMetadata(form);
    Page<EventDto> page =
        new Page<>(
            events.stream().map(EventDto::new).toList(),
            pageMetadata.getTotalPages(),
            pageMetadata.getTotalRows());

    log.info("[EventController.searchEvents] - Found {} events", events.size());
    return ResponseEntity.ok().body(new ApiResponse<>(page));
  }

  @DeleteMapping("/event/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable UUID id) {
    log.info("[EventController.deleteEvent] - Deleting event with id: {}", id);
    try {
      eventService.deleteEvent(id);
      log.info("[EventController.deleteEvent] - Event deleted successfully");
      return ResponseEntity.ok().body(new ApiResponse<>(null, "Event deleted successfully."));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ApiResponse<>(null, e.getMessage()));
    } catch (EntityFrozenException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.ENTITY_FROZEN, e.getMessage()));
    }
  }
}