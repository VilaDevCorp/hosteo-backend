package com.viladevcorp.hosteo.controller;

import com.viladevcorp.hosteo.exceptions.DebtOperationNotAllowedException;
import com.viladevcorp.hosteo.model.Page;
import com.viladevcorp.hosteo.model.PageMetadata;
import com.viladevcorp.hosteo.model.Transaction;
import com.viladevcorp.hosteo.model.dto.BalanceDto;
import com.viladevcorp.hosteo.model.dto.TransactionDto;
import com.viladevcorp.hosteo.model.forms.TransactionCreateForm;
import com.viladevcorp.hosteo.model.forms.TransactionSearchForm;
import com.viladevcorp.hosteo.model.forms.TransactionUpdateForm;
import com.viladevcorp.hosteo.service.TransactionService;
import com.viladevcorp.hosteo.utils.ApiResponse;
import com.viladevcorp.hosteo.utils.CodeErrors;
import com.viladevcorp.hosteo.utils.ValidationUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
public class TransactionController {

  private final TransactionService transactionService;

  @Autowired
  public TransactionController(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  @PostMapping("/transactions")
  public ResponseEntity<ApiResponse<TransactionDto>> createPayment(
      @Valid @RequestBody TransactionCreateForm form, BindingResult bindingResult) {
    log.info("[TransactionController.createPayment] - Creating payment");
    ResponseEntity<ApiResponse<TransactionDto>> validationResponse =
        ValidationUtils.handleFormValidation(bindingResult);
    if (validationResponse != null) {
      return validationResponse;
    }
    try {
      Transaction transaction = transactionService.createPayment(form);
      return ResponseEntity.ok().body(new ApiResponse<>(new TransactionDto(transaction)));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ApiResponse<>(null, e.getMessage()));
    }
  }

  @PutMapping("/transactions")
  public ResponseEntity<ApiResponse<TransactionDto>> updatePayment(
      @Valid @RequestBody TransactionUpdateForm form, BindingResult bindingResult) {
    log.info("[TransactionController.updatePayment] - Updating payment: {}", form.getId());
    ResponseEntity<ApiResponse<TransactionDto>> validationResponse =
        ValidationUtils.handleFormValidation(bindingResult);
    if (validationResponse != null) {
      return validationResponse;
    }
    try {
      Transaction transaction = transactionService.updatePayment(form);
      return ResponseEntity.ok().body(new ApiResponse<>(new TransactionDto(transaction)));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ApiResponse<>(null, e.getMessage()));
    } catch (DebtOperationNotAllowedException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.DEBT_OPERATION_NOT_ALLOWED, e.getMessage()));
    }
  }

  @PostMapping("/transactions/search")
  public ResponseEntity<ApiResponse<Page<TransactionDto>>> searchTransactions(
      @RequestBody TransactionSearchForm form) {
    log.info("[TransactionController.searchTransactions] - Searching transactions");
    List<Transaction> transactions = transactionService.findTransactions(form);
    PageMetadata pageMetadata = transactionService.getTransactionsMetadata(form);
    Page<TransactionDto> page =
        new Page<>(
            transactions.stream().map(TransactionDto::new).toList(),
            pageMetadata.getTotalPages(),
            pageMetadata.getTotalRows());
    log.info("[TransactionController.searchTransactions] - Found {} transactions", transactions.size());
    return ResponseEntity.ok().body(new ApiResponse<>(page));
  }

  @GetMapping("/transactions/balances")
  public ResponseEntity<ApiResponse<List<BalanceDto>>> getBalances() {
    log.info("[TransactionController.getBalances] - Fetching balances");
    return ResponseEntity.ok().body(new ApiResponse<>(transactionService.getBalances()));
  }

  @DeleteMapping("/transactions/{transactionId}")
  public ResponseEntity<ApiResponse<Void>> deletePayment(@PathVariable UUID transactionId) {
    log.info("[TransactionController.deletePayment] - Deleting payment: {}", transactionId);
    try {
      transactionService.deletePayment(transactionId);
      return ResponseEntity.ok()
          .body(new ApiResponse<>(null, "Transaction deleted successfully."));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ApiResponse<>(null, e.getMessage()));
    } catch (DebtOperationNotAllowedException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(new ApiResponse<>(CodeErrors.DEBT_OPERATION_NOT_ALLOWED, e.getMessage()));
    }
  }
}
