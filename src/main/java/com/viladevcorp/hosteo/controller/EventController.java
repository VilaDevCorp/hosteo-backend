package com.viladevcorp.hosteo.controller;

import com.viladevcorp.hosteo.exceptions.*;
import com.viladevcorp.hosteo.model.Event;
import com.viladevcorp.hosteo.model.ImpBooking;
import com.viladevcorp.hosteo.model.Page;
import com.viladevcorp.hosteo.model.PageMetadata;
import com.viladevcorp.hosteo.model.dto.*;
import com.viladevcorp.hosteo.model.forms.EventCreateForm;
import com.viladevcorp.hosteo.model.forms.EventSearchForm;
import com.viladevcorp.hosteo.model.forms.EventUpdateForm;
import com.viladevcorp.hosteo.model.types.EventState;
import com.viladevcorp.hosteo.service.EventService;
import com.viladevcorp.hosteo.service.ImportService;
import com.viladevcorp.hosteo.utils.ApiResponse;
import com.viladevcorp.hosteo.utils.CodeErrors;
import com.viladevcorp.hosteo.utils.ValidationUtils;
import jakarta.validation.Valid;
import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api")
public class EventController {

  private final EventService eventService;

  private final ImportService importService;

  @Autowired
  public EventController(EventService eventService, ImportService importService) {
    this.eventService = eventService;
    this.importService = importService;
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
  public ResponseEntity<ApiResponse<List<EventUpdateError>>> updateEventsState(
      @RequestBody Set<UUID> eventIds, @PathVariable EventState state) {
    log.info("[EventController.updateEventsState] - Updating events state with ids: {}", eventIds);
    List<EventUpdateError> updateBulkErrors = eventService.updateBulkEventState(eventIds, state);
    log.info("[EventController.updateEventsState] - Events state updated  successfully");
    return ResponseEntity.ok().body(new ApiResponse<>(updateBulkErrors));
  }

  @GetMapping("/event/{id}")
  public ResponseEntity<ApiResponse<EventWithAssignmentsDto>> getEvent(@PathVariable UUID id) {
    log.info("[EventController.getEvent] - Fetching event with id: {}", id);
    try {
      EventWithAssignmentsDto event = eventService.getEventByIdWithAssigments(id);
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
    }
  }

  @GetMapping("/booking/import/exists")
  public ResponseEntity<Void> checkExistentImports() {
    log.info("[EventController.checkExistentImports] - Checking existent imports");
    if (importService.existsImportInProgress()) {
      log.info("[EventController.checkExistentImports] - Import in progress found");
      return ResponseEntity.ok().build();
    } else {
      log.info("[EventController.checkExistentImports] - No import in progress found");
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  @GetMapping("/booking/import")
  public ResponseEntity<ApiResponse<Page<ImpBookingDto>>> getImportedEvents(
      @RequestParam(defaultValue = "0") int pageNumber) {
    log.info("[EventController.getImportedEvents] - Searching import events");
    List<ImpBooking> events = importService.searchUserImpBookings(pageNumber);
    PageMetadata pageMetadata = importService.getImpBookingsMetadata();
    Page<ImpBookingDto> page =
        new Page<>(
            events.stream().map(ImpBookingDto::new).toList(),
            pageMetadata.getTotalPages(),
            pageMetadata.getTotalRows());

    log.info("[EventController.getImportedEvents] - Found {} imported events", events.size());
    return ResponseEntity.ok().body(new ApiResponse<>(page));
  }

  @PostMapping(value = "booking/import/airbnb", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<List<ImpBookingDto>>> importAirbnbEvents(
      @RequestParam("file") MultipartFile multipartFile) {
    log.info("[EventController.importAirbnbEvents] - Importing Airbnb events");
    List<ImpBooking> importedEvents;
    File tempFile = null;
    try {
      tempFile = File.createTempFile("uploaded", ".csv");
      multipartFile.transferTo(tempFile);
      importedEvents = importService.importAirbnbBookings(tempFile);
      log.info(
          "[EventController.importAirbnbEvents] - Imported {} Airbnb events",
          importedEvents.size());
    } catch (Exception e) {
      log.error(
          "[EventController.importAirbnbEvents] - Error importing Airbnb events: {}",
          e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ApiResponse<>(null, "Error importing Airbnb events: " + e.getMessage()));
    } finally {
      if (tempFile != null && tempFile.exists()) {
        tempFile.delete();
      }
    }
    return ResponseEntity.ok()
        .body(new ApiResponse<>(importedEvents.stream().map(ImpBookingDto::new).toList()));
  }

  @PostMapping(value = "booking/import/booking", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<List<ImpBookingDto>>> importEventEvents(
      @RequestParam("file") MultipartFile multipartFile) {
    log.info("[EventController.importEventEvents] - Importing Event events");
    List<ImpBooking> importedEvents;
    File tempFile = null;
    try {
      tempFile = File.createTempFile("uploaded", ".csv");
      tempFile.deleteOnExit();
      multipartFile.transferTo(tempFile);
      importedEvents = importService.importBookingBookings(tempFile);
      log.info(
          "[EventController.importEventEvents] - Imported {} Event events", importedEvents.size());
    } catch (Exception e) {
      log.error(
          "[EventController.importEventEvents] - Error importing Event events: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ApiResponse<>(null, "Error importing Event events: " + e.getMessage()));
    } finally {
      if (tempFile != null && tempFile.exists()) {
        tempFile.delete();
      }
    }
    return ResponseEntity.ok()
        .body(new ApiResponse<>(importedEvents.stream().map(ImpBookingDto::new).toList()));
  }

  @PostMapping(value = "booking/import/execute")
  public ResponseEntity<ApiResponse<ImportResultDto>> executeImport() {
    log.info("[EventController.executeImport] - Executing event import");
    try {
      if (!importService.existsImportInProgress()) {
        log.info("[EventController.executeImport] - No import in progress found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ApiResponse<>(null, "No import in progress found"));
      }
      ImportResultDto result = importService.executeImportBookings();
      log.info("[EventController.executeImport] - Event import executed successfully");
      return ResponseEntity.ok().body(new ApiResponse<>(result));
    } catch (Exception e) {
      log.error(
          "[EventController.executeImport] - Error executing event import: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ApiResponse<>(null, "Error executing event import: " + e.getMessage()));
    }
  }

  @DeleteMapping("/booking/import")
  public ResponseEntity<Void> deleteUserImportData() {
    log.info("[EventController.deleteUserImportData] - Deleting user import data");
    importService.deleteUserImpBookings();
    log.info("[EventController.deleteUserImportData] - User import data deleted successfully");
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/booking/import/{id}")
  public ResponseEntity<Void> deleteImportedEvent(@PathVariable UUID id) {
    log.info("[EventController.deleteImportedEvent] - Deleting imported event with id: {}", id);
    importService.deleteImpBookingById(id);
    log.info("[EventController.deleteImportedEvent] - Imported event deleted successfully");
    return ResponseEntity.ok().build();
  }
}
