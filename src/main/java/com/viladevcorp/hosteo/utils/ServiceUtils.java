package com.viladevcorp.hosteo.utils;

import com.viladevcorp.hosteo.exceptions.InvalidDateRangeException;
import com.viladevcorp.hosteo.model.Assignment;
import com.viladevcorp.hosteo.model.Event;
import com.viladevcorp.hosteo.repository.AssignmentRepository;
import com.viladevcorp.hosteo.repository.EventRepository;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.UUID;

@Slf4j
public class ServiceUtils {
  public static int calculateTotalPages(int pageSize, long totalRows) {
    return pageSize > 0 ? ((Double) Math.ceil((double) totalRows / pageSize)).intValue() : 1;
  }

  public static void validateDateRange(Instant startDate, Instant endDate)
      throws InvalidDateRangeException {
    if (startDate == null || endDate == null || !endDate.isAfter(startDate)) {
      throw new InvalidDateRangeException("The end date must be after the start date.");
    }
  }

  public static PageRequest createPageRequest(int pageNumber, int pageSize) {
    if (pageSize > 0) {
      pageNumber = Math.max(0, pageNumber);
      return PageRequest.of(pageNumber, pageSize);
    } else {
      return null;
    }
  }

  public static Pair<Event, Assignment> getScheduleConflicts(
      EventRepository eventRepository,
      AssignmentRepository assignmentRepository,
      UUID apartmentId,
      Instant startDate,
      Instant endDate,
      UUID excludeEventId,
      UUID excludeAssignmentId) {
    Event eventConflict =
        eventRepository
            .findEventsBetween(
                AuthUtils.getUsername(), apartmentId, startDate, endDate, excludeEventId)
            .stream()
            .findFirst()
            .orElse(null);
    Assignment assignmentConflict =
        assignmentRepository
            .findAssignmentsBetween(
                AuthUtils.getUsername(), apartmentId, startDate, endDate, excludeAssignmentId)
            .stream()
            .findFirst()
            .orElse(null);
    return new Pair<Event, Assignment>(eventConflict, assignmentConflict);
  }
}
