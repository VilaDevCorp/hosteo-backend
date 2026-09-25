package com.viladevcorp.hosteo.model;

import com.viladevcorp.hosteo.model.dto.WorkerDto;
import com.viladevcorp.hosteo.model.types.Language;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "workers")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@AllArgsConstructor
public class Worker extends BaseEntity {

  @NotNull
  @NotBlank
  @Column(nullable = false, unique = true)
  private String name;

  @Enumerated(EnumType.STRING)
  private Language language = Language.EN;

  private double salary;

  @Builder.Default private boolean visible = true;

  @Override
  public WorkerDto toDto() {
    return new WorkerDto(this);
  }
}
