package com.viladevcorp.hosteo.service;

import com.viladevcorp.hosteo.exceptions.EntityHasDependenciesException;
import com.viladevcorp.hosteo.model.PageMetadata;
import com.viladevcorp.hosteo.model.Worker;
import com.viladevcorp.hosteo.model.forms.WorkerCreateForm;
import com.viladevcorp.hosteo.model.forms.WorkerSearchForm;
import com.viladevcorp.hosteo.model.forms.WorkerUpdateForm;
import com.viladevcorp.hosteo.model.types.AssignmentState;
import com.viladevcorp.hosteo.repository.AssignmentRepository;
import com.viladevcorp.hosteo.repository.WorkerRepository;
import com.viladevcorp.hosteo.utils.AuthUtils;
import com.viladevcorp.hosteo.utils.ServiceUtils;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class WorkerService {

  private final WorkerRepository workerRepository;
  private final AssignmentRepository assignmentRepository;

  @Autowired
  public WorkerService(WorkerRepository workerRepository, AssignmentRepository assignmentRepository) {
    this.workerRepository = workerRepository;
    this.assignmentRepository = assignmentRepository;
  }

  public Worker createWorker(WorkerCreateForm form) {
    Worker worker =
        Worker.builder()
            .name(form.getName())
            .language(form.getLanguage())
            .visible(form.isVisible())
            .build();
    return workerRepository.save(worker);
  }

  public Worker updateWorker(WorkerUpdateForm form) throws EntityNotFoundException {
    Worker worker = getWorkerById(form.getId());
    BeanUtils.copyProperties(form, worker, "id");
    return workerRepository.save(worker);
  }

  public Worker getWorkerById(UUID id) throws EntityNotFoundException {
    Optional<Worker> result = workerRepository.findById(id, AuthUtils.getUsername());
    if (result.isEmpty()) {
      throw new EntityNotFoundException("Worker not found with id: " + id);
    } else {
      return result.get();
    }
  }

  public List<Worker> findWorkers(WorkerSearchForm form) {
    String workerName =
        form.getName() == null || form.getName().isEmpty()
            ? null
            : "%" + form.getName().toLowerCase() + "%";

    PageRequest pageRequest =
        ServiceUtils.createPageRequest(form.getPageNumber(), form.getPageSize());
    Boolean visible = form.getVisible() == null ? Boolean.TRUE : form.getVisible();
    return workerRepository.advancedSearch(
        AuthUtils.getUsername(), workerName, visible, pageRequest);
  }

  public PageMetadata getWorkersMetadata(WorkerSearchForm form) {
    String workerName =
        form.getName() == null || form.getName().isEmpty()
            ? null
            : "%" + form.getName().toLowerCase() + "%";
    Boolean visible = form.getVisible() == null ? Boolean.TRUE : form.getVisible();
    int totalRows =
        workerRepository.advancedCount(AuthUtils.getUsername(), workerName, visible);
    int totalPages = ServiceUtils.calculateTotalPages(form.getPageSize(), totalRows);
    return new PageMetadata(totalPages, totalRows);
  }

  public void hideWorker(UUID id) throws EntityNotFoundException, EntityHasDependenciesException {
    Worker worker = getWorkerById(id);
    if (assignmentRepository.existsByWorkerIdAndStateAndCreatedByUsername(
        worker.getId(), AssignmentState.PENDING, AuthUtils.getUsername())) {
      throw new EntityHasDependenciesException(
          "Cannot hide worker with pending assignments.");
    }
    worker.setVisible(false);
    workerRepository.save(worker);
  }

  public void unhideWorker(UUID id) throws EntityNotFoundException {
    Worker worker = getWorkerById(id);
    worker.setVisible(true);
    workerRepository.save(worker);
  }

  public void deleteWorker(UUID id)
      throws EntityNotFoundException, EntityHasDependenciesException {
    Worker worker = getWorkerById(id);
    if (assignmentRepository.existsByWorkerIdAndCreatedByUsername(
        worker.getId(), AuthUtils.getUsername())) {
      throw new EntityHasDependenciesException(
          "Cannot delete worker with assignments.");
    }
    workerRepository.delete(worker);
  }
}
