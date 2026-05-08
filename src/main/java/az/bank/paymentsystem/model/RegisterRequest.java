package az.bank.paymentsystem.model;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    @NotBlank(message = "{username.required}")
    @Size(min = 3, max = 50, message = "{username.size}")
    private String username;
    @NotBlank(message = "{password.required}")
    @Size(min = 6, max = 100, message = "{password.size}")
    private String password;
    @NotNull(message = "{customer.id.required}")
    private Long customerId;
    @NotBlank(message = "{registration.token.required}")
    private String registrationToken;
}