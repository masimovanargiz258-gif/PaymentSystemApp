package az.bank.paymentsystem.service;

import az.bank.paymentsystem.enums.*;
import az.bank.paymentsystem.exception.*;
import az.bank.paymentsystem.model.PaymentRequest;
import az.bank.paymentsystem.model.PaymentResponse;
import az.bank.paymentsystem.model.entity.CustomerEntity;
import az.bank.paymentsystem.model.entity.PaymentEntity;
import az.bank.paymentsystem.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;


@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentValidationService validationService;
    private final CommissionService commissionService;

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        CustomerEntity customer = validationService.findAndValidateCustomer(request.getCustomerId());
        validationService.validatePaymentMethod(request.getCustomerId(), request.getPaymentSourceType());
        validationService.validatePaymentSource(request);
        PaymentEntity payment = buildPaymentEntity(request, customer);
        return mapToResponse(paymentRepository.save(payment));
    }

    private PaymentEntity buildPaymentEntity(PaymentRequest request, CustomerEntity customer) {
        BigDecimal commission = commissionService.calculateCommission(request.getAmount(), request.getToAccountNumber());
        PaymentEntity payment = new PaymentEntity();
        payment.setCustomer(customer);
        payment.setPaymentSourceType(request.getPaymentSourceType());
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        payment.setPaymentStatus(PaymentStatus.PROCESS);
        payment.setFromAccountNumber(request.getFromAccountNumber());
        payment.setToAccountNumber(request.getToAccountNumber());
        payment.setTransferType(TransferType.EXTERNAL);
        payment.setCommissionAmount(commission);
        return payment;
    }

    private PaymentResponse mapToResponse(PaymentEntity entity) {
        PaymentResponse response = new PaymentResponse();
        response.setId(entity.getId());
        response.setAmount(entity.getAmount());
        response.setCurrency(entity.getCurrency());
        response.setPaymentStatus(entity.getPaymentStatus());
        response.setPaymentSourceType(entity.getPaymentSourceType());
        response.setFromAccountNumber(entity.getFromAccountNumber());
        response.setToAccountNumber(entity.getToAccountNumber());
        response.setCommissionAmount(entity.getCommissionAmount());
        response.setCreatedAt(entity.getCreatedAt());
        return response;
    }
}