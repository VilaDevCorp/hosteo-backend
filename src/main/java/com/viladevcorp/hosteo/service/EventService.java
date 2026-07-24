package com.viladevcorp.hosteo.service;

import com.viladevcorp.hosteo.exceptions.*;
import com.viladevcorp.hosteo.model.*;
import com.viladevcorp.hosteo.model.dto.EventUpdateError;
import com.viladevcorp.hosteo.model.dto.EventWithAssignmentsDto;
import com.viladevcorp.hosteo.model.forms.EventCreateForm;
import com.viladevcorp.hosteo.model.forms.EventSearchForm;
import com.viladevcorp.hosteo.model.forms.EventUpdateForm;
import com.viladevcorp.hosteo.model.types.EventState;
import com.viladevcorp.hosteo.repository.AssignmentRepository;
import com.viladevcorp.hosteo.repository.EventRepository;
import com.viladevcorp.hosteo.utils.AuthUtils;
import com.viladevcorp.hosteo.utils.CodeErrors;
import com.viladevcorp.hosteo.utils.ServiceUtils;
import java.time.Instant;
import java.util.*;
import jakarta.persistence.EntityNotFoundException;

import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class EventService {

  private final EventRepository eventRepository;
  private final AssignmentRepository assignmentRepository;
  private final WorkflowService workflowService;
  private final EventProcessor eventProcessor;

  @Autowired
  public EventService(
      EventRepository eventRepository,
      WorkflowService workflowService,
      AssignmentRepository assignmentRepository,
      EventProcessor eventProcessor) {
    this.eventRepository = eventRepository;
    this.workflowService = workflowService;
    this.assignmentRepository = assignmentRepository;
    this.eventProcessor = eventProcessor;
  }

  public Event createEvent(EventCreateForm form)
      throws EntityNotFoundException,
          NotAvailableDatesException,
          PrevOfInProgressCannotBePendingOrInProgress,
          PrevOfFinishedCannotBeNotPendingOrInProgress,
          NextOfPendingCannotBeInprogressOrFinished,
          NextOfInProgressCannotBeFinishedOrInProgress {
    return eventProcessor.executeCreateEventLogic(form);
  }

  public Event updateEvent(EventUpdateForm form)
      throws EntityNotFoundException,
          NotAvailableDatesException,
          PrevOfInProgressCannotBePendingOrInProgress,
          PrevOfFinishedCannotBeNotPendingOrInProgress,
          NextOfPendingCannotBeInprogressOrFinished,
          NextOfInProgressCannotBeFinishedOrInProgress {
    Event event = getEventById(form.getId());
    UUID apartmentId = event.getApartment().getId();

    Pair<Event, Assignment> conflicts =
        ServiceUtils.getScheduleConflicts(
            eventRepository,
            assignmentRepository,
            apartmentId,
            form.getStartDate(),
            form.getEndDate(),
            form.getId(),
            null);

    if (conflicts.a != null || conflicts.b != null) {
      log.error(
          "[{}] - Apartment with id: {} is not available between {} and {}",
          "EventService.updateEvent",
          apartmentId,
          form.getStartDate(),
          form.getEndDate());
      throw new NotAvailableDatesException("Apartment is not available in the selected dates.");
    }

    BeanUtils.copyProperties(form, event, "id");

    Event result = eventRepository.save(event);
    workflowService.calculateApartmentState(result.getApartment().getId());
    eventProcessor.validateEventState(apartmentId, form.getState(), form.getStartDate());

    return result;
  }

  public Event updateEventState(UUID eventId, EventState state)
      throws EntityNotFoundException,
          PrevOfInProgressCannotBePendingOrInProgress,
          PrevOfFinishedCannotBeNotPendingOrInProgress,
          NextOfPendingCannotBeInprogressOrFinished,
          NextOfInProgressCannotBeFinishedOrInProgress {
    return eventProcessor.executeUpdateStateLogic(eventId, state);
  }

  public List<EventUpdateError> updateBulkEventState(Set<UUID> eventIds, EventState state) {

    List<EventUpdateError> errors = new ArrayList<>();

    // Retrieve the events from DB
    List<Event> events =
        eventRepository.findInIdsAndCreatedByUsername(eventIds, AuthUtils.getUsername());

    // Now process the events
    for (Event event : events) {
      try {
        // Call the method that starts a new transaction for each event.
        eventProcessor.executeUpdateStateLogic(event.getId(), state);
      } catch (NextOfInProgressCannotBeFinishedOrInProgress e) {
        errors.add(
            new EventUpdateError(
                event, CodeErrors.NEXT_OF_INPROGRESS_CANNOT_BE_FINISHED_OR_INPROGRESS));
      } catch (PrevOfFinishedCannotBeNotPendingOrInProgress e) {
        errors.add(
            new EventUpdateError(
                event, CodeErrors.PREV_OF_FINISHED_CANNOT_BE_NOT_PENDING_OR_INPROGRESS));
      } catch (PrevOfInProgressCannotBePendingOrInProgress e) {
        errors.add(
            new EventUpdateError(
                event, CodeErrors.PREV_OF_INPROGRESS_CANNOT_BE_PENDING_OR_INPROGRESS));
      } catch (NextOfPendingCannotBeInprogressOrFinished e) {
        errors.add(
            new EventUpdateError(
                event, CodeErrors.NEXT_OF_PENDING_CANNOT_BE_INPROGRESS_OR_FINISHED));
      } catch (EntityNotFoundException e) {
        errors.add(new EventUpdateError(event, e.getMessage()));
      } catch (Exception e) {
        // Catch any other exception to prevent the main loop from stopping.
        errors.add(new EventUpdateError(event, e.getMessage()));
      }
    }
    return errors;
  }

  public Event getEventById(UUID id) throws EntityNotFoundException {
    Optional<Event> resultOpt = eventRepository.findById(id, AuthUtils.getUsername());
    if (resultOpt.isEmpty()) {
      throw new EntityNotFoundException("Event not found with id: " + id);
    } else {
      return resultOpt.get();
    }
  }

  public EventWithAssignmentsDto getEventByIdWithAssigments(UUID id)
      throws EntityNotFoundException {
    Optional<Event> resultOpt =
        eventRepository.findEventByIdWithAssignments(id, AuthUtils.getUsername());
    if (resultOpt.isEmpty()) {
      throw new EntityNotFoundException("Event not found with id: " + id);
    }
    return new EventWithAssignmentsDto(resultOpt.get());
  }

  public List<Event> findEvents(EventSearchForm form) {
    String apartmentName =
        form.getApartmentName() == null || form.getApartmentName().isEmpty()
            ? null
            : "%" + form.getApartmentName().toLowerCase() + "%";
    PageRequest pageRequest =
        ServiceUtils.createPageRequest(form.getPageNumber(), form.getPageSize());
    return eventRepository.advancedSearch(
        AuthUtils.getUsername(),
        apartmentName,
        form.getStates(),
        form.getTypes(),
        form.getStartDate(),
        form.getEndDate(),
        pageRequest);
  }

  public PageMetadata getEventsMetadata(EventSearchForm form) {
    String apartmentName =
        form.getApartmentName() == null || form.getApartmentName().isEmpty()
            ? null
            : "%" + form.getApartmentName().toLowerCase() + "%";
    int totalRows =
        eventRepository.advancedCount(
            AuthUtils.getUsername(),
            apartmentName,
            form.getStates(),
            form.getTypes(),
            form.getStartDate(),
            form.getEndDate());
    int totalPages = ServiceUtils.calculateTotalPages(form.getPageSize(), totalRows);
    return new PageMetadata(totalPages, totalRows);
  }

  public void deleteEvent(UUID id) throws EntityNotFoundException {
    Event event = getEventById(id);
    eventRepository.delete(event);
    workflowService.calculateApartmentState(event.getApartment().getId());
  }
}
