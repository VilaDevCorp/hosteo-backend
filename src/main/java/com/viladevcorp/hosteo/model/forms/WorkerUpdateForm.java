package com.viladevcorp.hosteo.model.forms;

import java.util.UUID;

import com.viladevcorp.hosteo.model.types.Language;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class WorkerUpdateForm {

  @NotNull private UUID id;

  @NotNull @NotBlank private String name;

  private Language language;

  private double salary;

  private boolean visible;
}
