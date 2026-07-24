package com.viladevcorp.hosteo.service;

import com.viladevcorp.hosteo.exceptions.*;
import com.viladevcorp.hosteo.model.*;
import com.viladevcorp.hosteo.model.forms.EventCreateForm;
import com.viladevcorp.hosteo.model.types.EventState;
import com.viladevcorp.hosteo.repository.ApartmentRepository;
import com.viladevcorp.hosteo.repository.AssignmentRepository;
import com.viladevcorp.hosteo.repository.EventRepository;
import com.viladevcorp.hosteo.utils.AuthUtils;
import com.viladevcorp.hosteo.utils.ServiceUtils;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class EventProcessor {

  private final EventRepository eventRepository;
  private final AssignmentRepository assignmentRepository;
  private final WorkflowService workflowService;
  private final ApartmentRepository apartmentRepository;

  @Autowired
  public EventProcessor(
      EventRepository eventRepository,
      WorkflowService workflowService,
      AssignmentRepository assignmentRepository,
      ApartmentRepository apartmentRepository) {
    this.eventRepository = eventRepository;
    this.workflowService = workflowService;
    this.assignmentRepository = assignmentRepository;
    this.apartmentRepository = apartmentRepository;
  }

  public Event getEventById(UUID id) throws EntityNotFoundException {
    Optional<Event> resultOpt = eventRepository.findById(id, AuthUtils.getUsername());
    if (resultOpt.isEmpty()) {
      throw new EntityNotFoundException("Event not found with id: " + id);
    } else {
      return resultOpt.get();
    }
  }

  public void validateEventState(UUID apartmentId, EventState state, Instant startDate)
      throws NextOfPendingCannotBeInprogressOrFinished,
          PrevOfInProgressCannotBePendingOrInProgress,
          PrevOfFinishedCannotBeNotPendingOrInProgress,
          NextOfInProgressCannotBeFinishedOrInProgress {
    // If the state is pending we cannot have IN PROGRESS or FINISHED events after (if the next ones
    // have finished this one should have too)
    if (state.isPending()) {
      Optional<Event> nextEventOpt =
          eventRepository.findFirstEventAfterDateWithState(
              AuthUtils.getAuthUser().getId(), apartmentId, startDate, null);
      Event nextEvent = nextEventOpt.orElse(null);
      if (nextEvent != null
          && (nextEvent.getState().isInProgress() || nextEvent.getState().isFinished())) {
        log.error(
            "[EventService.validateEventState] - Event cannot be set to PENDING because there is a next event IN_PROGRESS or FINISHED for apartment id: {}",
            apartmentId);
        throw new NextOfPendingCannotBeInprogressOrFinished();
      }
    }
    if (state.isInProgress()) {
      Optional<Event> previousEventOpt =
          eventRepository.findFirstEventBeforeDateWithState(
              AuthUtils.getAuthUser().getId(), apartmentId, startDate, null);
      Event previousEvent = previousEventOpt.orElse(null);
      if (previousEvent != null
          && (previousEvent.getState().isPending() || previousEvent.getState().isInProgress())) {
        log.error(
            "[EventService.validateEventState] - Event cannot be set to IN_PROGRESS because there is a previous event PENDING or IN_PROGRESS for apartment id: {}",
            apartmentId);
        throw new PrevOfInProgressCannotBePendingOrInProgress();
      }
      Optional<Event> nextEventOpt =
          eventRepository.findFirstEventAfterDateWithState(
              AuthUtils.getAuthUser().getId(), apartmentId, startDate, null);
      Event nextEvent = nextEventOpt.orElse(null);
      if (nextEvent != null
          && (nextEvent.getState().isInProgress() || nextEvent.getState().isFinished())) {
        log.error(
            "[EventService.validateEventState] - Event cannot be set to IN_PROGRESS because there is a next event IN_PROGRESS or FINISHED for apartment id: {}",
            apartmentId);
        throw new NextOfInProgressCannotBeFinishedOrInProgress();
      }
    }
    if (state.isFinished()) {
      Optional<Event> previousEventOpt =
          eventRepository.findFirstEventBeforeDateWithState(
              AuthUtils.getAuthUser().getId(), apartmentId, startDate, null);
      Event previousEvent = previousEventOpt.orElse(null);
      if (previousEvent != null
          && (previousEvent.getState().isPending() || previousEvent.getState().isInProgress())) {
        log.error(
            "[EventService.validateEventState] - Event cannot be set to FINISHED because there is a previous event PENDING or IN_PROGRESS for apartment id: {}",
            apartmentId);
        throw new PrevOfFinishedCannotBeNotPendingOrInProgress();
      }
    }
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
  public Event executeCreateEventLogic(EventCreateForm form)
      throws EntityNotFoundException,
          NotAvailableDatesException,
          PrevOfInProgressCannotBePendingOrInProgress,
          PrevOfFinishedCannotBeNotPendingOrInProgress,
          NextOfPendingCannotBeInprogressOrFinished,
          NextOfInProgressCannotBeFinishedOrInProgress {
    Optional<Apartment> apartmentOpt =
        apartmentRepository.findById(form.getApartmentId(), AuthUtils.getUsername());
    if (apartmentOpt.isEmpty()) {
      throw new EntityNotFoundException("Apartment not found with id: " + form.getApartmentId());
    }
    Apartment apartment = apartmentOpt.get();
    Pair<Event, Assignment> conflicts =
        ServiceUtils.getScheduleConflicts(
            eventRepository,
            assignmentRepository,
            form.getApartmentId(),
            form.getStartDate(),
            form.getEndDate(),
            null,
            null);

    if (conflicts.a != null || conflicts.b != null) {
      log.error(
          "[{}] - Apartment with id: {} is not available between {} and {}",
          "EventService.createEvent",
          form.getApartmentId(),
          form.getStartDate(),
          form.getEndDate());
      throw new NotAvailableDatesException("Apartment is not available in the selected dates.");
    }

    Event event =
        Event.builder()
            .type(form.getType())
            .apartment(apartment)
            .startDate(form.getStartDate())
            .endDate(form.getEndDate())
            .name(form.getName())
            .state(form.getState())
            .source(form.getSource())
            .build();

    Event result = eventRepository.save(event);
    workflowService.calculateApartmentState(form.getApartmentId());
    validateEventState(form.getApartmentId(), form.getState(), form.getStartDate());
    return result;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
  public Event executeUpdateStateLogic(UUID eventId, EventState state)
      throws EntityNotFoundException,
          PrevOfInProgressCannotBePendingOrInProgress,
          PrevOfFinishedCannotBeNotPendingOrInProgress,
          NextOfPendingCannotBeInprogressOrFinished,
          NextOfInProgressCannotBeFinishedOrInProgress {
    Event event = getEventById(eventId);
    UUID apartmentId = event.getApartment().getId();
    event.setState(state);
    Event result = eventRepository.save(event);
    try {
      workflowService.calculateApartmentState(result.getApartment().getId());
      validateEventState(apartmentId, state, event.getStartDate());
    } catch (Exception e) {
      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
      throw e;
    }
    return result;
  }
}
