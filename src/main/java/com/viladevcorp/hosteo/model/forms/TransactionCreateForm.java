package com.viladevcorp.hosteo.model.forms;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TransactionCreateForm {

  @NotNull private UUID workerId;

  @Positive private double amount;
}
