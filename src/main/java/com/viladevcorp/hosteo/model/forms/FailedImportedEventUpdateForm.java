package com.viladevcorp.hosteo.model.forms;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FailedImportedEventUpdateForm {

  private UUID apartmentId;

  private String name;

  private Instant startDate;

  private Instant endDate;
}