package com.viladevcorp.hosteo.model.dto;

import com.viladevcorp.hosteo.model.*;
import com.viladevcorp.hosteo.model.types.ApartmentState;
import com.viladevcorp.hosteo.model.types.ImageType;
import com.viladevcorp.hosteo.utils.ImageUrlResolver;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
@NoArgsConstructor
public class ApartmentDto extends BaseEntityDto {

  public ApartmentDto(Apartment apartment) {
    if (apartment == null) {
      return;
    }
    BeanUtils.copyProperties(apartment, this);
    this.imageUrl =
        ImageUrlResolver.resolve(
            ImageType.APARTMENT, apartment.getId().toString(), apartment.isHasImage());
  }

  private String name;

  private String airbnbId;

  private String bookingId;

  private Address address;

  private ApartmentState state = ApartmentState.READY;

  private boolean visible = true;

  private String imageUrl;
}
