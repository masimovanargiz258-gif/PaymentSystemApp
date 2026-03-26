package az.bank.paymentsystem.model;

import az.bank.paymentsystem.enums.Currency;
import az.bank.paymentsystem.enums.CurrentAccountStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CurrentAccountResponse {
    private Long id;
    private String accountNumber;
    private BigDecimal balance;
    private CurrentAccountStatus currentAccountStatus;
    private Currency currency;
    private LocalDate activationDate;
    private LocalDate expirationDate;
    private String customerFullName;
}
