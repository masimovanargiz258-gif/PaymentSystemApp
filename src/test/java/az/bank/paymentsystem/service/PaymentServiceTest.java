package az.bank.paymentsystem.service;

import az.bank.paymentsystem.enums.*;
import az.bank.paymentsystem.exception.CustomerStatusException;
import az.bank.paymentsystem.model.PaymentResponse;
import az.bank.paymentsystem.model.entity.CustomerEntity;
import az.bank.paymentsystem.model.entity.PaymentEntity;
import az.bank.paymentsystem.model.PaymentRequest;
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
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentValidationService validationService;
    @Mock
    private CommissionService commissionService;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void createPayment_Success() {
        CustomerEntity customer = new CustomerEntity();
        customer.setId(1L);
        customer.setCustomerStatus(CustomerStatus.ACTIVE);

        PaymentRequest request = new PaymentRequest();
        request.setCustomerId(1L);
        request.setAmount(BigDecimal.valueOf(20));
        request.setCurrency(Currency.AZN);
        request.setPaymentSourceType(PaymentSourceType.CARD);
        request.setFromAccountNumber("4169123456789012");
        request.setToAccountNumber("AZ21NABZ00000000137010001944");

        PaymentEntity savedPayment = new PaymentEntity();
        savedPayment.setId(1L);
        savedPayment.setAmount(BigDecimal.valueOf(20));
        savedPayment.setCurrency(Currency.AZN);
        savedPayment.setPaymentStatus(PaymentStatus.PROCESS);
        savedPayment.setPaymentSourceType(PaymentSourceType.CARD);
        savedPayment.setFromAccountNumber("4169123456789012");
        savedPayment.setToAccountNumber("AZ21NABZ00000000137010001944");
        savedPayment.setCommissionAmount(BigDecimal.valueOf(0.40));

        when(validationService.findAndValidateCustomer(1L))
                .thenReturn(customer);
        doNothing().when(validationService).validatePaymentMethod(any(), any());
        doNothing().when(validationService).validatePaymentSource(any());
        when(commissionService.calculateCommission(any(), any()))
                .thenReturn(BigDecimal.valueOf(0.40));
        when(paymentRepository.save(any()))
                .thenReturn(savedPayment);

        PaymentResponse response = paymentService.createPayment(request);

        assertNotNull(response);
        assertEquals(PaymentStatus.PROCESS, response.getPaymentStatus());
        assertEquals(BigDecimal.valueOf(0.40), response.getCommissionAmount());
        verify(paymentRepository).save(any());
    }

    @Test
    void createPayment_BlockedCustomer_ThrowsException() {
        PaymentRequest request = new PaymentRequest();
        request.setCustomerId(1L);
        request.setAmount(BigDecimal.valueOf(20));
        request.setCurrency(Currency.AZN);
        request.setPaymentSourceType(PaymentSourceType.CARD);
        request.setFromAccountNumber("4169123456789012");
        request.setToAccountNumber("AZ21NABZ00000000137010001944");

        when(validationService.findAndValidateCustomer(1L))
                .thenThrow(new CustomerStatusException("Customer is blocked"));

        assertThrows(CustomerStatusException.class,
                () -> paymentService.createPayment(request));

        verify(paymentRepository, never()).save(any());
    }
}