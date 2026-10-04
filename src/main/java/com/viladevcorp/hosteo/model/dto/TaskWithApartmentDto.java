package com.viladevcorp.hosteo.model.dto;

import com.viladevcorp.hosteo.model.Task;
import com.viladevcorp.hosteo.model.types.CategoryEnum;
import java.util.ArrayList;
import java.util.List;

import com.viladevcorp.hosteo.model.types.TaskType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
@NoArgsConstructor
public class TaskWithApartmentDto extends BaseEntityDto {

  public TaskWithApartmentDto(Task task) {
    if (task == null) {
      return;
    }
    BeanUtils.copyProperties(task, this, "apartment");
    if (task.getApartment() != null) {
      this.apartment = new ApartmentDto(task.getApartment());
    }
  }

  private String name;

  private CategoryEnum category;

  private int duration;

  private TaskType type;

  private List<String> steps = new ArrayList<>();

  private boolean visible = true;

  private ApartmentDto apartment;
}
