package az.bank.paymentsystem.util;

import az.bank.paymentsystem.enums.CustomerStatus;
import az.bank.paymentsystem.exception.AgeRestrictionException;
import az.bank.paymentsystem.exception.CustomerStatusException;
import az.bank.paymentsystem.model.entity.CustomerEntity;
import az.bank.paymentsystem.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;

@Component
@RequiredArgsConstructor
public class CustomerValidationUtils {
    private final MessageService messageService;

    public void validateAge(CustomerEntity customer) {
        if (customer.getBirthDate() != null) {
            int age = Period.between(customer.getBirthDate(), LocalDate.now()).getYears();
            if (age < 18) {
                throw new AgeRestrictionException(messageService.getMessage("customer.underage"));
            }
        }
    }
    public void validateStatus(CustomerEntity customer) {
        if (customer.getCustomerStatus() == CustomerStatus.BLOCKED) {
            throw new CustomerStatusException(messageService.getMessage("customer.is.blocked"));
        }
        if (customer.getCustomerStatus() == CustomerStatus.DELETED) {
            throw new CustomerStatusException(messageService.getMessage("customer.is.deleted"));
        }
        if (customer.getCustomerStatus() == CustomerStatus.SUSPICIOUS) {
            throw new CustomerStatusException(messageService.getMessage("customer.is.suspicious"));
        }
    }
}
