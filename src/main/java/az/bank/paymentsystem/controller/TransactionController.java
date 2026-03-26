package az.bank.paymentsystem.controller;

import az.bank.paymentsystem.model.TransactionResponse;
import az.bank.paymentsystem.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transaction", description = "Transaction API")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping("/card/{pan}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get last 100 transactions by card PAN")
    public List<TransactionResponse> getLast100ByCard(@PathVariable String pan) {
        return transactionService.getLast100ByCard(pan);
    }

    @GetMapping("/account/{accountNumber}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get last 100 transactions by account number")
    public List<TransactionResponse> getLast100ByCurrentAccount(@PathVariable String accountNumber) {
        return transactionService.getLast100ByCurrentAccount(accountNumber);
    }
}
