package az.bank.paymentsystem.controller;

import az.bank.paymentsystem.model.CardOrderRequest;
import az.bank.paymentsystem.model.CardResponse;
import az.bank.paymentsystem.service.CardOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/card-orders")
@Tag(name = "CardOrder", description = "Card Order API")
@RequiredArgsConstructor
public class CardOrderController {
    private final CardOrderService cardOrderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Order a card")
    public CardResponse orderCard(@Valid @RequestBody CardOrderRequest request) {
        return cardOrderService.orderCard(request);
    }
}