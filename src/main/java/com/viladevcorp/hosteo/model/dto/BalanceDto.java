package com.viladevcorp.hosteo.model.dto;

import com.viladevcorp.hosteo.model.Worker;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BalanceDto {

    public BalanceDto(Worker worker, double balance) {
        if (worker == null) {
            return;
        }
        this.worker = new WorkerDto(worker);
        this.balance = balance;
    }

    private WorkerDto worker;

    private double balance;
}
