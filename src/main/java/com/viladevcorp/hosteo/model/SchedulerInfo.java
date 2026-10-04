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

  /** Calendar range: event IDs to render in the scheduler grid. */
  private Set<EventSchedulerDto> events = new HashSet<>();

  private Set<AssignmentDto> assignments = new HashSet<>();
}
