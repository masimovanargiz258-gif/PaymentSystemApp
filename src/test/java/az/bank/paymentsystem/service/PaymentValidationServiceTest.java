package az.bank.paymentsystem.service;

import az.bank.paymentsystem.config.PaymentLimitsConfig;
import az.bank.paymentsystem.enums.*;
import az.bank.paymentsystem.exception.*;
import az.bank.paymentsystem.model.entity.*;
import az.bank.paymentsystem.model.BasePaymentRequest;
import az.bank.paymentsystem.repository.*;
import az.bank.paymentsystem.util.CurrencyConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentValidationServiceTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private CurrentAccountRepository currentAccountRepository;
    @Mock
    private CurrencyConverter currencyConverter;
    @Mock
    private PaymentLimitsConfig limitsConfig;
    @Mock
    private MessageService messageService;

    @InjectMocks
    private PaymentValidationService paymentValidationService;

    @Test
    void findAndValidateCustomer_Success() {
        CustomerEntity customer = new CustomerEntity();
        customer.setId(1L);
        customer.setCustomerStatus(CustomerStatus.ACTIVE);

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        CustomerEntity result = paymentValidationService.findAndValidateCustomer(1L);

        assertNotNull(result);
        assertEquals(CustomerStatus.ACTIVE, result.getCustomerStatus());
    }

    @Test
    void findAndValidateCustomer_Blocked_ThrowsException() {
        CustomerEntity customer = new CustomerEntity();
        customer.setId(1L);
        customer.setCustomerStatus(CustomerStatus.BLOCKED);

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));
        when(messageService.getMessage(any()))
                .thenReturn("Customer is blocked");

        assertThrows(CustomerStatusException.class,
                () -> paymentValidationService.findAndValidateCustomer(1L));
    }

    @Test
    void findAndValidateCustomer_Suspicious_ThrowsException() {
        CustomerEntity customer = new CustomerEntity();
        customer.setId(1L);
        customer.setCustomerStatus(CustomerStatus.SUSPICIOUS);

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));
        when(messageService.getMessage(any()))
                .thenReturn("Customer is suspicious");

        assertThrows(CustomerStatusException.class,
                () -> paymentValidationService.findAndValidateCustomer(1L));
    }

    @Test
    void validateCardPayment_Success() {
        CustomerEntity customer = new CustomerEntity();
        customer.setId(1L);

        CardEntity card = new CardEntity();
        card.setCustomer(customer);
        card.setCardStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.valueOf(500));
        card.setCurrency(Currency.AZN);
        card.setExpireDate(LocalDate.now().plusYears(3));

        BasePaymentRequest request = new BasePaymentRequest();
        request.setCustomerId(1L);
        request.setFromAccountNumber("4169123456789012");
        request.setAmount(BigDecimal.valueOf(20));
        request.setCurrency(Currency.AZN);

        when(cardRepository.findByPan("4169123456789012"))
                .thenReturn(Optional.of(card));
        when(currencyConverter.convertToAZN(any(), any()))
                .thenReturn(BigDecimal.valueOf(500));
        when(limitsConfig.getCardMinBalance())
                .thenReturn(BigDecimal.valueOf(10));

        assertDoesNotThrow(() ->
                paymentValidationService.validateCardPayment(request));
    }

    @Test
    void validateCardPayment_BlockedCard_ThrowsException() {
        CustomerEntity customer = new CustomerEntity();
        customer.setId(1L);

        CardEntity card = new CardEntity();
        card.setCustomer(customer);
        card.setCardStatus(CardStatus.BLOCKED);
        card.setExpireDate(LocalDate.now().plusYears(3));

        BasePaymentRequest request = new BasePaymentRequest();
        request.setCustomerId(1L);
        request.setFromAccountNumber("4169123456789012");

        when(cardRepository.findByPan("4169123456789012"))
                .thenReturn(Optional.of(card));
        when(messageService.getMessage(any()))
                .thenReturn("Card is blocked");

        assertThrows(CardStatusException.class,
                () -> paymentValidationService.validateCardPayment(request));
    }

    @Test
    void validateCardPayment_InsufficientBalance_NoAccount_ThrowsException() {
        CustomerEntity customer = new CustomerEntity();
        customer.setId(1L);

        CardEntity card = new CardEntity();
        card.setCustomer(customer);
        card.setCardStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.valueOf(3));
        card.setCurrency(Currency.AZN);
        card.setExpireDate(LocalDate.now().plusYears(3));

        BasePaymentRequest request = new BasePaymentRequest();
        request.setCustomerId(1L);
        request.setFromAccountNumber("4169123456789012");
        request.setAmount(BigDecimal.valueOf(20));
        request.setCurrency(Currency.AZN);

        when(cardRepository.findByPan("4169123456789012"))
                .thenReturn(Optional.of(card));
        when(currencyConverter.convertToAZN(any(), any()))
                .thenReturn(BigDecimal.valueOf(3));
        when(limitsConfig.getCardMinBalance())
                .thenReturn(BigDecimal.valueOf(10));
        when(currentAccountRepository.findByCustomerIdAndCurrentAccountStatusForUpdate(any(), any()))
                .thenReturn(Collections.emptyList());
        when(messageService.getMessage(any()))
                .thenReturn("Card balance is below minimum");

        assertThrows(InsufficientBalanceException.class,
                () -> paymentValidationService.validateCardPayment(request));
    }

    @Test
    void validateAccountPayment_InsufficientBalance_ThrowsException() {
        CustomerEntity customer = new CustomerEntity();
        customer.setId(1L);

        CurrentAccountEntity account = new CurrentAccountEntity();
        account.setCustomer(customer);
        account.setCurrentAccountStatus(CurrentAccountStatus.ACTIVE);
        account.setBalance(BigDecimal.valueOf(2));
        account.setCurrency(Currency.USD);
        account.setExpireDate(LocalDate.now().plusYears(3));

        BasePaymentRequest request = new BasePaymentRequest();
        request.setCustomerId(1L);
        request.setFromAccountNumber("NB12345678901234567890");
        request.setAmount(BigDecimal.valueOf(20));
        request.setCurrency(Currency.AZN);

        when(currentAccountRepository.findByAccountNumber("NB12345678901234567890"))
                .thenReturn(Optional.of(account));
        when(currencyConverter.convertToUSD(any(), any()))
                .thenReturn(BigDecimal.valueOf(2));
        when(limitsConfig.getAccountMinBalance())
                .thenReturn(BigDecimal.valueOf(5));
        when(messageService.getMessage(any()))
                .thenReturn("Account balance is below minimum");

        assertThrows(InsufficientBalanceException.class,
                () -> paymentValidationService.validateAccountPayment(request));
    }

    @Test
    void validateToCard_BlockedReceiver_ThrowsException() {
        CardEntity card = new CardEntity();
        card.setCardStatus(CardStatus.BLOCKED);

        when(cardRepository.findByPan("4169999999999999"))
                .thenReturn(Optional.of(card));
        when(messageService.getMessage(any()))
                .thenReturn("Receiver card is blocked");

        assertThrows(CardStatusException.class,
                () -> paymentValidationService.validateToCard("4169999999999999"));
    }

    @Test
    void validateToAccount_BlockedReceiver_ThrowsException() {
        CurrentAccountEntity account = new CurrentAccountEntity();
        account.setCurrentAccountStatus(CurrentAccountStatus.BLOCKED);

        when(currentAccountRepository.findByAccountNumber("NB99999999999999999999"))
                .thenReturn(Optional.of(account));
        when(messageService.getMessage(any()))
                .thenReturn("Receiver account is blocked");

        assertThrows(AccountStatusException.class,
                () -> paymentValidationService.validateToAccount("NB99999999999999999999"));
    }
}
