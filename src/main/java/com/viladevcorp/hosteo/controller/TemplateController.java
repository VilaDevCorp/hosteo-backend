package com.viladevcorp.hosteo.controller;

import com.viladevcorp.hosteo.model.Page;
import com.viladevcorp.hosteo.model.PageMetadata;
import com.viladevcorp.hosteo.model.dto.TemplateDto;
import com.viladevcorp.hosteo.model.forms.TemplateCreateForm;
import com.viladevcorp.hosteo.model.forms.TemplateSearchForm;
import com.viladevcorp.hosteo.model.forms.TemplateUpdateForm;
import com.viladevcorp.hosteo.service.TemplateService;
import com.viladevcorp.hosteo.utils.ApiResponse;
import com.viladevcorp.hosteo.utils.ValidationUtils;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.EntityNotFoundException;
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
public class TemplateController {

  private final TemplateService templateService;

  @Autowired
  public TemplateController(TemplateService templateService) {
    this.templateService = templateService;
  }

  @PostMapping("/template")
  public ResponseEntity<ApiResponse<TemplateDto>> createTemplate(
      @Valid @RequestBody TemplateCreateForm form, BindingResult bindingResult) {
    log.info("[TemplateController.createTemplate] - Creating template");

    ResponseEntity<ApiResponse<TemplateDto>> validationResponse =
        ValidationUtils.handleFormValidation(bindingResult);
    if (validationResponse != null) {
      return validationResponse;
    }

    TemplateDto template = templateService.createTemplate(form);
    log.info("[TemplateController.createTemplate] - Template created successfully");
    return ResponseEntity.ok().body(new ApiResponse<>(template));
  }

  @PutMapping("/template")
  public ResponseEntity<ApiResponse<TemplateDto>> updateTemplate(
      @Valid @RequestBody TemplateUpdateForm form, BindingResult bindingResult) {
    log.info("[TemplateController.updateTemplate] - Updating template");

    ResponseEntity<ApiResponse<TemplateDto>> validationResponse =
        ValidationUtils.handleFormValidation(bindingResult);
    if (validationResponse != null) {
      return validationResponse;
    }

    try {
      TemplateDto template = templateService.updateTemplate(form);
      log.info("[TemplateController.updateTemplate] - Template updated successfully");
      return ResponseEntity.ok().body(new ApiResponse<>(template));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ApiResponse<>(null, e.getMessage()));
    }
  }

  @GetMapping("/template/{id}")
  public ResponseEntity<ApiResponse<TemplateDto>> getTemplate(@PathVariable UUID id) {
    log.info("[TemplateController.getTemplate] - Fetching template with id: {}", id);

    try {
      TemplateDto template = templateService.getTemplateById(id);
      log.info("[TemplateController.getTemplate] - Template found successfully");
      return ResponseEntity.ok().body(new ApiResponse<>(template));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ApiResponse<>(null, e.getMessage()));
    }
  }

  @PostMapping("/template/search")
  public ResponseEntity<ApiResponse<Page<TemplateDto>>> searchTemplates(
      @RequestBody TemplateSearchForm form) {
    log.info("[TemplateController.searchTemplates] - Searching templates");

    List<TemplateDto> templates = templateService.findTemplates(form);
    PageMetadata pageMetadata = templateService.getTemplatesMetadata(form);
    Page<TemplateDto> page =
        new Page<>(templates, pageMetadata.getTotalPages(), pageMetadata.getTotalRows());

    log.info("[TemplateController.searchTemplates] - Found {} templates", templates.size());
    return ResponseEntity.ok().body(new ApiResponse<>(page));
  }

  @DeleteMapping("/template/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteTemplate(@PathVariable UUID id) {
    log.info("[TemplateController.deleteTemplate] - Deleting template with id: {}", id);
    try {
      templateService.deleteTemplate(id);
      log.info("[TemplateController.deleteTemplate] - Template deleted successfully");
      return ResponseEntity.ok().body(new ApiResponse<>(null, "Template deleted successfully."));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ApiResponse<>(null, e.getMessage()));
    }
  }
}
