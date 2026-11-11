package com.viladevcorp.hosteo.model.dto;

import com.viladevcorp.hosteo.model.Worker;
import com.viladevcorp.hosteo.model.types.ImageType;
import com.viladevcorp.hosteo.model.types.Language;
import com.viladevcorp.hosteo.utils.ImageUrlResolver;
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
    this.imageUrl =
        ImageUrlResolver.resolve(
            ImageType.WORKER, worker.getId().toString(), worker.isHasImage());
  }

  private String name;

  private Language language;

  private double salary;

  private boolean visible;

  private String imageUrl;
}
