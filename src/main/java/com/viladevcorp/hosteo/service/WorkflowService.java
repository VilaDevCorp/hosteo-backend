package com.viladevcorp.hosteo.service;

import com.viladevcorp.hosteo.model.*;
import com.viladevcorp.hosteo.model.dto.*;
import com.viladevcorp.hosteo.model.types.Alert;
import com.viladevcorp.hosteo.model.types.ApartmentState;
import com.viladevcorp.hosteo.model.types.EventState;
import com.viladevcorp.hosteo.model.types.TaskType;
import com.viladevcorp.hosteo.repository.ApartmentRepository;
import com.viladevcorp.hosteo.repository.AssignmentRepository;
import com.viladevcorp.hosteo.repository.EventRepository;
import com.viladevcorp.hosteo.repository.TaskRepository;
import com.viladevcorp.hosteo.utils.AuthUtils;
import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import javax.management.InstanceNotFoundException;
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

  public void calculateApartmentState(UUID id) throws InstanceNotFoundException {

    Optional<Apartment> apartmentOpt = apartmentRepository.findById(id, AuthUtils.getUsername());
    if (apartmentOpt.isEmpty()) {
      throw new InstanceNotFoundException("Apartment not found with id: " + id);
    }
    Apartment apartment = apartmentOpt.get();

    // If the apartment has an event in progress, is occupied
    if (eventRepository.existsEventByApartmentIdAndState(id, EventState.IN_PROGRESS.toString())) {
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
            AuthUtils.getUsername(), id, EventState.FINISHED.toString());

    // If not finished event found, the apartment is ready
    if (lastFinishedEvent.isEmpty()) {
      apartment.setState(ApartmentState.READY);
      apartmentRepository.save(apartment);
      return;
    }

    // We loop through the assignments of that last finished event
    Set<Assignment> eventAssignments = lastFinishedEvent.get().getAssignments();
    for (Assignment assignment : eventAssignments) {
      // If one of the assignments is not completed, the apartment is still USED
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
    List<TaskDto> tasks;
    Event nextPendingEvent;
    ApartmentState state;

    public ApartmentInfo(List<TaskDto> tasks, Event nextPendingEvent, ApartmentState state) {
      this.tasks = tasks;
      this.nextPendingEvent = nextPendingEvent;
      this.state = state;
    }
  }

  //  private ApartmentInfo processApartment(
  //      Apartment apartment, Map<UUID, ApartmentInfo> apartmentInfoMap) {
  //    UUID apartmentId = apartment.getId();
  //    if (!apartmentInfoMap.containsKey(apartmentId)) {
  //      List<TaskDto> apartmentTasks =
  //          taskRepository
  //              .findNonExtraTasksByApartmentId(AuthUtils.getUsername(), apartmentId)
  //              .stream()
  //              .map(TaskDto::new)
  //              .collect(Collectors.toList());
  //      Event nextPendingEvent =
  //          eventRepository
  //              .findFirstByCreatedByUsernameAndApartmentIdAndStateOrderByEndDateAsc(
  //                  AuthUtils.getUsername(), apartmentId, EventState.PENDING)
  //              .orElse(null);
  //      ApartmentInfo aptInfo =
  //          new ApartmentInfo(apartmentTasks, nextPendingEvent, apartment.getState());
  //      apartmentInfoMap.put(apartmentId, aptInfo);
  //      return aptInfo;
  //    } else {
  //      return apartmentInfoMap.get(apartmentId);
  //    }
  //  }
  //
  //  private EventSchedulerDto processEventForScheduler(
  //      Event event, Map<UUID, ApartmentInfo> apartmentInfoMap, Map<UUID, EventSchedulerDto>
  // eventMap)
  //      throws InstanceNotFoundException {
  //    if (event == null) {
  //      return null;
  //    }
  //    if (eventMap.containsKey(event.getId())) {
  //      return eventMap.get(event.getId());
  //    }
  //    Set<Assignment> eventAssignments = getAssigmentsRelatedToEvent(event.getId());
  //    EventSchedulerDto eventDto = new EventSchedulerDto();
  //    eventDto.setEvent(new EventDto(event));
  //    ApartmentInfo apartmentInfo = processApartment(event.getApartment(), apartmentInfoMap);
  //    List<TaskDto> apartmentTasks = apartmentInfo.tasks;
  //    Set<UUID> assignedTaskIds =
  //        eventAssignments.stream()
  //            .map(assignment -> assignment.getTask().getId())
  //            .collect(Collectors.toSet());
  //    List<TaskDto> tasksToRemove = new ArrayList<>();
  //    for (TaskDto taskDto : apartmentTasks) {
  //      if (assignedTaskIds.contains(taskDto.getId())) {
  //        eventDto.getAssignedTasks().add(taskDto);
  //        tasksToRemove.add(taskDto);
  //      }
  //    }
  //    apartmentTasks.removeAll(tasksToRemove);
  //    eventDto.getUnassignedTasks().addAll(apartmentTasks);
  //    eventDto.setHasUnfinishedTasks(
  //        eventAssignments.stream()
  //            .anyMatch(
  //                assignment ->
  //                    assignment.getState()
  //                        == com.viladevcorp.hosteo.model.types.AssignmentState.PENDING));
  //
  //    if (!eventMap.containsKey(event.getId())) {
  //      eventMap.put(event.getId(), eventDto);
  //    }
  //    Event previousEvent =
  //        eventRepository
  //            .findFirstEventBeforeDateWithState(
  //                AuthUtils.getAuthUser().getId(),
  //                event.getApartment().getId(),
  //                event.getStartDate(),
  //                null)
  //            .orElse(null);
  //    eventDto.setPrevEvent(new SimpleEventSchedulerDto(previousEvent));
  //
  //    return eventDto;
  //  }
  //
  //  private void passAlertsToRangeEvent(
  //      List<EventSchedulerDto> rangeEvents, EventSchedulerDto eventWithAlert) {
  //    EventSchedulerDto rangeEventSched =
  //        rangeEvents.stream()
  //            .filter(event -> event.getEvent().getId().equals(eventWithAlert.getEvent().getId()))
  //            .findFirst()
  //            .orElse(null);
  //    if (rangeEventSched != null) {
  //      rangeEventSched.setAlert(eventWithAlert.getAlert());
  //    }
  //  }
  //
  //  public SchedulerInfo getSchedulerInfo(Instant startDate, Instant endDate)
  //      throws InstanceNotFoundException {
  //    SchedulerInfo schedulerInfo = new SchedulerInfo();
  //    List<Event> eventsOnRange =
  //        eventRepository.findEventsByDateRange(AuthUtils.getUsername(), startDate, endDate);
  //    List<EventSchedulerDto> rangeEvents = new ArrayList<>();
  //    List<EventSchedulerDto> redAlertEvents = new ArrayList<>();
  //    List<EventSchedulerDto> yellowAlertEvents = new ArrayList<>();
  //    Map<UUID, ApartmentInfo> apartmentInfoMap = new HashMap<>();
  //    Map<UUID, EventSchedulerDto> eventMap = new HashMap<>();
  //    // With this we got all events inside the range of the scheduler
  //    for (Event event : eventsOnRange) {
  //      rangeEvents.add(processEventForScheduler(event, apartmentInfoMap, eventMap));
  //    }
  //
  //    // Now we get the pending events until 5 days from now to check for alerts
  //    List<Event> alertEvents =
  //        eventRepository.advancedSearch(
  //            AuthUtils.getUsername(),
  //            null,
  //            List.of(EventState.PENDING),
  //            null,
  //            Instant.now(clock).plusSeconds(5 * 24 * 3600),
  //            null);
  //
  //    // Group by apartment in the alertEventsMap, processing them for the scheduler
  //    Map<UUID, List<EventSchedulerDto>> alertEventsMap = new HashMap<>();
  //
  //    for (Event event : alertEvents) {
  //      if (!alertEventsMap.containsKey(event.getApartment().getId())) {
  //        alertEventsMap.put(event.getApartment().getId(), new ArrayList<>());
  //      }
  //      alertEventsMap
  //          .get(event.getApartment().getId())
  //          .add(processEventForScheduler(event, apartmentInfoMap, eventMap));
  //    }
  //
  //    for (UUID apartmentId : alertEventsMap.keySet()) {
  //      List<EventSchedulerDto> aptEvents = alertEventsMap.get(apartmentId);
  //      aptEvents.sort(Comparator.comparing(b -> b.getEvent().getStartDate()));
  //      for (int i = 0; i < aptEvents.size(); i++) {
  //        EventSchedulerDto previousEventSched;
  //        EventSchedulerDto currentEventSched = aptEvents.get(i);
  //        // If its the next pending event and the apartment is ready, we dont care about cleaning
  //        // tasks (override)
  //        if (currentEventSched
  //                .getEvent()
  //                .getId()
  //                .equals(apartmentInfoMap.get(apartmentId).nextPendingEvent.getId())
  //            && apartmentInfoMap.get(apartmentId).state == ApartmentState.READY) {
  //          aptEvents.get(i).setApartmentReady(true);
  //          continue;
  //        }
  //        // If the event is the first in the alertEvents of the apartment, we check for a
  //        // previous event in DB
  //        if (i == 0) {
  //          previousEventSched =
  //              processEventForScheduler(
  //                  eventRepository
  //                      .findFirstEventBeforeDateWithState(
  //                          AuthUtils.getAuthUser().getId(),
  //                          apartmentId,
  //                          currentEventSched.getEvent().getStartDate(),
  //                          null)
  //                      .orElse(null),
  //                  apartmentInfoMap,
  //                  eventMap);
  //          // If its not the first one, we can get the previous event from the alert list (we
  //          // already retrieved it)
  //        } else {
  //          previousEventSched = aptEvents.get(i - 1);
  //        }
  //
  //        final Instant RED_FLAG_LIMIT = Instant.now(clock).plusSeconds(2 * 24 * 3600);
  //        if (currentEventSched.getEvent().getStartDate().isBefore(RED_FLAG_LIMIT)) {
  //          // if the event is the first one of the apartment, but the apartment is dirty, we
  // assume
  //          // that the
  //          if (previousEventSched == null) {}
  //
  //          if (!previousEventSched.getUnassignedTasks().isEmpty()) {
  //            currentEventSched.setAlert(Alert.DAYS_LEFT_2_UNASSIGNED);
  //            passAlertsToRangeEvent(rangeEvents, currentEventSched);
  //            redAlertEvents.add(currentEventSched);
  //            continue;
  //          }
  //          if (previousEventSched.isHasUnfinishedTasks()) {
  //            currentEventSched.setAlert(Alert.DAYS_LEFT_2_NOT_COMPLETED);
  //            passAlertsToRangeEvent(rangeEvents, currentEventSched);
  //            redAlertEvents.add(currentEventSched);
  //            continue;
  //          }
  //        }
  //        Instant YELLOW_FLAG_LIMIT = Instant.now(clock).plusSeconds(5 * 24 * 3600);
  //        if (currentEventSched.getEvent().getStartDate().isBefore(YELLOW_FLAG_LIMIT)) {
  //          if (!previousEventSched.getUnassignedTasks().isEmpty()) {
  //            currentEventSched.setAlert(Alert.DAYS_LEFT_5_UNASSIGNED);
  //            passAlertsToRangeEvent(rangeEvents, currentEventSched);
  //            yellowAlertEvents.add(currentEventSched);
  //          }
  //        }
  //      }
  //    }
  //
  //    schedulerInfo.setEvents(rangeEvents);
  //    schedulerInfo.setRedAlertEvents(redAlertEvents);
  //    schedulerInfo.setYellowAlertEvents(yellowAlertEvents);
  //
  //    // We get the assignments and for each one, we calculate the limit dates (previous event end
  //    // and next event start)
  //    schedulerInfo.setAssignments(
  //        assignmentRepository
  //            .findByApartmentAndStateAndDateRangeAndExtra(
  //                AuthUtils.getUsername(), null, null, startDate, endDate, null)
  //            .stream()
  //            .map(
  //                assignment -> {
  //                  SimpleEventSchedulerDto previousEvent =
  //                      new SimpleEventSchedulerDto(
  //                          eventRepository
  //                              .findFirstEventBeforeDateWithState(
  //                                  AuthUtils.getAuthUser().getId(),
  //                                  assignment.getTask().getApartment().getId(),
  //                                  assignment.getStartDate(),
  //                                  null)
  //                              .orElse(null));
  //
  //                  Event nextEvent =
  //                      eventRepository
  //                          .findFirstEventAfterDateWithState(
  //                              AuthUtils.getAuthUser().getId(),
  //                              assignment.getTask().getApartment().getId(),
  //                              assignment.getStartDate(),
  //                              null)
  //                          .orElse(null);
  //                  SimpleEventSchedulerDto nextEventDto = null;
  //
  //                  if (nextEvent != null) {
  //                    if (eventMap.get(nextEvent.getId()) != null) {
  //                      nextEventDto = new
  // SimpleEventSchedulerDto(eventMap.get(nextEvent.getId()));
  //                    } else {
  //                      nextEventDto = new SimpleEventSchedulerDto(nextEvent);
  //                    }
  //                  }
  //
  //                  return new AssignmentForSchedulerDto(assignment, previousEvent, nextEventDto);
  //                })
  //            .collect(Collectors.toSet()));
  //    schedulerInfo.setExtraTasks(
  //        taskRepository.findExtraTasksNotAssigned(AuthUtils.getUsername()).stream()
  //            .map(TaskDto::new)
  //            .collect(Collectors.toList()));
  //    return schedulerInfo;
  //  }
}
