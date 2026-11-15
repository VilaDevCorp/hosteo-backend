package com.viladevcorp.hosteo.model.forms;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.viladevcorp.hosteo.model.types.CategoryEnum;

import com.viladevcorp.hosteo.model.types.TaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TaskUpdateForm {

  @NotNull private UUID id;

  @NotNull @NotBlank private String name;

  @NotNull private TaskType type;

  @NotNull private CategoryEnum category;

  @PositiveOrZero private int duration;

  @PositiveOrZero private double cost;

  private List<String> steps = new ArrayList<>();
}
