package com.viladevcorp.hosteo.model.dto;

import com.viladevcorp.hosteo.model.Transaction;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
@NoArgsConstructor
public class TransactionDto extends BaseEntityDto {

  public TransactionDto(Transaction transaction) {
    if (transaction == null) {
      return;
    }
    BeanUtils.copyProperties(transaction, this, "worker", "assignment");
    this.worker = new WorkerDto(transaction.getWorker());
    if (transaction.getAssignment() != null) {
      this.assignment = new AssignmentDto(transaction.getAssignment());
      this.taskName = transaction.getAssignment().getTask().getName();
      if (transaction.getAssignment().getTask().getApartment() != null) {
        this.apartmentName = transaction.getAssignment().getTask().getApartment().getName();
      }
    }
  }

  private WorkerDto worker;

  private double amount;

  private AssignmentDto assignment;

  private String taskName;

  private String apartmentName;
}
