package com.viladevcorp.hosteo.model.dto;

import com.viladevcorp.hosteo.model.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AssignmentOperationError extends BaseEntityDto {

  public AssignmentOperationError(Assignment assignment, String error) {
    this.assignment = assignment == null ? null : assignment.toDto();
    this.error = error;
  }

  AssignmentDto assignment;
  String error;
}