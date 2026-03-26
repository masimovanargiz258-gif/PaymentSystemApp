package az.bank.paymentsystem.model.entity;

import az.bank.paymentsystem.enums.CardStatus;
import az.bank.paymentsystem.enums.CardType;
import az.bank.paymentsystem.enums.Currency;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cards")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CardEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 3, nullable = false)
    private String cvv;
    @Column(unique = true, nullable = false, length = 16)
    private String pan;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus cardStatus;
    private String cardName;
    private Boolean isVisible;
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal balance;
    private LocalDate activationDate;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardType cardType;
    private LocalDate expireDate;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;
}
