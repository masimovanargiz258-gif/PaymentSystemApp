package az.bank.paymentsystem.model;

import az.bank.paymentsystem.enums.PaymentSourceType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest extends BasePaymentRequest {
    @NotNull(message = "{payment.source.type.required}")
    private PaymentSourceType paymentSourceType;

}