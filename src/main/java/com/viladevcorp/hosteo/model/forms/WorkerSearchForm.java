package com.viladevcorp.hosteo.model.forms;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class WorkerSearchForm {

  private String name;
  private Boolean visible;
  private int pageNumber;
  private int pageSize;
}
