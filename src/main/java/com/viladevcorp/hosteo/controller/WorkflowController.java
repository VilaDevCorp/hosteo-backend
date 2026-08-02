package com.viladevcorp.hosteo.controller;

import com.viladevcorp.hosteo.model.SchedulerInfo;
import com.viladevcorp.hosteo.service.WorkflowService;
import com.viladevcorp.hosteo.utils.ApiResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
public class WorkflowController {

  private final WorkflowService workflowService;

  @Autowired
  public WorkflowController(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  @GetMapping("/scheduler/{startDateStr}")
  public ResponseEntity<ApiResponse<SchedulerInfo>> getSchedulerInfo(
      @PathVariable String startDateStr) {
    log.info(
        "[WorkflowController.getSchedulerInfo] - Getting scheduler info for date: {}",
        startDateStr);
    // We use a date formatter
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    Instant startDate =
        LocalDate.parse(startDateStr, formatter).atStartOfDay().toInstant(ZoneOffset.UTC);
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
}
