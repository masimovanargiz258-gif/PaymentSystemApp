package az.bank.paymentsystem.controller;

import az.bank.paymentsystem.model.PaymentRequest;
import az.bank.paymentsystem.model.PaymentResponse;
import az.bank.paymentsystem.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payment", description = "Payment API")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    @PostMapping("/make-payment")
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    @Operation(summary = "Make payment")
    public PaymentResponse makePayment(@Valid @RequestBody PaymentRequest paymentRequest){
        return paymentService.createPayment(paymentRequest);
    }
}
