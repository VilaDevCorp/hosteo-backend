package com.viladevcorp.hosteo.controller;

import com.viladevcorp.hosteo.model.Page;
import com.viladevcorp.hosteo.model.PageMetadata;
import com.viladevcorp.hosteo.model.Worker;
import com.viladevcorp.hosteo.model.dto.WorkerDto;
import com.viladevcorp.hosteo.model.forms.WorkerCreateForm;
import com.viladevcorp.hosteo.model.forms.WorkerSearchForm;
import com.viladevcorp.hosteo.model.forms.WorkerUpdateForm;
import com.viladevcorp.hosteo.service.WorkerService;
import com.viladevcorp.hosteo.utils.ApiResponse;
import com.viladevcorp.hosteo.utils.ValidationUtils;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import javax.management.InstanceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
public class WorkerController {

  private final WorkerService workerService;

  @Autowired
  public WorkerController(WorkerService workerService) {
    this.workerService = workerService;
  }

  @PostMapping("/worker")
  public ResponseEntity<ApiResponse<WorkerDto>> createWorker(
      @Valid @RequestBody WorkerCreateForm form, BindingResult bindingResult) {
    log.info("[WorkerController.createWorker] - Creating worker");
    ResponseEntity<ApiResponse<WorkerDto>> validationResponse =
        ValidationUtils.handleFormValidation(bindingResult);
    if (validationResponse != null) {
      return validationResponse;
    }
    Worker worker = workerService.createWorker(form);
    log.info("[WorkerController.createWorker] - Worker created");
    return ResponseEntity.ok().body(new ApiResponse<>(new WorkerDto(worker)));
  }

  @PutMapping("/worker")
  public ResponseEntity<ApiResponse<WorkerDto>> updateWorker(
      @Valid @RequestBody WorkerUpdateForm form, BindingResult bindingResult) {
    log.info("[WorkerController.updateWorker] - Updating worker");
    ResponseEntity<ApiResponse<WorkerDto>> validationResponse =
        ValidationUtils.handleFormValidation(bindingResult);
    if (validationResponse != null) {
      return validationResponse;
    }
    Worker worker;
    try {
      worker = workerService.updateWorker(form);
      log.info("[WorkerController.updateWorker] - Worker updated");
      return ResponseEntity.ok().body(new ApiResponse<>(new WorkerDto(worker)));
    } catch (InstanceNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ApiResponse<>(null, e.getMessage()));
    }
  }

  @GetMapping("/worker/{id}")
  public ResponseEntity<ApiResponse<WorkerDto>> getWorker(@PathVariable UUID id) {
    log.info("[WorkerController.getWorker] - Fetching worker with id: {}", id);
    Worker worker;
    try {
      worker = workerService.getWorkerById(id);
      log.info("[WorkerController.getWorker] - Worker fetched");
      return ResponseEntity.ok().body(new ApiResponse<>(new WorkerDto(worker)));
    } catch (InstanceNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ApiResponse<>(null, e.getMessage()));
    }
  }

  @PostMapping("/worker/search")
  public ResponseEntity<ApiResponse<Page<WorkerDto>>> searchWorkers(
      @RequestBody WorkerSearchForm form) {
    log.info("[WorkerController.searchWorkers] - Searching workers");
    List<Worker> workers = workerService.findWorkers(form);
    PageMetadata pageMetadata = workerService.getWorkersMetadata(form);
    Page<WorkerDto> page =
        new Page<>(
            workers.stream().map(WorkerDto::new).toList(),
            pageMetadata.getTotalPages(),
            pageMetadata.getTotalRows());
    log.info("[WorkerController.searchWorkers] - Found {} workers", workers.size());
    return ResponseEntity.ok().body(new ApiResponse<>(page));
  }

  @DeleteMapping("/worker/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteWorker(@PathVariable UUID id) {
    log.info("[WorkerController.deleteWorker] - Deleting worker with id: {}", id);
    try {
      workerService.deleteWorker(id);
      log.info("[WorkerController.deleteWorker] - Worker deleted");
      return ResponseEntity.ok().body(new ApiResponse<>(null, "Worker deleted successfully."));
    } catch (InstanceNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ApiResponse<>(null, e.getMessage()));
    }
  }
}
