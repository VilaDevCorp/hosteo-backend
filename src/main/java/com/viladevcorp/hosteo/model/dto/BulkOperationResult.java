package com.viladevcorp.hosteo.model.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BulkOperationResult<T> {

  private int successCount;
  private List<T> errors = new ArrayList<>();

  public BulkOperationResult(int successCount, List<T> errors) {
    this.successCount = successCount;
    this.errors = errors == null ? new ArrayList<>() : errors;
  }
}