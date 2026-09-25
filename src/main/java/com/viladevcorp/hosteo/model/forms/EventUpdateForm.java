package com.viladevcorp.hosteo.model.forms;

import com.viladevcorp.hosteo.model.types.EventSource;
import com.viladevcorp.hosteo.model.types.EventState;
import com.viladevcorp.hosteo.model.types.EventType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class EventUpdateForm {

  @NotNull private UUID id;

  @NotNull private Instant startDate;

  @NotNull private Instant endDate;

  @NotNull private String name;

  private EventSource source;

  @NotNull private EventState state;

  @NotNull private EventType type;
}
