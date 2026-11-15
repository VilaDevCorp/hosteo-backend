package com.viladevcorp.hosteo.model.forms;

import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class TransactionSearchForm {

  private UUID workerId;

  private Instant startDate;

  private Instant endDate;

  private Boolean isPayment;

  private int pageNumber;

  private int pageSize;
}
