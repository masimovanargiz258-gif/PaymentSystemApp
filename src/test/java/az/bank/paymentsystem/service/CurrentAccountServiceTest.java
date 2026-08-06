package az.bank.paymentsystem.service;

import az.bank.paymentsystem.enums.Currency;
import az.bank.paymentsystem.enums.CurrentAccountStatus;
import az.bank.paymentsystem.exception.AccountNotFoundException;
import az.bank.paymentsystem.exception.AccountStatusException;
import az.bank.paymentsystem.exception.InsufficientAmountException;
import az.bank.paymentsystem.model.CurrentAccountDepositRequest;
import az.bank.paymentsystem.model.CurrentAccountResponse;
import az.bank.paymentsystem.model.entity.CurrentAccountEntity;
import az.bank.paymentsystem.model.entity.CustomerEntity;
import az.bank.paymentsystem.repository.CurrentAccountRepository;
import az.bank.paymentsystem.util.SecurityUtils;
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
class CurrentAccountServiceTest {

    @Mock
    private CurrentAccountRepository currentAccountRepository;
    @Mock
    private MessageService messageService;
    @InjectMocks
    private CurrentAccountService currentAccountService;
    @Mock
    private SecurityUtils securityUtils;

    @Test
    void deposit_NewAccount_BecomesActive() {
        CustomerEntity customer = new CustomerEntity();
        customer.setName("Aysel");
        customer.setSurname("Aliyeva");

        CurrentAccountEntity account = new CurrentAccountEntity();
        account.setId(1L);
        account.setCurrentAccountStatus(CurrentAccountStatus.NEW);
        account.setBalance(BigDecimal.ZERO);
        account.setCurrency(Currency.AZN);
        account.setExpireDate(LocalDate.now().plusYears(3));
        account.setCustomer(customer);

        CurrentAccountDepositRequest request = new CurrentAccountDepositRequest();
        request.setAmount(BigDecimal.valueOf(500));

        when(currentAccountRepository.findWithCustomerById(1L))
                .thenReturn(Optional.of(account));
        when(currentAccountRepository.save(any()))
                .thenReturn(account);

        CurrentAccountResponse response = currentAccountService.deposit(1L, request);

        assertNotNull(response);
        assertEquals(CurrentAccountStatus.ACTIVE, account.getCurrentAccountStatus());
        assertEquals(BigDecimal.valueOf(500), account.getBalance());
        verify(currentAccountRepository).save(any());
    }

    @Test
    void deposit_NegativeAmount_ThrowsException() {
        CustomerEntity customer = new CustomerEntity();
        customer.setName("Aysel");
        customer.setSurname("Aliyeva");

        CurrentAccountEntity account = new CurrentAccountEntity();
        account.setId(1L);
        account.setCurrentAccountStatus(CurrentAccountStatus.ACTIVE);
        account.setBalance(BigDecimal.valueOf(100));
        account.setExpireDate(LocalDate.now().plusYears(3));
        account.setCustomer(customer);

        CurrentAccountDepositRequest request = new CurrentAccountDepositRequest();
        request.setAmount(BigDecimal.valueOf(-50));

        when(currentAccountRepository.findWithCustomerById(1L))
                .thenReturn(Optional.of(account));
        when(messageService.getMessage(any()))
                .thenReturn("Amount must be greater than 0");

        assertThrows(InsufficientAmountException.class,
                () -> currentAccountService.deposit(1L, request));
    }

    @Test
    void cancelAccount_Success() {
        CustomerEntity customer = new CustomerEntity();
        customer.setName("Aysel");
        customer.setSurname("Aliyeva");

        CurrentAccountEntity account = new CurrentAccountEntity();
        account.setId(1L);
        account.setCurrentAccountStatus(CurrentAccountStatus.ACTIVE);
        account.setExpireDate(LocalDate.now().plusYears(3));
        account.setCustomer(customer);

        when(currentAccountRepository.findWithCustomerById(1L))
                .thenReturn(Optional.of(account));
        when(currentAccountRepository.save(any()))
                .thenReturn(account);

        currentAccountService.cancelCurrentAccount(1L);

        assertEquals(CurrentAccountStatus.CANCELED, account.getCurrentAccountStatus());
        verify(currentAccountRepository).save(any());
    }

    @Test
    void cancelAccount_AlreadyCanceled_ThrowsException() {
        CustomerEntity customer = new CustomerEntity();
        customer.setName("Aysel");
        customer.setSurname("Aliyeva");

        CurrentAccountEntity account = new CurrentAccountEntity();
        account.setId(1L);
        account.setCurrentAccountStatus(CurrentAccountStatus.CANCELED);
        account.setCustomer(customer);

        when(currentAccountRepository.findWithCustomerById(1L))
                .thenReturn(Optional.of(account));
        when(messageService.getMessage(any()))
                .thenReturn("Account is already canceled");

        assertThrows(AccountStatusException.class,
                () -> currentAccountService.cancelCurrentAccount(1L));
    }

    @Test
    void unblockAccount_Success() {
        CustomerEntity customer = new CustomerEntity();
        customer.setName("Aysel");
        customer.setSurname("Aliyeva");

        CurrentAccountEntity account = new CurrentAccountEntity();
        account.setId(1L);
        account.setCurrentAccountStatus(CurrentAccountStatus.BLOCKED);
        account.setCustomer(customer);

        when(currentAccountRepository.findWithCustomerById(1L))
                .thenReturn(Optional.of(account));
        when(currentAccountRepository.save(any()))
                .thenReturn(account);

        currentAccountService.unblockAccount(1L);

        assertEquals(CurrentAccountStatus.ACTIVE, account.getCurrentAccountStatus());
        verify(currentAccountRepository).save(any());
    }

    @Test
    void getAccountById_NotFound_ThrowsException() {
        when(currentAccountRepository.findWithCustomerById(999L))
                .thenReturn(Optional.empty());
        when(messageService.getMessage(any()))
                .thenReturn("Account not found");

        assertThrows(AccountNotFoundException.class,
                () -> currentAccountService.getCurrentAccountById(999L));
    }
}