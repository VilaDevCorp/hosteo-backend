package com.viladevcorp.hosteo.model.dto;

import com.viladevcorp.hosteo.model.*;
import com.viladevcorp.hosteo.model.types.ApartmentState;
import com.viladevcorp.hosteo.model.types.ImageType;
import com.viladevcorp.hosteo.utils.ImageUrlResolver;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ApartmentWithTasksDto extends BaseEntityDto {

  public ApartmentWithTasksDto(Apartment apartment) {
    if (apartment == null) {
      return;
    }
    BeanUtils.copyProperties(apartment, this, "tasks");
    this.imageUrl =
        ImageUrlResolver.resolve(
            ImageType.APARTMENT, apartment.getId().toString(), apartment.isHasImage());
    this.tasks =
        apartment.getTasks().stream()
            .sorted(Comparator.comparing(Task::getCreatedAt))
            .map(TaskDto::new)
            .toList();
  }

  private String name;

  private String airbnbId;

  private String bookingId;

  private Address address;

  private ApartmentState state = ApartmentState.READY;

  private boolean visible = true;

  private String imageUrl;

  private List<TaskDto> tasks = new ArrayList<>();
}
