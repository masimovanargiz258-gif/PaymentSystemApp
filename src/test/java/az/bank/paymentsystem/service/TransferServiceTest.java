package az.bank.paymentsystem.service;

import az.bank.paymentsystem.enums.*;
import az.bank.paymentsystem.exception.InvalidTransferException;
import az.bank.paymentsystem.model.TransferResponse;
import az.bank.paymentsystem.model.entity.CustomerEntity;
import az.bank.paymentsystem.model.entity.PaymentEntity;
import az.bank.paymentsystem.model.TransferRequest;
import az.bank.paymentsystem.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentValidationService validationService;
    @Mock
    private MessageService messageService;
    @Mock
    private CommissionService commissionService;

    @InjectMocks
    private TransferService transferService;

    @Test
    void cardToCard_Success() {
        CustomerEntity customer = new CustomerEntity();
        customer.setId(1L);

        TransferRequest request = new TransferRequest();
        request.setCustomerId(1L);
        request.setAmount(BigDecimal.valueOf(10));
        request.setCurrency(Currency.AZN);
        request.setFromAccountNumber("4169111111111111");
        request.setToAccountNumber("4169222222222222");

        PaymentEntity savedPayment = new PaymentEntity();
        savedPayment.setId(1L);
        savedPayment.setAmount(BigDecimal.valueOf(10));
        savedPayment.setCurrency(Currency.AZN);
        savedPayment.setPaymentStatus(PaymentStatus.PROCESS);
        savedPayment.setTransferType(TransferType.CARD_TO_CARD);
        savedPayment.setFromAccountNumber("4169111111111111");
        savedPayment.setToAccountNumber("4169222222222222");
        savedPayment.setCommissionAmount(BigDecimal.ZERO);

        when(validationService.findAndValidateCustomer(1L))
                .thenReturn(customer);
        doNothing().when(validationService).validatePaymentMethod(any(), any());
        doNothing().when(validationService).validateCardPayment(any());
        doNothing().when(validationService).validateToCard(any());
        when(commissionService.calculateCommission(any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(paymentRepository.save(any()))
                .thenReturn(savedPayment);

        TransferResponse response = transferService.cardToCard(request);

        assertNotNull(response);
        assertEquals(PaymentStatus.PROCESS, response.getPaymentStatus());
        assertEquals(TransferType.CARD_TO_CARD, response.getTransferType());
        verify(paymentRepository).save(any());
    }

    @Test
    void cardToCard_SameAccount_ThrowsException() {
        TransferRequest request = new TransferRequest();
        request.setCustomerId(1L);
        request.setAmount(BigDecimal.valueOf(10));
        request.setCurrency(Currency.AZN);
        request.setFromAccountNumber("4169111111111111");
        request.setToAccountNumber("4169111111111111");

        when(messageService.getMessage(any()))
                .thenReturn("Cannot transfer to same account");

        assertThrows(InvalidTransferException.class,
                () -> transferService.cardToCard(request));

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void accountToAccount_Success() {
        CustomerEntity customer = new CustomerEntity();
        customer.setId(1L);

        TransferRequest request = new TransferRequest();
        request.setCustomerId(1L);
        request.setAmount(BigDecimal.valueOf(50));
        request.setCurrency(Currency.AZN);
        request.setFromAccountNumber("NB11111111111111111111");
        request.setToAccountNumber("NB22222222222222222222");

        PaymentEntity savedPayment = new PaymentEntity();
        savedPayment.setId(1L);
        savedPayment.setAmount(BigDecimal.valueOf(50));
        savedPayment.setCurrency(Currency.AZN);
        savedPayment.setPaymentStatus(PaymentStatus.PROCESS);
        savedPayment.setTransferType(TransferType.ACCOUNT_TO_ACCOUNT);
        savedPayment.setFromAccountNumber("NB11111111111111111111");
        savedPayment.setToAccountNumber("NB22222222222222222222");
        savedPayment.setCommissionAmount(BigDecimal.ZERO);

        when(validationService.findAndValidateCustomer(1L))
                .thenReturn(customer);
        doNothing().when(validationService).validatePaymentMethod(any(), any());
        doNothing().when(validationService).validateAccountPayment(any());
        doNothing().when(validationService).validateToAccount(any());
        when(commissionService.calculateCommission(any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(paymentRepository.save(any()))
                .thenReturn(savedPayment);

        TransferResponse response = transferService.accountToAccount(request);

        assertNotNull(response);
        assertEquals(PaymentStatus.PROCESS, response.getPaymentStatus());
        assertEquals(TransferType.ACCOUNT_TO_ACCOUNT, response.getTransferType());
        verify(paymentRepository).save(any());
    }

    @Test
    void external_WithCommission_Success() {
        CustomerEntity customer = new CustomerEntity();
        customer.setId(1L);

        TransferRequest request = new TransferRequest();
        request.setCustomerId(1L);
        request.setAmount(BigDecimal.valueOf(100));
        request.setCurrency(Currency.AZN);
        request.setFromAccountNumber("4169111111111111");
        request.setToAccountNumber("5123456789012345");

        PaymentEntity savedPayment = new PaymentEntity();
        savedPayment.setId(1L);
        savedPayment.setAmount(BigDecimal.valueOf(100));
        savedPayment.setCurrency(Currency.AZN);
        savedPayment.setPaymentStatus(PaymentStatus.PROCESS);
        savedPayment.setTransferType(TransferType.EXTERNAL);
        savedPayment.setFromAccountNumber("4169111111111111");
        savedPayment.setToAccountNumber("5123456789012345");
        savedPayment.setCommissionAmount(BigDecimal.valueOf(2));

        when(validationService.findAndValidateCustomer(1L))
                .thenReturn(customer);
        doNothing().when(validationService).validatePaymentMethod(any(), any());
        doNothing().when(validationService).validateCardPayment(any());
        when(commissionService.calculateCommission(any(), any()))
                .thenReturn(BigDecimal.valueOf(2));
        when(paymentRepository.save(any()))
                .thenReturn(savedPayment);

        TransferResponse response = transferService.external(request);

        assertNotNull(response);
        assertEquals(TransferType.EXTERNAL, response.getTransferType());
        verify(paymentRepository).save(any());
    }
}