package az.bank.paymentsystem.model;

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
public class CurrentAccountDepositRequest {
    @NotNull(message = "{amount.required}")
    @Positive(message = "{amount.positive}")
    private BigDecimal amount;
}
