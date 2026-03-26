package az.bank.paymentsystem.model.entity;
import az.bank.paymentsystem.enums.CurrentAccountStatus;
import az.bank.paymentsystem.enums.Currency;
import jakarta.persistence.*;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;


@Entity
@Table(name = "current_accounts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CurrentAccountEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String accountNumber;
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal balance;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency  currency;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CurrentAccountStatus currentAccountStatus;
    private LocalDate activationDate;
    private LocalDate expireDate;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;
}
