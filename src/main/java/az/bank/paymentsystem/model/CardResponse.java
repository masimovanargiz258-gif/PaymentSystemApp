package az.bank.paymentsystem.model;

import az.bank.paymentsystem.enums.CardStatus;
import az.bank.paymentsystem.enums.CardType;
import az.bank.paymentsystem.enums.Currency;
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
public class CardResponse {
    private Long id;
    private String pan;
    private CardStatus cardStatus;
    private CardType cardType;
    private String cardName;
    private BigDecimal balance;
    private Currency currency;
    private LocalDate expireDate;
    private String cardHolderName;
}
