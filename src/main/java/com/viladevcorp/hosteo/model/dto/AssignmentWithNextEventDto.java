package com.viladevcorp.hosteo.model.dto;

import com.viladevcorp.hosteo.model.Assignment;
import com.viladevcorp.hosteo.model.types.AssignmentState;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
@NoArgsConstructor
public class AssignmentWithNextEventDto extends BaseEntityDto {

  public AssignmentWithNextEventDto(Assignment assignment, EventDto nextEvent) {
    if (assignment == null) {
      return;
    }
    BeanUtils.copyProperties(assignment, this, "task", "worker", "event");
    this.task = new TaskDto(assignment.getTask());
    this.worker = new WorkerDto(assignment.getWorker());
    this.event = new EventDto(assignment.getEvent());
    this.nextEvent = nextEvent;
  }

  private TaskDto task;

  private Instant startDate;

  private Instant endDate;

  private WorkerDto worker;

  private AssignmentState state;

  private EventDto event;

  private EventDto nextEvent;
}
