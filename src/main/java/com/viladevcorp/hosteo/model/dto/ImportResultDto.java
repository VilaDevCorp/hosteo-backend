package com.viladevcorp.hosteo.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ImportResultDto {

  private int successCount;
  private List<FailedImportedEventDto> failedEvents;

  public ImportResultDto(int successCount, List<FailedImportedEventDto> failedEvents) {
    this.successCount = successCount;
    this.failedEvents = failedEvents;
  }
}