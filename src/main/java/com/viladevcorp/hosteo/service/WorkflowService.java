package com.viladevcorp.hosteo.service;

import com.viladevcorp.hosteo.model.*;
import com.viladevcorp.hosteo.model.dto.*;
import com.viladevcorp.hosteo.model.types.*;
import com.viladevcorp.hosteo.repository.ApartmentRepository;
import com.viladevcorp.hosteo.repository.AssignmentRepository;
import com.viladevcorp.hosteo.repository.EventRepository;
import com.viladevcorp.hosteo.repository.TaskRepository;
import com.viladevcorp.hosteo.utils.AuthUtils;
import java.time.Clock;
import org.springframework.data.domain.Pageable;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class WorkflowService {

  private final ApartmentRepository apartmentRepository;
  private final EventRepository eventRepository;
  private final AssignmentRepository assignmentRepository;
  private final TaskRepository taskRepository;
  private final Clock clock;

  @Autowired
  public WorkflowService(
      ApartmentRepository apartmentRepository,
      EventRepository eventRepository,
      AssignmentRepository assignmentRepository,
      TaskRepository taskRepository,
      Clock clock) {
    this.apartmentRepository = apartmentRepository;
    this.eventRepository = eventRepository;
    this.assignmentRepository = assignmentRepository;
    this.taskRepository = taskRepository;
    this.clock = clock;
  }

  public void calculateApartmentState(UUID id) throws EntityNotFoundException {

    Optional<Apartment> apartmentOpt = apartmentRepository.findById(id, AuthUtils.getUsername());
    if (apartmentOpt.isEmpty()) {
      throw new EntityNotFoundException("Apartment not found with id: " + id);
    }
    Apartment apartment = apartmentOpt.get();

    // If the apartment has an event in progress, is occupied
    if (eventRepository.existsEventByApartmentIdAndState(id, EventState.IN_PROGRESS)) {
      apartment.setState(ApartmentState.OCCUPIED);
      apartmentRepository.save(apartment);
      return;
    }

    List<Task> apartmentTasks = apartment.getTasks();
    // If the apartment has no tasks, its ready (nothing to do)
    if (apartmentTasks.isEmpty()) {
      apartment.setState(ApartmentState.READY);
      apartmentRepository.save(apartment);
      return;
    }

    // We index by taskId the tasks (faster access)
    Map<UUID, Task> mandatoryTasksMap = new HashMap<>();
    apartmentTasks.forEach(
        task -> {
          if (task.getType() == TaskType.MANDATORY) {
            mandatoryTasksMap.put(task.getId(), task);
          }
        });

    // Get the last finished event of the apartment
    Optional<Event> lastFinishedEvent =
        eventRepository.findFirstByCreatedByUsernameAndApartmentIdAndStateOrderByEndDateDesc(
            AuthUtils.getUsername(), id, EventState.FINISHED);

    // If not finished event found, the apartment is ready
    if (lastFinishedEvent.isEmpty()) {
      apartment.setState(ApartmentState.READY);
      apartmentRepository.save(apartment);
      return;
    }

    // We loop through the assignments of that last finished event
    Set<Assignment> eventAssignments = lastFinishedEvent.get().getAssignments();
    for (Assignment assignment : eventAssignments) {
      // If one of the assignments is not completed, the apartment is still USED (if the not
      // completed is an optional task, we still have to set used, as optional tasks are mandatory
      // when scheduled)
      if (assignment.getState().isPending()) {
        apartment.setState(ApartmentState.USED);
        apartmentRepository.save(apartment);
        return;
      }
      // If the finished task is one of the mandatory, we remove it from the map
      if (assignment.getTask().getType() == TaskType.MANDATORY) {
        mandatoryTasksMap.remove(assignment.getTask().getId());
      }
    }

    // At the end, if the map is empty (all mandatory tasks completed) we can set to ready the
    // apartment
    if (mandatoryTasksMap.isEmpty()) {
      apartment.setState(ApartmentState.READY);
    } else {
      apartment.setState(ApartmentState.USED);
    }
    apartmentRepository.save(apartment);
  }

  private static class ApartmentInfo {
    List<TaskDto> mandatoryTasks;
    Event nextPendingEvent;
    ApartmentState state;

    public ApartmentInfo(List<TaskDto> mandatoryTasks, Event nextPendingEvent, ApartmentState state) {
      this.mandatoryTasks = mandatoryTasks;
      this.nextPendingEvent = nextPendingEvent;
      this.state = state;
    }
  }

  private ApartmentInfo processApartment(
      Apartment apartment, Map<UUID, ApartmentInfo> apartmentInfoMap) {
    UUID apartmentId = apartment.getId();
    if (!apartmentInfoMap.containsKey(apartmentId)) {
      // We get the mandatory tasks of the apartment
      List<TaskDto> apartmentTasks =
          apartment.getTasks().stream()
              .filter(task -> task.getType() == TaskType.MANDATORY)
              .map(TaskDto::new)
              .collect(Collectors.toList());
      // We get the next pending event of the apartment
      Event nextPendingEvent =
          eventRepository
              .findFirstByCreatedByUsernameAndApartmentIdAndStateOrderByEndDateDesc(
                  AuthUtils.getUsername(), apartmentId, EventState.PENDING)
              .orElse(null);
      ApartmentInfo aptInfo =
          new ApartmentInfo(apartmentTasks, nextPendingEvent, apartment.getState());
      apartmentInfoMap.put(apartmentId, aptInfo);
      return aptInfo;
    } else {
      return apartmentInfoMap.get(apartmentId);
    }
  }

  /**
   * Processes an event for the scheduler: computes task/assignment counts and stores them in the
   * event DTO. Does NOT fetch the previous event — that relationship is handled externally via the
   * {@code previousEvent} map in {@link SchedulerInfo}.
   */
  private EventSchedulerDto processEventForScheduler(
      Event event,
      Map<UUID, ApartmentInfo> apartmentInfoMap,
      Map<UUID, EventSchedulerDto> eventMap)
      throws EntityNotFoundException {
    if (event == null) {
      return null;
    }
    // If the event has already been processed, we return the cached info
    if (eventMap.containsKey(event.getId())) {
      return eventMap.get(event.getId());
    }

    ApartmentInfo apartmentInfo = processApartment(event.getApartment(), apartmentInfoMap);
    // We create the eventDto based on the event
    EventSchedulerDto eventDto = new EventSchedulerDto(event);

    // We get the assignments of the event
    Set<Assignment> eventAssignments = event.getAssignments();
    int nMandatoryAssignedTask = 0, nExtraAssignedTask = 0, nCompletedAssignments = 0;
    List<AssignmentDto> uncompletedAssignments = new ArrayList<>();

    List<TaskDto> apartmentMandatoryTasks = apartmentInfo.mandatoryTasks;
    List<TaskDto> mandatoryUnassignedTasks = new ArrayList<>(apartmentMandatoryTasks);

    // For each assignment of the event
    for (Assignment assignment : eventAssignments) {
      // If its mandatory task, we add it to the mandatory assigned tasks counter
      if (assignment.getTask().getType() == TaskType.MANDATORY) {
        nMandatoryAssignedTask++;
        // We remove the task of the assignment from the list of mandatory unassigned tasks
        mandatoryUnassignedTasks.removeIf(
            taskDto -> taskDto.getId().equals(assignment.getTask().getId()));
      } else {
        // If its extra, we add it to the extra assigned tasks counter
        nExtraAssignedTask++;
      }
      // If the assignment is finished, we add it to the completed assignments counter
      if (assignment.getState() == AssignmentState.FINISHED) {
        nCompletedAssignments++;
      } else {
        // If its not finished, we add it to the uncompleted assignments list
        uncompletedAssignments.add(new AssignmentDto(assignment));
      }
    }
    eventDto.setNMandatoryAssignedTasks(nMandatoryAssignedTask);
    eventDto.setNExtraAssignedTasks(nExtraAssignedTask);
    eventDto.setMandatoryUnassignedTasks(mandatoryUnassignedTasks);
    eventDto.setNCompletedAssignments(nCompletedAssignments);
    eventDto.setUncompletedAssignments(uncompletedAssignments);

    eventMap.put(event.getId(), eventDto);
    return eventDto;
  }

  public SchedulerInfo getSchedulerInfo(Instant startDate, Instant endDate)
      throws EntityNotFoundException {
    SchedulerInfo schedulerInfo = new SchedulerInfo();

    // Events happening in the range of the scheduler range
    List<Event> eventsOnRange =
        eventRepository.findEventsByDateRange(AuthUtils.getUsername(), startDate, endDate);

    // Cached info about processed apartments
    Map<UUID, ApartmentInfo> apartmentInfoMap = new HashMap<>();
    // Central normalized map: all processed event data, keyed by event ID
    Map<UUID, EventSchedulerDto> eventMap = new HashMap<>();

    // Process events on range to get scheduler info — store IDs only
    for (Event event : eventsOnRange) {
      EventSchedulerDto dto = processEventForScheduler(event, apartmentInfoMap, eventMap);
      schedulerInfo.getBookings().add(dto.getId());
    }

    // Now we get the pending events until 5 days from now to check for alerts
    List<Event> alertEvents =
        eventRepository.advancedSearch(
            AuthUtils.getUsername(),
            null,
            Set.of(EventState.PENDING.name()),
            null,
            null,
            Instant.now(clock).plusSeconds(5 * 24 * 3600),
            Pageable.unpaged());

    // Group candidate events by apartment
    Map<UUID, List<Event>> alertEventsByApartment = new HashMap<>();
    for (Event event : alertEvents) {
      alertEventsByApartment
          .computeIfAbsent(event.getApartment().getId(), k -> new ArrayList<>())
          .add(event);
    }

    final Instant RED_FLAG_LIMIT = Instant.now(clock).plusSeconds(2 * 24 * 3600);
    final Instant YELLOW_FLAG_LIMIT = Instant.now(clock).plusSeconds(5 * 24 * 3600);

    // === Alert logic: resolve predecessors, process them, and compute alerts ===
    for (Map.Entry<UUID, List<Event>> entry : alertEventsByApartment.entrySet()) {
      UUID apartmentId = entry.getKey();
      List<Event> aptEvents = entry.getValue();
      aptEvents.sort(Comparator.comparing(Event::getStartDate));

      for (int i = 0; i < aptEvents.size(); i++) {
        Event currentEvent = aptEvents.get(i);

        // Get or create candidate DTO (may already exist from range processing)
        EventSchedulerDto currentEventSched = eventMap.get(currentEvent.getId());
        if (currentEventSched == null) {
          currentEventSched = new EventSchedulerDto(currentEvent);
          eventMap.put(currentEvent.getId(), currentEventSched);
        }

        // If the apartment is ready and this is the next pending event, skip alert
        ApartmentInfo aptInfo = apartmentInfoMap.get(apartmentId);
        if (aptInfo != null
            && aptInfo.nextPendingEvent != null
            && currentEvent.getId().equals(aptInfo.nextPendingEvent.getId())
            && aptInfo.state == ApartmentState.READY) {
          continue;
        }

        // Resolve predecessor
        Event predecessor;
        if (i == 0) {
          // First event in the sorted list: query DB for previous event
          predecessor =
              eventRepository
                  .findFirstEventBeforeDateWithState(
                      AuthUtils.getAuthUser().getId(),
                      apartmentId,
                      currentEvent.getStartDate(),
                      null)
                  .orElse(null);
        } else {
          // Subsequent events: predecessor is the previous one in the sorted list
          predecessor = aptEvents.get(i - 1);
        }

        if (predecessor == null) {
          continue;
        }

        // Store predecessor relationship
        schedulerInfo.getPreviousEvent().put(currentEvent.getId(), predecessor.getId());

        // Process the predecessor (cached in eventMap) — this is what alert logic inspects
        EventSchedulerDto previousEventSched =
            processEventForScheduler(predecessor, apartmentInfoMap, eventMap);

        // Mark overdue if the event start date is in the past
        if (currentEvent.getStartDate().isBefore(Instant.now(clock))) {
          currentEventSched.setOverdue(true);
        }

        // Red alert: event starts within 2 days
        if (currentEvent.getStartDate().isBefore(RED_FLAG_LIMIT)) {
          if (!previousEventSched.getMandatoryUnassignedTasks().isEmpty()) {
            currentEventSched.setAlert(Alert.DAYS_LEFT_2_UNASSIGNED);
            schedulerInfo.getRedAlertBookings().add(currentEventSched.getId());
            continue;
          }
          if (hasUnfinishedTasks(previousEventSched)) {
            currentEventSched.setAlert(Alert.DAYS_LEFT_2_NOT_COMPLETED);
            schedulerInfo.getRedAlertBookings().add(currentEventSched.getId());
            continue;
          }
        }

        // Yellow alert: event starts within 5 days
        if (currentEvent.getStartDate().isBefore(YELLOW_FLAG_LIMIT)) {
          if (!previousEventSched.getMandatoryUnassignedTasks().isEmpty()) {
            currentEventSched.setAlert(Alert.DAYS_LEFT_5_UNASSIGNED);
            schedulerInfo.getYellowAlertBookings().add(currentEventSched.getId());
          }
        }
      }
    }

    // Set the central event info map and propagate alerts to calendar events in eventInfo
    schedulerInfo.setEventInfo(eventMap);

    // Map assignments in range to DTOs (event is already JOIN FETCHed)
    schedulerInfo.setAssignments(
        assignmentRepository
            .findByApartmentAndStateAndDateRange(
                AuthUtils.getUsername(), null, null, startDate, endDate)
            .stream()
            .map(AssignmentDto::new)
            .collect(Collectors.toSet()));
    return schedulerInfo;
  }

  /** Checks whether the event scheduler DTO has any unfinished (pending) assignments. */
  private boolean hasUnfinishedTasks(EventSchedulerDto dto) {
    return dto.getUncompletedAssignments() != null
        && !dto.getUncompletedAssignments().isEmpty();
  }
}