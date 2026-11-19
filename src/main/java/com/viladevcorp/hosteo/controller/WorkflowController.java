package com.viladevcorp.hosteo.controller;

import com.viladevcorp.hosteo.model.SchedulerInfo;
import com.viladevcorp.hosteo.model.dto.AlertInfo;
import com.viladevcorp.hosteo.model.dto.WorkerAssignmentDto;
import com.viladevcorp.hosteo.service.WorkflowService;
import com.viladevcorp.hosteo.utils.ApiResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
public class WorkflowController {

  private static final DateTimeFormatter SCHEDULER_DATE_FORMATTER =
      DateTimeFormatter.ofPattern("dd-MM-yyyy");

  private final WorkflowService workflowService;

  @Autowired
  public WorkflowController(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  @GetMapping("/alerts")
  public ResponseEntity<ApiResponse<AlertInfo>> getAlerts() {
    log.info("[WorkflowController.getAlerts] - Getting alerts");
    AlertInfo alerts = workflowService.getAlerts();
    log.info(
        "[WorkflowController.getAlerts] - Retrieved {} alerts ({} red, {} yellow)",
        alerts.getAlerts().size(),
        alerts.getNRedAlerts(),
        alerts.getNYellowAlerts());
    return ResponseEntity.ok().body(new ApiResponse<>(alerts));
  }

  @GetMapping("/scheduler/{startDateStr}")
  public ResponseEntity<ApiResponse<SchedulerInfo>> getSchedulerInfo(
      @PathVariable String startDateStr) {
    log.info(
        "[WorkflowController.getSchedulerInfo] - Getting scheduler info for date: {}",
        startDateStr);
    Instant startDate =
        LocalDate.parse(startDateStr, SCHEDULER_DATE_FORMATTER)
            .atStartOfDay()
            .toInstant(ZoneOffset.UTC);
    Instant endDate = startDate.plusSeconds(7 * 24 * 60 * 60); // One week later
    try {
      SchedulerInfo info = workflowService.getSchedulerInfo(startDate, endDate);
      log.info("[WorkflowController.getSchedulerInfo] - Scheduler info retrieved successfully");
      return ResponseEntity.ok().body(new ApiResponse<>(info));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ApiResponse<>(null, e.getMessage()));
    }
  }

  @GetMapping("/scheduler/assignments")
  public ResponseEntity<ApiResponse<List<WorkerAssignmentDto>>> getAssignmentsByWorker(
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate) {
    log.info(
        "[WorkflowController.getAssignmentsByWorker] - Getting assignments from {} to {}",
        startDate,
        endDate);

    if (startDate == null || endDate == null) {
      return ResponseEntity.badRequest()
          .body(new ApiResponse<>(null, "Both startDate and endDate are required."));
    }

    Instant start;
    Instant end;
    try {
      start =
          LocalDate.parse(startDate, SCHEDULER_DATE_FORMATTER)
              .atStartOfDay()
              .toInstant(ZoneOffset.UTC);
      end =
          LocalDate.parse(endDate, SCHEDULER_DATE_FORMATTER)
              .atStartOfDay()
              .toInstant(ZoneOffset.UTC);
    } catch (DateTimeParseException e) {
      return ResponseEntity.badRequest()
          .body(new ApiResponse<>(null, "Dates must use the format dd-MM-yyyy."));
    }

    if (start.isAfter(end)) {
      return ResponseEntity.badRequest()
          .body(new ApiResponse<>(null, "startDate must not be after endDate."));
    }

    List<WorkerAssignmentDto> assignments = workflowService.getAssignmentsByWorker(start, end);
    log.info(
        "[WorkflowController.getAssignmentsByWorker] - Retrieved assignments for {} workers",
        assignments.size());
    return ResponseEntity.ok().body(new ApiResponse<>(assignments));
  }
}
