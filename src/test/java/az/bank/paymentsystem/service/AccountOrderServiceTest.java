package az.bank.paymentsystem.service;


import az.bank.paymentsystem.config.PaymentLimitsConfig;
import az.bank.paymentsystem.enums.*;
import az.bank.paymentsystem.exception.*;
import az.bank.paymentsystem.model.CurrentAccountResponse;
import az.bank.paymentsystem.model.entity.CurrentAccountEntity;
import az.bank.paymentsystem.model.entity.CustomerEntity;
import az.bank.paymentsystem.model.AccountOrderRequest;
import az.bank.paymentsystem.repository.AccountOrderRepository;
import az.bank.paymentsystem.repository.CurrentAccountRepository;
import az.bank.paymentsystem.repository.CustomerRepository;
import az.bank.paymentsystem.util.CustomerValidationUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountOrderServiceTest {

    @Mock
    private AccountOrderRepository accountOrderRepository;
    @Mock
    private CurrentAccountRepository currentAccountRepository;
    @Mock
    private CurrentAccountService currentAccountService;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private MessageService messageService;
    @Mock
    private PaymentLimitsConfig limitsConfig;
    @Mock
    private CustomerValidationUtils customerValidationUtils;

    @InjectMocks
    private AccountOrderService accountOrderService;

    @Test
    void orderAccount_Success() {
        AccountOrderRequest request = new AccountOrderRequest();
        request.setCustomerId(1L);
        request.setCurrency(Currency.AZN);

        CustomerEntity customer = new CustomerEntity();
        customer.setId(1L);
        customer.setCustomerStatus(CustomerStatus.ACTIVE);
        customer.setBirthDate(LocalDate.of(1995, 1, 1));

        CurrentAccountEntity savedAccount = new CurrentAccountEntity();
        savedAccount.setId(1L);
        savedAccount.setCurrentAccountStatus(CurrentAccountStatus.NEW);
        savedAccount.setBalance(BigDecimal.ZERO);

        CurrentAccountResponse accountResponse = new CurrentAccountResponse();
        accountResponse.setId(1L);

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));
        doNothing().when(customerValidationUtils).validateStatus(any());
        doNothing().when(customerValidationUtils).validateAge(any());
        when(currentAccountRepository.countByCustomerId(1L))
                .thenReturn(0L);
        when(limitsConfig.getAccountLimit())
                .thenReturn(3);
        when(currentAccountRepository.existsByAccountNumber(any()))
                .thenReturn(false);
        when(currentAccountRepository.save(any()))
                .thenReturn(savedAccount);
        when(currentAccountService.getCurrentAccountById(1L))
                .thenReturn(accountResponse);

        CurrentAccountResponse response =
                accountOrderService.orderCurrentAccount(request);

        assertNotNull(response);
        verify(currentAccountRepository).save(any());
        verify(accountOrderRepository).save(any());
    }

    @Test
    void orderAccount_CustomerNotFound_ThrowsException() {
        AccountOrderRequest request = new AccountOrderRequest();
        request.setCustomerId(999L);

        when(customerRepository.findById(999L))
                .thenReturn(Optional.empty());
        when(messageService.getMessage(any()))
                .thenReturn("Customer not found");

        assertThrows(CustomerNotFoundException.class,
                () -> accountOrderService.orderCurrentAccount(request));
    }

    @Test
    void orderAccount_AccountLimitExceeded_ThrowsException() {
        AccountOrderRequest request = new AccountOrderRequest();
        request.setCustomerId(1L);
        request.setCurrency(Currency.AZN);

        CustomerEntity customer = new CustomerEntity();
        customer.setId(1L);
        customer.setCustomerStatus(CustomerStatus.ACTIVE);
        customer.setBirthDate(LocalDate.of(1995, 1, 1));

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));
        doNothing().when(customerValidationUtils).validateStatus(any());
        doNothing().when(customerValidationUtils).validateAge(any());
        when(currentAccountRepository.countByCustomerId(1L))
                .thenReturn(3L);
        when(limitsConfig.getAccountLimit())
                .thenReturn(3);
        when(messageService.getMessage(any()))
                .thenReturn("Account limit exceeded");

        assertThrows(AccountLimitException.class,
                () -> accountOrderService.orderCurrentAccount(request));
    }
}