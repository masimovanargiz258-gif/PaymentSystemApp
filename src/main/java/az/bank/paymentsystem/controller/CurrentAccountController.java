package az.bank.paymentsystem.controller;

import az.bank.paymentsystem.model.CurrentAccountDepositRequest;
import az.bank.paymentsystem.model.CurrentAccountResponse;
import az.bank.paymentsystem.service.CurrentAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/current-accounts")
@Tag(name = "CurrentAccount", description = "CurrentAccount API")
@RequiredArgsConstructor
public class CurrentAccountController {
    private final CurrentAccountService currentAccountService;

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get current account by id")
    public CurrentAccountResponse getCurrentAccountById(@PathVariable Long id) {
        return currentAccountService.getCurrentAccountById(id);
    }

    @GetMapping("/customer/{customerId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all current accounts by customer")
    public List<CurrentAccountResponse> getAllCurrentAccounts(@PathVariable Long customerId) {
        return currentAccountService.getAllCurrentAccounts(customerId);
    }

    @PatchMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Cancel current account")
    public CurrentAccountResponse cancelCurrentAccount(@PathVariable Long id) {
        return currentAccountService.cancelCurrentAccount(id);
    }

    @PatchMapping("/{id}/deposit")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Deposit money to current account")
    public CurrentAccountResponse deposit(@PathVariable Long id,@Valid @RequestBody CurrentAccountDepositRequest request) {
        return currentAccountService.deposit(id, request);
    }

    @PatchMapping("/{id}/unblock")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Unblock current account")
    public CurrentAccountResponse unblockAccount(@PathVariable Long id) {
        return currentAccountService.unblockAccount(id);
    }
}

