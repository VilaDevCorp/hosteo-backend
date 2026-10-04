package com.viladevcorp.hosteo.model.dto;

import com.viladevcorp.hosteo.model.Event;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EventOperationError {

  public EventOperationError(Event event, String error) {
    this.event = event == null ? null : event.toDto();
    this.error = error;
  }

  EventDto event;
  String error;
}