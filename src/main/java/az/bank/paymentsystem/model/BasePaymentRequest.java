package az.bank.paymentsystem.model;

import az.bank.paymentsystem.enums.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BasePaymentRequest {
    @NotNull(message = "{customer.id.required}")
    private Long customerId;
    @NotNull(message = "{amount.required}")
    @Positive(message = "{amount.positive}")
    private BigDecimal amount;
    @NotNull(message = "{currency.required}")
    private Currency currency;
    @NotBlank(message = "{from.account.required}")
    private String fromAccountNumber;
    @NotBlank(message = "{to.account.required}")
    private String toAccountNumber;
}
