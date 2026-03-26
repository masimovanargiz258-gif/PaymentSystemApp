package az.bank.paymentsystem.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomerUpdateRequest {
    @Size(min = 1, max = 50, message = "{name.size}")
    private String name;
    @Size(min = 1, max = 50, message = "{surname.size}")
    private String surname;
    private String phoneNumber;
    @Email(message = "{email.invalid}")
    private String email;
}
