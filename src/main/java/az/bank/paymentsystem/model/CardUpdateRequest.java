package az.bank.paymentsystem.model;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CardUpdateRequest {
    @Size(min = 1, max = 50, message = "{card.name.size}")
    private String cardName;
    private Boolean isVisible;
}
