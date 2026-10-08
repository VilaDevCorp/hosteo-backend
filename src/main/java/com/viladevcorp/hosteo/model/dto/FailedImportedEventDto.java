package com.viladevcorp.hosteo.model.dto;

import com.viladevcorp.hosteo.model.FailedImportedEvent;
import com.viladevcorp.hosteo.model.types.EventSource;
import java.time.Instant;
import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
@NoArgsConstructor
public class FailedImportedEventDto extends BaseEntityDto {

  public FailedImportedEventDto(FailedImportedEvent importedEvent) {
    if (importedEvent == null) {
      return;
    }
    BeanUtils.copyProperties(importedEvent, this, "apartment");
    if (importedEvent.getApartment() != null) {
      this.apartmentId = importedEvent.getApartment().getId();
      this.apartment = new ApartmentDto(importedEvent.getApartment());
    }
  }

  private UUID apartmentId;

  private ApartmentDto apartment;

  private String name;

  private Instant startDate;

  private Instant endDate;

  private EventSource source;

  private String error;
}