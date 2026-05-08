package az.bank.paymentsystem.model.entity;

import az.bank.paymentsystem.enums.CustomerStatus;
import az.bank.paymentsystem.enums.CustomerType;
import jakarta.persistence.*;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "customers")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomerEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String surname;
    private String fullName;
    @Column(unique = true, length = 7)
    private String fin;
    @Column(unique = true)
    private String voen;
    private LocalDate birthDate;
    @Column(nullable = false)
    private String phoneNumber;
    @Column(nullable = false)
    private String email;
    private Boolean isVisible = true;
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal monthlyTurnover = BigDecimal.ZERO;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerStatus customerStatus;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerType customerType;
    @OneToMany(mappedBy = "customer")
    Set<CurrentAccountEntity> currentAccounts;
    @OneToMany(mappedBy = "customer")
    Set<CardEntity> cards;
    @OneToOne(mappedBy = "customer")
    private UserEntity user;
    @Column(unique = true)
    private String registrationToken;
}
