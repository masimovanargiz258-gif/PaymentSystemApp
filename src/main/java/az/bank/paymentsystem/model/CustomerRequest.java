package az.bank.paymentsystem.model;

import az.bank.paymentsystem.enums.CustomerType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomerRequest {
    @NotBlank(message = "{name.required}")
    private String name;
    @NotBlank(message = "{surname.required}")
    private String surname;
    private String fin;
    private String voen;
    private LocalDate birthDate;
    @NotBlank(message = "{phone.required}")
    private String phoneNumber;
    @NotBlank(message = "{email.required}")
    @Email(message = "{email.invalid}")
    private String email;
    @NotNull(message = "{customer.type.required}")
    private CustomerType customerType;
}
