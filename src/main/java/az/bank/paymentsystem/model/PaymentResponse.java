package az.bank.paymentsystem.model;

import az.bank.paymentsystem.enums.Currency;
import az.bank.paymentsystem.enums.PaymentSourceType;
import az.bank.paymentsystem.enums.PaymentStatus;
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
public class PaymentResponse {
    private Long id;
    private BigDecimal amount;
    private Currency currency;
    private PaymentStatus paymentStatus;
    private PaymentSourceType paymentSourceType;
    private String fromAccountNumber;
    private String toAccountNumber;
    private BigDecimal commissionAmount;
    private LocalDateTime createdAt;
}