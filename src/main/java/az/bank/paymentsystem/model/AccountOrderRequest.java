package az.bank.paymentsystem.model;

import az.bank.paymentsystem.enums.Currency;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AccountOrderRequest {
    @NotNull(message = "{customer.id.required}")
    private Long customerId;
    @NotNull(message = "{currency.required}")
    private Currency currency;
}