package com.viladevcorp.hosteo.service;

import com.viladevcorp.hosteo.exceptions.EntityHasDependenciesException;
import com.viladevcorp.hosteo.model.Apartment;
import com.viladevcorp.hosteo.model.PageMetadata;
import com.viladevcorp.hosteo.model.forms.ApartmentCreateForm;
import com.viladevcorp.hosteo.model.forms.ApartmentSearchForm;
import com.viladevcorp.hosteo.model.forms.ApartmentUpdateForm;
import com.viladevcorp.hosteo.model.types.ApartmentState;
import com.viladevcorp.hosteo.model.types.AssignmentState;
import com.viladevcorp.hosteo.model.types.EventState;
import com.viladevcorp.hosteo.repository.ApartmentRepository;
import com.viladevcorp.hosteo.repository.AssignmentRepository;
import com.viladevcorp.hosteo.repository.EventRepository;
import com.viladevcorp.hosteo.repository.FailedImportedEventRepository;
import com.viladevcorp.hosteo.repository.TaskRepository;
import com.viladevcorp.hosteo.utils.AuthUtils;
import com.viladevcorp.hosteo.utils.ServiceUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ApartmentService {

  private final ApartmentRepository apartmentRepository;
  private final AssignmentRepository assignmentRepository;
  private final EventRepository eventRepository;
  private final TaskRepository taskRepository;
  private final FailedImportedEventRepository importedEventRepository;

  @Autowired
  public ApartmentService(
      ApartmentRepository apartmentRepository,
      AssignmentRepository assignmentRepository,
      EventRepository eventRepository,
      TaskRepository taskRepository,
      FailedImportedEventRepository importedEventRepository) {
    this.apartmentRepository = apartmentRepository;
    this.assignmentRepository = assignmentRepository;
    this.eventRepository = eventRepository;
    this.taskRepository = taskRepository;
    this.importedEventRepository = importedEventRepository;
  }

  public Apartment createApartment(ApartmentCreateForm form) {
    Apartment apartment =
        Apartment.builder()
            .name(form.getName())
            .airbnbId(form.getAirbnbId())
            .bookingId(form.getBookingId())
            .address(form.getAddress())
            .state(ApartmentState.READY)
            .visible(form.isVisible())
            .build();
    return apartmentRepository.save(apartment);
  }

  public Apartment updateApartment(ApartmentUpdateForm form) throws EntityNotFoundException {
    Apartment apartment = getApartmentById(form.getId());
    BeanUtils.copyProperties(form, apartment, "id");
    return apartmentRepository.save(apartment);
  }

  public Apartment getApartmentById(UUID id) throws EntityNotFoundException {
    Optional<Apartment> result = apartmentRepository.findById(id, AuthUtils.getUsername());
    if (result.isEmpty()) {
      throw new EntityNotFoundException("Apartment not found with id: " + id);
    } else {
      return result.get();
    }
  }

  public List<Apartment> findApartments(ApartmentSearchForm form) {
    String apartmentName =
        form.getName() == null || form.getName().isEmpty()
            ? null
            : "%" + form.getName().toLowerCase() + "%";

    PageRequest pageRequest =
        ServiceUtils.createPageRequest(form.getPageNumber(), form.getPageSize());
    Boolean visible = form.getVisible() == null ? Boolean.TRUE : form.getVisible();
    return apartmentRepository.advancedSearch(
        AuthUtils.getUsername(), apartmentName, form.getStates(), visible, pageRequest);
  }

  public PageMetadata getApartmentsMetadata(ApartmentSearchForm form) {
    String apartmentName =
        form.getName() == null || form.getName().isEmpty()
            ? null
            : "%" + form.getName().toLowerCase() + "%";
    Boolean visible = form.getVisible() == null ? Boolean.TRUE : form.getVisible();
    int totalRows =
        apartmentRepository.advancedCount(
            AuthUtils.getUsername(), apartmentName, form.getStates(), visible);
    int totalPages = ServiceUtils.calculateTotalPages(form.getPageSize(), totalRows);
    return new PageMetadata(totalPages, totalRows);
  }

  public void hideApartment(UUID id)
      throws EntityNotFoundException, EntityHasDependenciesException {
    Apartment apartment = getApartmentById(id);
    boolean hasActiveEvents =
        eventRepository.existsByApartmentIdAndStateIn(
            AuthUtils.getUsername(),
            apartment.getId(),
            Set.of(EventState.PENDING, EventState.IN_PROGRESS));
    if (hasActiveEvents) {
      throw new EntityHasDependenciesException(
          "Cannot hide apartment with pending or in-progress events.");
    }
    boolean hasPendingAssignments =
        assignmentRepository.existsByTaskApartmentIdAndStateAndCreatedByUsername(
            apartment.getId(), AssignmentState.PENDING, AuthUtils.getUsername());
    if (hasPendingAssignments) {
      throw new EntityHasDependenciesException(
          "Cannot hide apartment with pending assignments.");
    }
    apartment.setVisible(false);
    apartmentRepository.save(apartment);
  }

  public void unhideApartment(UUID id) throws EntityNotFoundException {
    Apartment apartment = getApartmentById(id);
    apartment.setVisible(true);
    apartmentRepository.save(apartment);
  }

  public void deleteApartment(UUID id) throws EntityNotFoundException {
    Apartment apartment = getApartmentById(id);
    String username = AuthUtils.getUsername();
    UUID apartmentId = apartment.getId();

    // Ordered subtree purge: assignments -> imported_events -> events -> tasks -> apartment.
    // DB has CASCADE for apartment->events / apartment->tasks / apartment->imported_events
    // and event->assignments, but explicit ordering avoids mixed CASCADE/RESTRICT issues.
    assignmentRepository.deleteByApartmentId(apartmentId, username);
    importedEventRepository.deleteByApartmentId(apartmentId, username);
    eventRepository.deleteByApartmentId(apartmentId, username);
    taskRepository.deleteByApartmentId(apartmentId, username);
    apartmentRepository.delete(apartment);
  }
}