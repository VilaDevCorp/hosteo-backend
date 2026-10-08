package com.viladevcorp.hosteo.controller;

import com.viladevcorp.hosteo.model.FailedImportedEvent;
import com.viladevcorp.hosteo.model.dto.ImportResultDto;
import com.viladevcorp.hosteo.model.dto.FailedImportedEventDto;
import com.viladevcorp.hosteo.model.forms.FailedImportedEventUpdateForm;
import com.viladevcorp.hosteo.model.types.EventSource;
import com.viladevcorp.hosteo.service.ImportService;
import com.viladevcorp.hosteo.utils.ApiResponse;
import com.viladevcorp.hosteo.utils.CodeErrors;
import jakarta.persistence.EntityNotFoundException;
import java.io.File;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/imported-events")
public class FailedImportedEventController {

  private final ImportService importService;

  @Autowired
  public FailedImportedEventController(ImportService importService) {
    this.importService = importService;
  }

  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<ImportResultDto>> upload(
      @RequestParam("file") MultipartFile multipartFile, @RequestParam("source") String source) {
    log.info("[FailedImportedEventController.upload] - Importing file with source: {}", source);
    File tempFile = null;
    try {
      tempFile = File.createTempFile("uploaded", ".csv");
      multipartFile.transferTo(tempFile);
      ImportResultDto result = importService.upload(tempFile, EventSource.valueOf(source));
      log.info(
          "[FailedImportedEventController.upload] - Import finished, successes: {}, failures: {}",
          result.getSuccessCount(),
          result.getFailedEvents().size());
      return ResponseEntity.ok().body(new ApiResponse<>(result));
    } catch (Exception e) {
      log.error(
          "[FailedImportedEventController.upload] - Error importing file: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ApiResponse<>(null, "An unexpected error occurred."));
    } finally {
      if (tempFile != null && tempFile.exists()) {
        tempFile.delete();
      }
    }
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<FailedImportedEventDto>>> list() {
    log.info("[FailedImportedEventController.list] - Listing imported events");
    List<FailedImportedEventDto> events =
        importService.getFailedImportedEvents().stream().map(FailedImportedEventDto::new).toList();
    return ResponseEntity.ok().body(new ApiResponse<>(events));
  }

  @PatchMapping("/{id}")
  public ResponseEntity<ApiResponse<FailedImportedEventDto>> update(
      @PathVariable UUID id, @RequestBody FailedImportedEventUpdateForm form) {
    log.info("[FailedImportedEventController.update] - Updating imported event with id: {}", id);
    try {
      FailedImportedEvent updatedEvent = importService.updateFailedImportedEvent(id, form);
      return ResponseEntity.ok().body(new ApiResponse<>(new FailedImportedEventDto(updatedEvent)));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ApiResponse<>(null, e.getMessage()));
    }
  }

  @PostMapping("/retry")
  public ResponseEntity<ApiResponse<ImportResultDto>> retry() {
    log.info("[FailedImportedEventController.retry] - Retrying imported events");
    try {
      ImportResultDto result = importService.retry();
      log.info(
          "[FailedImportedEventController.retry] - Retry finished, successes: {}, failures: {}",
          result.getSuccessCount(),
          result.getFailedEvents().size());
      return ResponseEntity.ok().body(new ApiResponse<>(result));
    } catch (Exception e) {
      log.error(
          "[FailedImportedEventController.retry] - Error retrying imports: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new ApiResponse<>(null, "An unexpected error occurred."));
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> dismiss(@PathVariable UUID id) {
    log.info("[FailedImportedEventController.dismiss] - Dismissing imported event with id: {}", id);
    importService.dismiss(id);
    return ResponseEntity.ok().body(new ApiResponse<>(null, "Import dismissed successfully."));
  }

  @DeleteMapping
  public ResponseEntity<ApiResponse<Void>> dismissAll() {
    log.info("[FailedImportedEventController.dismissAll] - Dismissing all imported events");
    importService.dismissAll();
    return ResponseEntity.ok().body(new ApiResponse<>(null, "Imports dismissed successfully"));
  }
}
