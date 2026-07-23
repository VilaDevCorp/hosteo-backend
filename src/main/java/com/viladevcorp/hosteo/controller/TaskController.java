package com.viladevcorp.hosteo.controller;

import com.viladevcorp.hosteo.model.Page;
import com.viladevcorp.hosteo.model.PageMetadata;
import com.viladevcorp.hosteo.model.Task;
import com.viladevcorp.hosteo.model.dto.TaskDto;
import com.viladevcorp.hosteo.model.forms.TaskCreateForm;
import com.viladevcorp.hosteo.model.forms.TaskSearchForm;
import com.viladevcorp.hosteo.model.forms.TaskUpdateForm;
import com.viladevcorp.hosteo.service.TaskService;
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
public class TaskController {

  private final TaskService taskService;

  @Autowired
  public TaskController(TaskService taskService) {
    this.taskService = taskService;
  }

  @PostMapping("/task")
  public ResponseEntity<ApiResponse<TaskDto>> createTask(
      @Valid @RequestBody TaskCreateForm form, BindingResult bindingResult) {
    log.info("[TaskController.createTask] - Creating task");
    ResponseEntity<ApiResponse<TaskDto>> validationResponse =
        ValidationUtils.handleFormValidation(bindingResult);
    if (validationResponse != null) {
      return validationResponse;
    }

    try {
      Task task = taskService.createTask(form);
      log.info("[TaskController.createTask] - Task created successfully");
      return ResponseEntity.ok().body(new ApiResponse<>(new TaskDto(task)));
    } catch (InstanceNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ApiResponse<>(null, e.getMessage()));
    }
  }

  @PutMapping("/task")
  public ResponseEntity<ApiResponse<TaskDto>> updateTask(
      @Valid @RequestBody TaskUpdateForm form, BindingResult bindingResult) {
    log.info("[TaskController.updateTask] - Updating task");

    ResponseEntity<ApiResponse<TaskDto>> validationResponse =
        ValidationUtils.handleFormValidation(bindingResult);
    if (validationResponse != null) {
      return validationResponse;
    }

    try {
      Task task = taskService.updateTask(form);
      log.info("[TaskController.updateTask] - Task updated successfully");
      return ResponseEntity.ok().body(new ApiResponse<>(new TaskDto(task)));
    } catch (InstanceNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ApiResponse<>(null, e.getMessage()));
    }
  }

  @GetMapping("/task/{id}")
  public ResponseEntity<ApiResponse<TaskDto>> getTask(@PathVariable UUID id) {
    log.info("[TaskController.getTask] - Fetching task with id: {}", id);

    try {
      Task task = taskService.getTaskById(id);
      log.info("[TaskController.getTask] - Task found successfully");
      return ResponseEntity.ok().body(new ApiResponse<>(new TaskDto(task)));
    } catch (InstanceNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ApiResponse<>(null, e.getMessage()));
    }
  }

  @PostMapping("/task/search")
  public ResponseEntity<ApiResponse<Page<TaskDto>>> searchTasks(@RequestBody TaskSearchForm form) {
    log.info("[TaskController.searchTasks] - Searching tasks");

    List<Task> tasks = taskService.findTasks(form);
    PageMetadata pageMetadata = taskService.getTasksMetadata(form);
    Page<TaskDto> page =
        new Page<>(
            tasks.stream().map(TaskDto::new).toList(),
            pageMetadata.getTotalPages(),
            pageMetadata.getTotalRows());

    log.info("[TaskController.searchTasks] - Found {} tasks", tasks.size());
    return ResponseEntity.ok().body(new ApiResponse<>(page));
  }

  @DeleteMapping("/task/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable UUID id) {
    log.info("[TaskController.deleteTask] - Deleting task with id: {}", id);
    try {
      taskService.deleteTask(id);
      log.info("[TaskController.deleteTask] - Task deleted successfully");
      return ResponseEntity.ok().body(new ApiResponse<>(null, "Task deleted successfully."));
    } catch (InstanceNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ApiResponse<>(null, e.getMessage()));
    }
  }
}
