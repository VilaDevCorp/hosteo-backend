package com.viladevcorp.hosteo.model;

import com.viladevcorp.hosteo.model.dto.BaseEntityDto;
import com.viladevcorp.hosteo.model.dto.TransactionDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Transaction extends BaseEntity {

  @NotNull
  @ManyToOne(optional = false)
  private Worker worker;

  @NotNull
  @Column(nullable = false)
  private double amount;

  @ManyToOne(optional = true)
  private Assignment assignment;

  @Override
  public BaseEntityDto toDto() {
    return new TransactionDto(this);
  }
}
