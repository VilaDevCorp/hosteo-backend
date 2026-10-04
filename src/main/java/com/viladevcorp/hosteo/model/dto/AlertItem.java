package com.viladevcorp.hosteo.model.dto;

import com.viladevcorp.hosteo.model.types.Alert;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AlertItem {

  /** Type of the alert (red: urgent 2-day, yellow: warning 5-day). */
  private Alert alertType;

  /** Upcoming event that triggers the alert. */
  private EventDto event;

  /** Previous event to clean / create assignments for. */
  private EventSchedulerDto prevEvent;

  public AlertItem(Alert alertType, EventDto event, EventSchedulerDto prevEvent) {
    this.alertType = alertType;
    this.event = event;
    this.prevEvent = prevEvent;
  }
}
