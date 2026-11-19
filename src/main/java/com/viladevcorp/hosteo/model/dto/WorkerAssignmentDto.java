package com.viladevcorp.hosteo.model.dto;

import com.viladevcorp.hosteo.model.Worker;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WorkerAssignmentDto {

  private WorkerDto worker;

  private List<AssignmentDto> assignments;

  public WorkerAssignmentDto(Worker worker, List<AssignmentDto> assignments) {
    this.worker = new WorkerDto(worker);
    this.assignments = assignments;
  }
}
