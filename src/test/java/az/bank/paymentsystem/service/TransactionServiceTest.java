package az.bank.paymentsystem.service;

import az.bank.paymentsystem.config.PaymentLimitsConfig;
import az.bank.paymentsystem.enums.*;
import az.bank.paymentsystem.model.entity.*;
import az.bank.paymentsystem.repository.*;
import az.bank.paymentsystem.util.CurrencyConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private TransactionRepository transactionRepository;
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


    @InjectMocks
    private TransactionService transactionService;

    @Test
    void processPayment_CardPayment_Success() {
        CustomerEntity customer = new CustomerEntity();
        customer.setId(1L);
        customer.setMonthlyTurnover(BigDecimal.ZERO);

        CardEntity card = new CardEntity();
        card.setCardStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.valueOf(500));
        card.setCurrency(Currency.AZN);
        card.setExpireDate(LocalDate.now().plusYears(3));

        PaymentEntity payment = new PaymentEntity();
        payment.setCustomer(customer);
        payment.setPaymentSourceType(PaymentSourceType.CARD);
        payment.setFromAccountNumber("4169123456789012");
        payment.setToAccountNumber("AZN5123456789012345");
        payment.setAmount(BigDecimal.valueOf(20));
        payment.setCurrency(Currency.AZN);
        payment.setCommissionAmount(BigDecimal.valueOf(0.40));
        payment.setPaymentStatus(PaymentStatus.PROCESS);

        when(cardRepository.findByPanForUpdate("4169123456789012"))
                .thenReturn(Optional.of(card));
        when(currencyConverter.convertToAZN(any(), any()))
                .thenReturn(BigDecimal.valueOf(500));
        when(currencyConverter.convert(any(), any(), any()))
                .thenReturn(BigDecimal.valueOf(20.40));
        when(limitsConfig.getCardMinBalance())
                .thenReturn(BigDecimal.valueOf(10));
        when(currencyConverter.convertToAZN(BigDecimal.valueOf(20), Currency.AZN))
                .thenReturn(BigDecimal.valueOf(20));
        when(customerRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(customer));
        when(limitsConfig.getMonthlyLimit())
                .thenReturn(BigDecimal.valueOf(100));
        when(transactionRepository.save(any()))
                .thenReturn(new TransactionEntity());
        when(paymentRepository.save(any()))
                .thenReturn(payment);

        transactionService.processPayment(payment);

        assertEquals(PaymentStatus.SUCCESS, payment.getPaymentStatus());
        verify(transactionRepository).save(any());
        verify(paymentRepository).save(any());
    }

    @Test
    void processPayment_CardNotFound_Rejected() {
        CustomerEntity customer = new CustomerEntity();
        customer.setId(1L);

        PaymentEntity payment = new PaymentEntity();
        payment.setCustomer(customer);
        payment.setPaymentSourceType(PaymentSourceType.CARD);
        payment.setFromAccountNumber("4169123456789012");
        payment.setToAccountNumber("AZ2100000000137010001944");
        payment.setAmount(BigDecimal.valueOf(20));
        payment.setCurrency(Currency.AZN);
        payment.setCommissionAmount(BigDecimal.ZERO);
        payment.setPaymentStatus(PaymentStatus.PROCESS);

        when(cardRepository.findByPanForUpdate("4169123456789012"))
                .thenReturn(Optional.empty());
        when(transactionRepository.save(any()))
                .thenReturn(new TransactionEntity());
        when(paymentRepository.save(any()))
                .thenReturn(payment);

        transactionService.processPayment(payment);

        assertEquals(PaymentStatus.REJECTED, payment.getPaymentStatus());
        verify(transactionRepository).save(any());
    }

    @Test
    void processPayment_AccountPayment_Success() {
        CustomerEntity customer = new CustomerEntity();
        customer.setId(1L);
        customer.setMonthlyTurnover(BigDecimal.ZERO);

        CurrentAccountEntity account = new CurrentAccountEntity();
        account.setCurrentAccountStatus(CurrentAccountStatus.ACTIVE);
        account.setBalance(BigDecimal.valueOf(1000));
        account.setCurrency(Currency.AZN);
        account.setExpireDate(LocalDate.now().plusYears(3));

        PaymentEntity payment = new PaymentEntity();
        payment.setCustomer(customer);
        payment.setPaymentSourceType(PaymentSourceType.CURRENT_ACCOUNT);
        payment.setFromAccountNumber("NB12345678901234567890");
        payment.setToAccountNumber("AZ2100000000137010001944");
        payment.setAmount(BigDecimal.valueOf(50));
        payment.setCurrency(Currency.AZN);
        payment.setCommissionAmount(BigDecimal.valueOf(1));
        payment.setPaymentStatus(PaymentStatus.PROCESS);

        when(currentAccountRepository.findByAccountNumberForUpdate("NB12345678901234567890"))
                .thenReturn(Optional.of(account));
        when(currencyConverter.convertToUSD(any(), any()))
                .thenReturn(BigDecimal.valueOf(30));
        when(currencyConverter.convert(any(), any(), any()))
                .thenReturn(BigDecimal.valueOf(51));
        when(limitsConfig.getAccountMinBalance())
                .thenReturn(BigDecimal.valueOf(5));
        when(currencyConverter.convertToAZN(any(), any()))
                .thenReturn(BigDecimal.valueOf(50));
        when(customerRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(customer));
        when(limitsConfig.getMonthlyLimit())
                .thenReturn(BigDecimal.valueOf(100));
        when(transactionRepository.save(any()))
                .thenReturn(new TransactionEntity());
        when(paymentRepository.save(any()))
                .thenReturn(payment);

        transactionService.processPayment(payment);

        assertEquals(PaymentStatus.SUCCESS, payment.getPaymentStatus());
        verify(transactionRepository).save(any());
    }

    @Test
    void processPayment_Fallback_Success() {
        CustomerEntity customer = new CustomerEntity();
        customer.setId(1L);
        customer.setMonthlyTurnover(BigDecimal.ZERO);

        CardEntity card = new CardEntity();
        card.setCardStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.valueOf(3));
        card.setCurrency(Currency.AZN);
        card.setExpireDate(LocalDate.now().plusYears(3));

        CurrentAccountEntity account = new CurrentAccountEntity();
        account.setCurrentAccountStatus(CurrentAccountStatus.ACTIVE);
        account.setBalance(BigDecimal.valueOf(500));
        account.setCurrency(Currency.AZN);
        account.setAccountNumber("NB12345678901234567890");

        PaymentEntity payment = new PaymentEntity();
        payment.setCustomer(customer);
        payment.setPaymentSourceType(PaymentSourceType.CARD);
        payment.setFromAccountNumber("4169123456789012");
        payment.setToAccountNumber("AZ2100000000137010001944");
        payment.setAmount(BigDecimal.valueOf(20));
        payment.setCurrency(Currency.AZN);
        payment.setCommissionAmount(BigDecimal.ZERO);
        payment.setPaymentStatus(PaymentStatus.PROCESS);

        when(cardRepository.findByPanForUpdate("4169123456789012"))
                .thenReturn(Optional.of(card));
        when(currencyConverter.convertToAZN(BigDecimal.valueOf(3), Currency.AZN))
                .thenReturn(BigDecimal.valueOf(3));
        when(limitsConfig.getCardMinBalance())
                .thenReturn(BigDecimal.valueOf(10));
        when(currentAccountRepository.findByCustomerIdAndCurrentAccountStatusForUpdate(1L, CurrentAccountStatus.ACTIVE))
                .thenReturn(List.of(account));
        when(currencyConverter.convertToUSD(any(), any()))
                .thenReturn(BigDecimal.valueOf(300));
        when(currencyConverter.convert(any(), any(), any()))
                .thenReturn(BigDecimal.valueOf(20));
        when(limitsConfig.getAccountMinBalance())
                .thenReturn(BigDecimal.valueOf(5));
        when(currencyConverter.convertToAZN(BigDecimal.valueOf(20), Currency.AZN))
                .thenReturn(BigDecimal.valueOf(20));
        when(customerRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(customer));
        when(limitsConfig.getMonthlyLimit())
                .thenReturn(BigDecimal.valueOf(100));
        when(transactionRepository.save(any()))
                .thenReturn(new TransactionEntity());
        when(paymentRepository.save(any()))
                .thenReturn(payment);

        transactionService.processPayment(payment);

        assertEquals(PaymentStatus.SUCCESS, payment.getPaymentStatus());
        verify(transactionRepository).save(any());
    }
}