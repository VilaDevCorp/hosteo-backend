package com.viladevcorp.hosteo.model.forms;

import com.viladevcorp.hosteo.model.types.EventState;
import com.viladevcorp.hosteo.model.types.EventType;
import lombok.Data;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
public class EventSearchForm {

  private Set<UUID> apartmentIds;
  private String apartmentName;
  private Set<String> types;
  private Set<String> states;
  private Instant startDate;
  private Instant endDate;

  /**
   * Filters events by whether they are frozen (their apartment is hidden).
   * {@code null} = all events, {@code true} = only frozen events,
   * {@code false} = only non-frozen events.
   */
  private Boolean frozen;

  private int pageNumber = -1;
  private int pageSize;
}
