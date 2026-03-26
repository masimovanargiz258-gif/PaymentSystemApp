package az.bank.paymentsystem.model.entity;

import az.bank.paymentsystem.enums.Currency;
import az.bank.paymentsystem.enums.PaymentSourceType;
import az.bank.paymentsystem.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name ="transactions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus transactionStatus;
    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime transactionDate;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentSourceType paymentSourceType;
    @Column(nullable = false)
    private String fromAccountNumber;
    @Column(nullable = false)
    private String toAccountNumber;
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal amountInAzn;
    @Column(name = "is_fallback")
    private Boolean isFallback = false;
    @Column(name = "balance_after")
    private BigDecimal balanceAfter;
    @Column(name = "commission_amount")
    private BigDecimal commissionAmount;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private PaymentEntity payment;
}

