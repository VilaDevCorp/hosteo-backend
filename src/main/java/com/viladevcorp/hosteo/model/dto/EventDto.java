package com.viladevcorp.hosteo.model.dto;

import com.viladevcorp.hosteo.model.Event;
import com.viladevcorp.hosteo.model.types.EventSource;
import com.viladevcorp.hosteo.model.types.EventState;
import com.viladevcorp.hosteo.model.types.EventType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
public class EventDto extends BaseEntityDto {

  private String name;
  private EventType type;
  private EventSource source;
  private EventState state;
  private ApartmentDto apartment;
  private Instant startDate;
  private Instant endDate;

  public EventDto(Event event) {
    if (event == null) {
      return;
    }
    BeanUtils.copyProperties(event, this, "apartment");
    if (event.getApartment() != null) {
      this.apartment = new ApartmentDto(event.getApartment());
    }
  }
}
