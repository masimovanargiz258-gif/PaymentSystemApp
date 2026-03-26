package az.bank.paymentsystem.model;

import az.bank.paymentsystem.enums.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {
    private Long id;
    private BigDecimal amount;
    private Currency currency;
    private PaymentSourceType paymentSourceType;
    private TransactionStatus transactionStatus;
    private LocalDateTime transactionDate;
    private String fromAccountNumber;
    private String toAccountNumber;
    private Boolean isFallback;
    private BigDecimal balanceAfter;
    private BigDecimal commissionAmount;
}
