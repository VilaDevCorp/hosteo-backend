package com.viladevcorp.hosteo.model.dto;

import com.viladevcorp.hosteo.model.Event;
import com.viladevcorp.hosteo.model.types.*;

import java.time.Instant;
import java.util.*;

import com.viladevcorp.hosteo.utils.ImageUrlResolver;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
@NoArgsConstructor
public class EventSchedulerDto extends BaseEntityDto {

  public EventSchedulerDto(Event event) {
    if (event == null) {
      return;
    }
    this.apartmentName = event.getApartment().getName();
    this.apartmentImageUrl =
        ImageUrlResolver.resolve(
            ImageType.APARTMENT,
            event.getApartment().getId().toString(),
            event.getApartment().isHasImage());
    this.frozen = !event.getApartment().isVisible();
    BeanUtils.copyProperties(event, this);
  }

  private UUID id;

  private EventType type;

  private EventState state;

  private Instant startDate;

  private Instant endDate;

  private String name;

  private String apartmentName;

  private String apartmentImageUrl;

  private EventSource source;

  private int nMandatoryAssignedTasks;
  private int nExtraAssignedTasks;
  private List<TaskDto> mandatoryUnassignedTasks = new ArrayList<>();

  private int nCompletedAssignments;
  private List<AssignmentDto> uncompletedAssignments;

  private Alert alert;

  private boolean overdue;

  /** True when the event belongs to a hidden apartment and must be treated as read-only. */
  private boolean frozen;
}
