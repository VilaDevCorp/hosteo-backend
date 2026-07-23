package com.viladevcorp.hosteo.model.forms;

import com.viladevcorp.hosteo.model.types.AssignmentState;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AssignmentSearchForm {

  private String taskName;

  private String state;

  private int pageNumber;

  private int pageSize;
}
