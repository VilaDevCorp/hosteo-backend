package com.viladevcorp.hosteo.model.forms;

import com.viladevcorp.hosteo.model.types.TaskType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class TaskSearchForm {

  private String name;

  private UUID apartmentId;

  private TaskType type;

  private Boolean visible;

  private int pageNumber;

  private int pageSize;
}
