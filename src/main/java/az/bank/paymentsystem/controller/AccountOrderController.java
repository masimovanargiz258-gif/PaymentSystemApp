package az.bank.paymentsystem.controller;

import az.bank.paymentsystem.model.AccountOrderRequest;
import az.bank.paymentsystem.model.CurrentAccountResponse;
import az.bank.paymentsystem.service.AccountOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/account-order")
@Tag(name = "AccountOrder", description = "AccountOrder API")
@RequiredArgsConstructor
public class AccountOrderController {
    private final AccountOrderService accountOrderService;
    @PostMapping
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    @Operation(summary = "Order a current account")
    public CurrentAccountResponse createAccountOrder(@Valid  @RequestBody AccountOrderRequest request) {
        return  accountOrderService.orderCurrentAccount(request);
    }
}
