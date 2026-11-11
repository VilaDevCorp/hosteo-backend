package com.viladevcorp.hosteo.controller;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.viladevcorp.hosteo.exceptions.EmptyFormFieldsException;
import com.viladevcorp.hosteo.exceptions.FileTooBigException;
import com.viladevcorp.hosteo.exceptions.FileTypeNotSupportedException;
import com.viladevcorp.hosteo.exceptions.NotAllowedResourceException;
import com.viladevcorp.hosteo.utils.ApiResponse;

@ControllerAdvice
public class ControllerExceptionHandler {
  @ExceptionHandler(value = EntityNotFoundException.class)
  public ResponseEntity<ApiResponse<?>> instanceNotFound(Exception e) {
    String errorMessage = e.getMessage() == null ? "Resource not found" : e.getMessage();
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(null, errorMessage));
  }

  @ExceptionHandler(value = EmptyFormFieldsException.class)
  public ResponseEntity<ApiResponse<?>> emptyFormField(Exception e) {
    String errorMessage =
        e.getMessage() == null ? "There are mandatory fields that are empty" : e.getMessage();
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ApiResponse<>(null, errorMessage));
  }

  @ExceptionHandler(value = NotAllowedResourceException.class)
  public ResponseEntity<ApiResponse<?>> notAllowedResourceError(Exception e) {
    String errorMessage =
        e.getMessage() == null
            ? "The user doesn't have permissions to do this operation"
            : e.getMessage();

    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(null, errorMessage));
  }

  @ExceptionHandler(value = {FileTooBigException.class, MaxUploadSizeExceededException.class})
  public ResponseEntity<ApiResponse<?>> fileTooBig(Exception e) {
    String errorMessage = e.getMessage() == null ? "The file is too big" : e.getMessage();
    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
        .body(new ApiResponse<>(null, errorMessage));
  }

  @ExceptionHandler(value = FileTypeNotSupportedException.class)
  public ResponseEntity<ApiResponse<?>> fileTypeNotSupported(Exception e) {
    String errorMessage =
        e.getMessage() == null ? "The file type is not supported" : e.getMessage();
    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
        .body(new ApiResponse<>(null, errorMessage));
  }

  @ExceptionHandler(value = Exception.class)
  public ResponseEntity<ApiResponse<?>> internalError(Exception e) {
    String errorMessage =
        e.getMessage() == null ? "It has occur an internal error" : e.getMessage();
    return ResponseEntity.internalServerError().body(new ApiResponse<>(null, errorMessage));
  }
}
