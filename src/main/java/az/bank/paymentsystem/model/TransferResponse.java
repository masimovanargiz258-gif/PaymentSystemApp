package az.bank.paymentsystem.model;

import az.bank.paymentsystem.enums.Currency;
import az.bank.paymentsystem.enums.PaymentStatus;
import az.bank.paymentsystem.enums.TransferType;
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
public class TransferResponse {
    private Long id;
    private BigDecimal amount;
    private Currency currency;
    private PaymentStatus paymentStatus;
    private TransferType transferType;
    private String fromAccountNumber;
    private String toAccountNumber;
    private LocalDateTime createdAt;
}
