package com.viladevcorp.hosteo.model;

import com.viladevcorp.hosteo.model.dto.AssignmentDto;
import com.viladevcorp.hosteo.model.dto.EventSchedulerDto;
import java.util.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SchedulerInfo {

  /** Central normalized map: all processed event data, keyed by event ID.
   *  Frontend looks up full event info (display details + task/assignment status + alert) here. */
  private Map<UUID, EventSchedulerDto> eventInfo = new HashMap<>();

  /** Relationships: futureEventId → previousEventId.
   *  For alert events, identifies which previous event's workload needs to be completed. */
  private Map<UUID, UUID> previousEvent = new HashMap<>();

  /** Calendar range: event IDs to render in the scheduler grid. */
  private List<UUID> bookings = new ArrayList<>();

  /** Sidebar red alerts: upcoming event IDs that have urgent (2-day) deadline pressure. */
  private List<UUID> redAlertBookings = new ArrayList<>();

  /** Sidebar yellow alerts: upcoming event IDs that have warning (5-day) deadline pressure. */
  private List<UUID> yellowAlertBookings = new ArrayList<>();

  private Set<AssignmentDto> assignments = new HashSet<>();
}
