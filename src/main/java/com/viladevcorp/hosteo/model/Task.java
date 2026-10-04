package com.viladevcorp.hosteo.model;

import java.util.ArrayList;
import java.util.List;

import com.viladevcorp.hosteo.model.dto.BaseEntityDto;
import com.viladevcorp.hosteo.model.dto.TaskDto;
import com.viladevcorp.hosteo.model.jsonconverters.StepsJsonConverter;
import com.viladevcorp.hosteo.model.types.CategoryEnum;

import com.viladevcorp.hosteo.model.types.TaskType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Task extends BaseEntity {

  @NotNull
  @Column(nullable = false)
  private String name;

  @NotNull
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private TaskType type;

  @NotNull
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private CategoryEnum category;

  @Column(nullable = false)
  private int duration;

  @ManyToOne(optional = true)
  private Apartment apartment;

  @Convert(converter = StepsJsonConverter.class)
  @Column(columnDefinition = "TEXT")
  @Builder.Default
  private List<String> steps = new ArrayList<>();

  @Builder.Default private boolean visible = true;

  @Override
  public BaseEntityDto toDto() {
    return new TaskDto(this);
  }
}
