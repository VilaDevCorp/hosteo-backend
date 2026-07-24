package com.viladevcorp.hosteo.service;

import com.viladevcorp.hosteo.model.PageMetadata;
import com.viladevcorp.hosteo.model.Worker;
import com.viladevcorp.hosteo.model.forms.WorkerCreateForm;
import com.viladevcorp.hosteo.model.forms.WorkerSearchForm;
import com.viladevcorp.hosteo.model.forms.WorkerUpdateForm;
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

  @Autowired
  public WorkerService(WorkerRepository workerRepository) {
    this.workerRepository = workerRepository;
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
    return workerRepository.advancedSearch(
        AuthUtils.getUsername(), workerName, form.getVisible(), pageRequest);
  }

  public PageMetadata getWorkersMetadata(WorkerSearchForm form) {
    String workerName =
        form.getName() == null || form.getName().isEmpty()
            ? null
            : "%" + form.getName().toLowerCase() + "%";
    int totalRows =
        workerRepository.advancedCount(AuthUtils.getUsername(), workerName, form.getVisible());
    int totalPages = ServiceUtils.calculateTotalPages(form.getPageSize(), totalRows);
    return new PageMetadata(totalPages, totalRows);
  }

  public void deleteWorker(UUID id) throws EntityNotFoundException {
    Worker worker = getWorkerById(id);
    workerRepository.delete(worker);
  }
}
