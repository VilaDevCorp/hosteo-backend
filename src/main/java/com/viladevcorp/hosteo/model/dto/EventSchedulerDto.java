package com.viladevcorp.hosteo.model.dto;

import com.viladevcorp.hosteo.model.Event;
import com.viladevcorp.hosteo.model.types.Alert;
import com.viladevcorp.hosteo.model.types.EventSource;
import java.time.Instant;
import java.util.*;

import com.viladevcorp.hosteo.model.types.EventType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
@NoArgsConstructor
public class EventSchedulerDto extends BaseEntityDto {

  public EventSchedulerDto(Event event) {
    if (event == null) {
      return;
    }
    BeanUtils.copyProperties(event, this);
  }

  private UUID id;

  private EventType type;

  private Instant startDate;

  private Instant endDate;

  private String name;

  private EventSource source;

  private int nMandatoryAssignedTasks;
  private int nExtraAssignedTasks;
  private List<TaskDto> mandatoryUnassignedTasks = new ArrayList<>();

  private int nCompletedAssignments;
  private List<AssignmentDto> uncompletedAssignments;

  private Alert alert;

  private boolean overdue;
}
