package com.viladevcorp.hosteo.service;

import com.viladevcorp.hosteo.exceptions.DebtOperationNotAllowedException;
import com.viladevcorp.hosteo.model.*;
import com.viladevcorp.hosteo.model.dto.BalanceDto;
import com.viladevcorp.hosteo.model.forms.TransactionCreateForm;
import com.viladevcorp.hosteo.model.forms.TransactionSearchForm;
import com.viladevcorp.hosteo.model.forms.TransactionUpdateForm;
import com.viladevcorp.hosteo.model.types.AssignmentState;
import com.viladevcorp.hosteo.repository.TransactionRepository;
import com.viladevcorp.hosteo.repository.WorkerRepository;
import com.viladevcorp.hosteo.utils.AuthUtils;
import com.viladevcorp.hosteo.utils.ServiceUtils;
import jakarta.persistence.EntityNotFoundException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WorkerRepository workerRepository;
    private final WorkerService workerService;

    @Autowired
    public TransactionService(
            TransactionRepository transactionRepository,
            WorkerRepository workerRepository,
            WorkerService workerService) {
        this.transactionRepository = transactionRepository;
        this.workerRepository = workerRepository;
        this.workerService = workerService;
    }

    /**
     * Creates a payment transaction. The service receives a positive amount and stores it negated.
     */
    public Transaction createPayment(TransactionCreateForm form) throws EntityNotFoundException {
        Worker worker = workerService.getWorkerById(form.getWorkerId());
        Transaction transaction =
                Transaction.builder().worker(worker).amount(-form.getAmount()).assignment(null).build();
        return transactionRepository.save(transaction);
    }

    /**
     * Updates a payment. Debts cannot be updated from the public API.
     */
    public Transaction updatePayment(TransactionUpdateForm form)
            throws EntityNotFoundException, DebtOperationNotAllowedException {
        Transaction transaction = getTransactionById(form.getId());
        if (transaction.getAssignment() != null) {
            throw new DebtOperationNotAllowedException("Debts cannot be updated.");
        }
        transaction.setAmount(-form.getAmount());
        return transactionRepository.save(transaction);
    }

    /**
     * Gets a transaction
     */
    public Transaction getTransactionById(UUID id) throws EntityNotFoundException {
        Optional<Transaction> result = transactionRepository.findById(id, AuthUtils.getUsername());
        if (result.isEmpty()) {
            throw new EntityNotFoundException("Transaction not found with id: " + id);
        } else {
            return result.get();
        }
    }

    /**
     * Deletes a payment. Debts cannot be deleted from the public API.
     */
    public void deletePayment(UUID id)
            throws EntityNotFoundException, DebtOperationNotAllowedException {
        Transaction transaction = getTransactionById(id);
        if (transaction.getAssignment() != null) {
            throw new DebtOperationNotAllowedException("Debts cannot be deleted.");
        }
        transactionRepository.delete(transaction);
    }

    public List<Transaction> findTransactions(TransactionSearchForm form) {
        return transactionRepository.advancedSearch(
                AuthUtils.getUsername(),
                form.getWorkerId(),
                form.getStartDate(),
                form.getEndDate(),
                form.getIsPayment(),
                ServiceUtils.createPageRequest(form.getPageNumber(), form.getPageSize()));
    }

    public PageMetadata getTransactionsMetadata(TransactionSearchForm form) {
        int totalRows =
                transactionRepository.advancedCount(
                        AuthUtils.getUsername(),
                        form.getWorkerId(),
                        form.getStartDate(),
                        form.getEndDate(),
                        form.getIsPayment());
        int totalPages = ServiceUtils.calculateTotalPages(form.getPageSize(), totalRows);
        return new PageMetadata(totalPages, totalRows);
    }

    public List<BalanceDto> getBalances() {
        String username = AuthUtils.getUsername();
        Map<UUID, Double> balanceByWorker = new HashMap<>();
        for (Object[] row : transactionRepository.sumByWorker(username)) {
            Worker worker = (Worker) row[0];
            balanceByWorker.put(worker.getId(), ((Number) row[1]).doubleValue());
        }

        List<BalanceDto> balances = new ArrayList<>();
        for (Worker worker : workerRepository.advancedSearch(username, null, null, Pageable.unpaged())) {
            balances.add(new BalanceDto(worker, balanceByWorker.getOrDefault(worker.getId(), 0.0)));
        }
        balances.sort(Comparator.comparingDouble(BalanceDto::getBalance).reversed());
        return balances;
    }

    /**
     * Synchronizes the debt transaction after an assignment is created/updated based on its previous
     * state and worker. This runs in the same transaction as the assignment change.
     */
    public void handleAssignmentChange(
            Assignment assignment, AssignmentState previousState, Worker previousWorker) {
        boolean wasFinished = previousState == AssignmentState.FINISHED;
        boolean isFinished = assignment.getState() == AssignmentState.FINISHED;

        // If we changed from unfinished to finished, create a debt transaction.
        if (!wasFinished && isFinished) {
            createDebt(assignment);
            return;
        }

        // If we changed from finished to unfinished, delete the debt transaction
        if (wasFinished && !isFinished) {
            deleteDebt(assignment);
            return;
        }

        // If the assignment was finished and the worker changed, update the debt transaction to the new worker.
        if (wasFinished && previousWorker != null
                && assignment.getWorker() != null
                && !previousWorker.getId().equals(assignment.getWorker().getId())) {
            updateDebtWorker(assignment);
        }
    }

    public void createDebt(Assignment assignment) {
        double cost = assignment.getTask().getCost();
        if (cost <= 0) {
            return;
        }
        if (transactionRepository.existsByAssignmentId(
                assignment.getId(), AuthUtils.getUsername())) {
            return;
        }
        Transaction transaction =
                Transaction.builder()
                        .worker(assignment.getWorker())
                        .amount(cost)
                        .assignment(assignment)
                        .build();
        transactionRepository.save(transaction);
    }

    public void deleteDebt(Assignment assignment) {
        transactionRepository.deleteByAssignmentId(assignment.getId(), AuthUtils.getUsername());
    }

    public void updateDebtWorker(Assignment assignment) {
        transactionRepository
                .findByAssignmentId(assignment.getId(), AuthUtils.getUsername())
                .ifPresent(
                        transaction -> {
                            transaction.setWorker(assignment.getWorker());
                            transactionRepository.save(transaction);
                        });
    }
}
