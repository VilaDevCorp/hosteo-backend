package com.viladevcorp.hosteo.model.dto;

import com.viladevcorp.hosteo.model.Assignment;
import com.viladevcorp.hosteo.model.Event;
import com.viladevcorp.hosteo.model.types.EventSource;
import com.viladevcorp.hosteo.model.types.EventState;

import java.time.Instant;
import java.util.*;

import com.viladevcorp.hosteo.model.types.EventType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
@NoArgsConstructor
public class EventWithAssignmentsDto extends BaseEntityDto {

  public EventWithAssignmentsDto(Event event) {
    if (event == null) {
      return;
    }
    BeanUtils.copyProperties(event, this, "apartment", "assignments");
    this.apartment = new ApartmentWithTasksDto(event.getApartment());

    List<AssignmentDto> assignmentsDto = new ArrayList<>();
    event
        .getAssignments()
        .forEach(
            assignment -> {
              this.assignments.add(new AssignmentDto(assignment));
            });
    this.assignments =
        this.assignments.stream()
            .sorted(Comparator.comparing(AssignmentDto::getStartDate).reversed())
            .toList();
  }

  private ApartmentWithTasksDto apartment;

  private Instant startDate;

  private Instant endDate;

  private String name;

  private EventState state;

  private EventSource source;

  private EventType type;

  private List<AssignmentDto> assignments = new ArrayList<>();
}
