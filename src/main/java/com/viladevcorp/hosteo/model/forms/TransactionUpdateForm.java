package com.viladevcorp.hosteo.model.forms;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TransactionUpdateForm {

  @Positive private double amount;
}
