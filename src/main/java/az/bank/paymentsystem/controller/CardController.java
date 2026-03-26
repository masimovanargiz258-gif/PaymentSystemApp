package az.bank.paymentsystem.controller;

import az.bank.paymentsystem.model.CardDepositRequest;
import az.bank.paymentsystem.model.CardResponse;
import az.bank.paymentsystem.model.CardUpdateRequest;
import az.bank.paymentsystem.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/v1/cards")
@Tag(name = "Card", description = "Card API")
@RequiredArgsConstructor
public class CardController {
    private final CardService cardService;

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get card by id")
    public CardResponse getCardById(@PathVariable Long id) {
        return cardService.getCardById(id);
    }

    @GetMapping("/customer/{customerId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all cards by customer")
    public List<CardResponse> getCardByCustomerId(@PathVariable Long customerId) {
        return cardService.getCardByCustomerId(customerId);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update card")
    public CardResponse updateCard(@PathVariable Long id,@Valid @RequestBody CardUpdateRequest request) {
        return cardService.updateCard(id, request);
    }

    @PatchMapping("/{id}/deposit")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Deposit money to card")
    public CardResponse deposit(@PathVariable Long id,@Valid @RequestBody CardDepositRequest request) {
        return cardService.deposit(id, request);
    }

    @PatchMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Cancel card")
    public CardResponse cancelCard(@PathVariable Long id) {
        return cardService.cancelCard(id);
    }

    @PatchMapping("/{id}/unblock")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Unblock card")
    public CardResponse unblockCard(@PathVariable Long id) {
        return cardService.unblockCard(id);
    }
}
