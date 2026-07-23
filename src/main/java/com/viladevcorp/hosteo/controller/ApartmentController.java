package com.viladevcorp.hosteo.controller;

import com.viladevcorp.hosteo.model.Apartment;
import com.viladevcorp.hosteo.model.Page;
import com.viladevcorp.hosteo.model.PageMetadata;
import com.viladevcorp.hosteo.model.dto.ApartmentDto;
import com.viladevcorp.hosteo.model.dto.ApartmentWithTasksDto;
import com.viladevcorp.hosteo.model.forms.ApartmentCreateForm;
import com.viladevcorp.hosteo.model.forms.ApartmentSearchForm;
import com.viladevcorp.hosteo.model.forms.ApartmentUpdateForm;
import com.viladevcorp.hosteo.service.ApartmentService;
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
public class ApartmentController {

  private final ApartmentService apartmentService;

  @Autowired
  public ApartmentController(ApartmentService apartmentService) {
    this.apartmentService = apartmentService;
  }

  @PostMapping("/apartment")
  public ResponseEntity<ApiResponse<ApartmentWithTasksDto>> createApartment(
      @Valid @RequestBody ApartmentCreateForm form, BindingResult bindingResult) {
    log.info("[ApartmentController.createApartment] - Creating apartment");
    ResponseEntity<ApiResponse<ApartmentWithTasksDto>> validationResponse =
        ValidationUtils.handleFormValidation(bindingResult);
    if (validationResponse != null) {
      return validationResponse;
    }
    Apartment apartment = apartmentService.createApartment(form);
    log.info("[ApartmentController.createApartment] - Apartment created");
    return ResponseEntity.ok().body(new ApiResponse<>(new ApartmentWithTasksDto(apartment)));
  }

  @PutMapping("/apartment")
  public ResponseEntity<ApiResponse<ApartmentWithTasksDto>> updateApartment(
      @Valid @RequestBody ApartmentUpdateForm form, BindingResult bindingResult) {
    log.info("[ApartmentController.updateApartment] - Updating apartment");
    ResponseEntity<ApiResponse<ApartmentWithTasksDto>> validationResponse =
        ValidationUtils.handleFormValidation(bindingResult);
    if (validationResponse != null) {
      return validationResponse;
    }
    Apartment apartment;
    try {
      apartment = apartmentService.updateApartment(form);
      log.info("[ApartmentController.updateApartment] - Apartment updated");
      return ResponseEntity.ok().body(new ApiResponse<>(new ApartmentWithTasksDto(apartment)));
    } catch (InstanceNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ApiResponse<>(null, e.getMessage()));
    }
  }

  @GetMapping("/apartment/{id}")
  public ResponseEntity<ApiResponse<ApartmentWithTasksDto>> getApartment(@PathVariable UUID id) {
    log.info("[ApartmentController.getApartment] - Fetching apartment with id: {}", id);
    Apartment apartment;
    try {
      apartment = apartmentService.getApartmentById(id);
      log.info("[ApartmentController.getApartment] - Apartment fetched");
      return ResponseEntity.ok().body(new ApiResponse<>(new ApartmentWithTasksDto(apartment)));
    } catch (InstanceNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ApiResponse<>(null, e.getMessage()));
    }
  }

  @PostMapping("/apartment/search")
  public ResponseEntity<ApiResponse<Page<ApartmentDto>>> searchApartments(
      @RequestBody ApartmentSearchForm form) {
    log.info("[ApartmentController.searchApartments] - Searching apartments");
    List<Apartment> apartments = apartmentService.findApartments(form);
    PageMetadata pageMetadata = apartmentService.getApartmentsMetadata(form);
    Page<ApartmentDto> page =
        new Page<>(
            apartments.stream().map(ApartmentDto::new).toList(),
            pageMetadata.getTotalPages(),
            pageMetadata.getTotalRows());
    log.info("[ApartmentController.searchApartments] - Found {} apartments", apartments.size());
    return ResponseEntity.ok().body(new ApiResponse<>(page));
  }

  @DeleteMapping("/apartment/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteApartment(@PathVariable UUID id) {
    log.info("[ApartmentController.deleteApartment] - Deleting apartment with id: {}", id);
    try {
      apartmentService.deleteApartment(id);
      log.info("[ApartmentController.deleteApartment] - Apartment deleted");
      return ResponseEntity.ok().body(new ApiResponse<>(null, "Apartment deleted successfully."));
    } catch (InstanceNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ApiResponse<>(null, e.getMessage()));
    }
  }
}
