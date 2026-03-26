package az.bank.paymentsystem.controller;

import az.bank.paymentsystem.model.TransferRequest;
import az.bank.paymentsystem.model.TransferResponse;
import az.bank.paymentsystem.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transfers")
@Tag(name = "Transfer", description = "Transfer API")
@RequiredArgsConstructor
public class TransferController {
    private final TransferService transferService;

    @PostMapping("/card-to-card")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Transfer from card to card")
    public TransferResponse cardToCard(@Valid @RequestBody TransferRequest request) {
        return transferService.cardToCard(request);
    }

    @PostMapping("/card-to-account")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Transfer from card to account")
    public TransferResponse cardToAccount(@Valid @RequestBody TransferRequest request) {
        return transferService.cardToAccount(request);
    }

    @PostMapping("/account-to-account")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Transfer from account to account")
    public TransferResponse accountToAccount(@Valid @RequestBody TransferRequest request) {
        return transferService.accountToAccount(request);
    }

    @PostMapping("/account-to-card")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Transfer from account to card")
    public TransferResponse accountToCard(@Valid @RequestBody TransferRequest request) {
        return transferService.accountToCard(request);
    }

    @PostMapping("/external")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "External transfer")
    public TransferResponse external(@Valid @RequestBody TransferRequest request) {
        return transferService.external(request);
    }
}