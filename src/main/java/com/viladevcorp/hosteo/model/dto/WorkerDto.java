package com.viladevcorp.hosteo.model.dto;

import com.viladevcorp.hosteo.model.Worker;
import com.viladevcorp.hosteo.model.types.Language;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
@NoArgsConstructor
public class WorkerDto extends BaseEntityDto {

  public WorkerDto(Worker worker) {
    if (worker == null) {
      return;
    }
    BeanUtils.copyProperties(worker, this);
  }

  private String name;

  private Language language;

  private double salary;

  private boolean visible;
}
