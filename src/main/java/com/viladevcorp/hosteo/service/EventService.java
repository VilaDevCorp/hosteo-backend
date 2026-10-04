package com.viladevcorp.hosteo.service;

import com.viladevcorp.hosteo.exceptions.*;
import com.viladevcorp.hosteo.model.*;
import com.viladevcorp.hosteo.model.dto.EventDto;
import com.viladevcorp.hosteo.model.dto.EventOperationError;
import com.viladevcorp.hosteo.model.dto.EventWithAssignmentsAndNextEventDto;
import com.viladevcorp.hosteo.model.forms.EventCreateForm;
import com.viladevcorp.hosteo.model.forms.EventSearchForm;
import com.viladevcorp.hosteo.model.forms.EventUpdateForm;
import com.viladevcorp.hosteo.model.types.EventState;
import com.viladevcorp.hosteo.repository.AssignmentRepository;
import com.viladevcorp.hosteo.repository.EventRepository;
import com.viladevcorp.hosteo.utils.AuthUtils;
import com.viladevcorp.hosteo.utils.CodeErrors;
import com.viladevcorp.hosteo.utils.ServiceUtils;

import java.util.*;
import jakarta.persistence.EntityNotFoundException;

import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
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
          NextOfInProgressCannotBeFinishedOrInProgress,
          EntityFrozenException {
    return eventProcessor.executeCreateEventLogic(form);
  }

  public Event updateEvent(EventUpdateForm form)
      throws EntityNotFoundException,
          NotAvailableDatesException,
          PrevOfInProgressCannotBePendingOrInProgress,
          PrevOfFinishedCannotBeNotPendingOrInProgress,
          NextOfPendingCannotBeInprogressOrFinished,
          NextOfInProgressCannotBeFinishedOrInProgress,
          EntityFrozenException {
    Event event = getEventById(form.getId());
    if (event.getApartment() != null && !event.getApartment().isVisible()) {
      throw new EntityFrozenException("Cannot update an event of a hidden apartment.");
    }
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
          NextOfInProgressCannotBeFinishedOrInProgress,
          EntityFrozenException {
    return eventProcessor.executeUpdateStateLogic(eventId, state);
  }

  public List<EventOperationError> updateBulkEventState(Set<UUID> eventIds, EventState state) {

    List<EventOperationError> errors = new ArrayList<>();

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
            new EventOperationError(
                event, CodeErrors.NEXT_OF_INPROGRESS_CANNOT_BE_FINISHED_OR_INPROGRESS));
      } catch (PrevOfFinishedCannotBeNotPendingOrInProgress e) {
        errors.add(
            new EventOperationError(
                event, CodeErrors.PREV_OF_FINISHED_CANNOT_BE_PENDING_OR_INPROGRESS));
      } catch (PrevOfInProgressCannotBePendingOrInProgress e) {
        errors.add(
            new EventOperationError(
                event, CodeErrors.PREV_OF_INPROGRESS_CANNOT_BE_PENDING_OR_INPROGRESS));
      } catch (NextOfPendingCannotBeInprogressOrFinished e) {
        errors.add(
            new EventOperationError(
                event, CodeErrors.NEXT_OF_PENDING_CANNOT_BE_INPROGRESS_OR_FINISHED));
      } catch (EntityNotFoundException e) {
        errors.add(new EventOperationError(event, e.getMessage()));
      } catch (Exception e) {
        // Catch any other exception to prevent the main loop from stopping.
        errors.add(new EventOperationError(event, e.getMessage()));
      }
    }
    return errors;
  }

  public List<EventOperationError> deleteBulkEvents(Set<UUID> eventIds) {
    List<EventOperationError> errors = new ArrayList<>();

    List<Event> events =
        eventRepository.findInIdsAndCreatedByUsername(eventIds, AuthUtils.getUsername());

    for (Event event : events) {
      try {
        eventProcessor.executeDeleteEventLogic(event.getId());
      } catch (EntityNotFoundException e) {
        errors.add(new EventOperationError(event, e.getMessage()));
      } catch (Exception e) {
        errors.add(new EventOperationError(event, e.getMessage()));
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

  public EventWithAssignmentsAndNextEventDto getEventByIdWithAssigmentsAndNextEvent(UUID id)
      throws EntityNotFoundException {
    Optional<Event> resultOpt =
        eventRepository.findEventByIdWithAssignments(id, AuthUtils.getUsername());
    if (resultOpt.isEmpty()) {
      throw new EntityNotFoundException("Event not found with id: " + id);
    }
    Event event = resultOpt.get();
    EventWithAssignmentsAndNextEventDto dto = new EventWithAssignmentsAndNextEventDto(event);
    // Attach the start date of the next event (used by the frontend as the assignment limit).
    eventRepository
        .findFirstEventAfterDateWithState(
            AuthUtils.getAuthUser().getId(),
            event.getApartment().getId(),
            event.getStartDate(),
            null)
        .ifPresent(nextEvent -> dto.setNextEvent(new EventDto(nextEvent)));
    return dto;
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
        form.getFrozen(),
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
            form.getEndDate(),
            form.getFrozen());
    int totalPages = ServiceUtils.calculateTotalPages(form.getPageSize(), totalRows);
    return new PageMetadata(totalPages, totalRows);
  }

  public void deleteEvent(UUID id) throws EntityNotFoundException, EntityFrozenException {
    Event event = getEventById(id);
    if (event.getApartment() != null && !event.getApartment().isVisible()) {
      throw new EntityFrozenException("Cannot delete an event of a hidden apartment.");
    }
    eventRepository.delete(event);
    event.getApartment().getEvents().remove(event);
    workflowService.calculateApartmentState(event.getApartment().getId());
  }
}
