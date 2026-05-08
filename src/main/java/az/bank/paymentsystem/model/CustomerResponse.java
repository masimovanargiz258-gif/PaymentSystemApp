package az.bank.paymentsystem.model;

import az.bank.paymentsystem.enums.CustomerStatus;
import az.bank.paymentsystem.enums.CustomerType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomerResponse {
    private Long id;
    private String fullName;
    private String fin;
    private String voen;
    private LocalDate birthDate;
    private String phoneNumber;
    private String email;
    private CustomerStatus customerStatus;
    private CustomerType customerType;
    private String registrationToken;
}
