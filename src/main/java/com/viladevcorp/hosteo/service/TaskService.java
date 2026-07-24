package com.viladevcorp.hosteo.service;

import com.viladevcorp.hosteo.model.Apartment;
import com.viladevcorp.hosteo.model.PageMetadata;
import com.viladevcorp.hosteo.model.Task;
import com.viladevcorp.hosteo.model.forms.TaskCreateForm;
import com.viladevcorp.hosteo.model.forms.TaskSearchForm;
import com.viladevcorp.hosteo.model.forms.TaskUpdateForm;
import com.viladevcorp.hosteo.model.types.TaskType;
import com.viladevcorp.hosteo.repository.ApartmentRepository;
import com.viladevcorp.hosteo.repository.TaskRepository;
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
public class TaskService {

  private final TaskRepository taskRepository;
  private final WorkflowService workflowService;
  private final ApartmentRepository apartmentRepository;

  @Autowired
  public TaskService(
      TaskRepository taskRepository,
      WorkflowService workflowService,
      ApartmentRepository apartmentRepository) {
    this.taskRepository = taskRepository;
    this.workflowService = workflowService;
    this.apartmentRepository = apartmentRepository;
  }

  public Task createTask(TaskCreateForm form) throws EntityNotFoundException {

    Optional<Apartment> apartmentOpt =
        apartmentRepository.findById(form.getApartmentId(), AuthUtils.getUsername());
    if (apartmentOpt.isEmpty()) {
      throw new EntityNotFoundException("Apartment not found with id: " + form.getApartmentId());
    }
    Apartment apartment = apartmentOpt.get();
    Task task =
        Task.builder()
            .name(form.getName())
            .category(form.getCategory())
            .duration(form.getDuration())
            .type(form.getType())
            .apartment(apartment)
            .steps(form.getSteps())
            .build();

    apartment.addTask(task);
    workflowService.calculateApartmentState(apartment.getId());
    return task;
  }

  public Task updateTask(TaskUpdateForm form) throws EntityNotFoundException {
    Task task = getTaskById(form.getId());
    TaskType oldTaskType = task.getType();
    BeanUtils.copyProperties(form, task, "id");
    task = taskRepository.save(task);
    if (oldTaskType != task.getType()) {
      workflowService.calculateApartmentState(task.getApartment().getId());
    }
    return task;
  }

  public Task getTaskById(UUID id) throws EntityNotFoundException {
    Optional<Task> resultOpt = taskRepository.findById(id, AuthUtils.getUsername());
    if (resultOpt.isEmpty()) {
      throw new EntityNotFoundException("Task not found with id: " + id);
    } else {
      return resultOpt.get();
    }
  }

  public List<Task> findTasks(TaskSearchForm form) {
    String name =
        form.getName() == null || form.getName().isEmpty()
            ? null
            : "%" + form.getName().toLowerCase() + "%";

    PageRequest pageRequest =
        ServiceUtils.createPageRequest(form.getPageNumber(), form.getPageSize());
    return taskRepository.advancedSearch(AuthUtils.getUsername(), name, null, pageRequest);
  }

  public PageMetadata getTasksMetadata(TaskSearchForm form) {
    String name =
        form.getName() == null || form.getName().isEmpty()
            ? null
            : "%" + form.getName().toLowerCase() + "%";
    int totalRows = taskRepository.advancedCount(AuthUtils.getUsername(), name, null);
    int totalPages = ServiceUtils.calculateTotalPages(form.getPageSize(), totalRows);
    return new PageMetadata(totalPages, totalRows);
  }

  public void deleteTask(UUID id) throws EntityNotFoundException {
    Task task = getTaskById(id);
    Apartment apartment = task.getApartment();
    apartment.removeTask(task);
    taskRepository.delete(task);
    workflowService.calculateApartmentState(apartment.getId());
  }
}
