package az.bank.paymentsystem.service;

import az.bank.paymentsystem.constant.Constant;
import az.bank.paymentsystem.enums.PaymentSourceType;
import az.bank.paymentsystem.enums.PaymentStatus;
import az.bank.paymentsystem.enums.TransferType;
import az.bank.paymentsystem.exception.InvalidTransferException;
import az.bank.paymentsystem.model.TransferRequest;
import az.bank.paymentsystem.model.TransferResponse;
import az.bank.paymentsystem.model.entity.CustomerEntity;
import az.bank.paymentsystem.model.entity.PaymentEntity;
import az.bank.paymentsystem.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final PaymentRepository paymentRepository;
    private final PaymentValidationService validationService;
    private final MessageService messageService;
    private final CommissionService commissionService;

    @Transactional
    public TransferResponse cardToCard(TransferRequest request) {
        validateNotSameAccount(request);
        CustomerEntity customer = validationService.findAndValidateCustomer(request.getCustomerId());
        validationService.validatePaymentMethod(request.getCustomerId(), PaymentSourceType.CARD);
        validationService.validateCardPayment(request);
        validationService.validateToCard(request.getToAccountNumber());
        return saveAndMap(request, customer, TransferType.CARD_TO_CARD, PaymentSourceType.CARD);
    }

    @Transactional
    public TransferResponse cardToAccount(TransferRequest request) {
        validateNotSameAccount(request);
        CustomerEntity customer = validationService.findAndValidateCustomer(request.getCustomerId());
        validationService.validatePaymentMethod(request.getCustomerId(), PaymentSourceType.CARD);
        validationService.validateCardPayment(request);
        validationService.validateToAccount(request.getToAccountNumber());
        return saveAndMap(request, customer, TransferType.CARD_TO_ACCOUNT, PaymentSourceType.CARD);
    }

    @Transactional
    public TransferResponse accountToAccount(TransferRequest request) {
        validateNotSameAccount(request);
        CustomerEntity customer = validationService.findAndValidateCustomer(request.getCustomerId());
        validationService.validatePaymentMethod(request.getCustomerId(), PaymentSourceType.CURRENT_ACCOUNT);
        validationService.validateAccountPayment(request);
        validationService.validateToAccount(request.getToAccountNumber());
        return saveAndMap(request, customer, TransferType.ACCOUNT_TO_ACCOUNT, PaymentSourceType.CURRENT_ACCOUNT);
    }

    @Transactional
    public TransferResponse accountToCard(TransferRequest request) {
        validateNotSameAccount(request);
        CustomerEntity customer = validationService.findAndValidateCustomer(request.getCustomerId());
        validationService.validatePaymentMethod(request.getCustomerId(), PaymentSourceType.CURRENT_ACCOUNT);
        validationService.validateAccountPayment(request);
        validationService.validateToCard(request.getToAccountNumber());
        return saveAndMap(request, customer, TransferType.ACCOUNT_TO_CARD, PaymentSourceType.CURRENT_ACCOUNT);
    }

    @Transactional
    public TransferResponse external(TransferRequest request) {
        validateNotSameAccount(request);
        CustomerEntity customer = validationService.findAndValidateCustomer(request.getCustomerId());
        PaymentSourceType sourceType;
        if (request.getFromAccountNumber().startsWith(Constant.BIN_PREFIX)) {
            sourceType = PaymentSourceType.CARD;
        } else {
            sourceType = PaymentSourceType.CURRENT_ACCOUNT;
        }
        validationService.validatePaymentMethod(request.getCustomerId(), sourceType);
        if (sourceType == PaymentSourceType.CARD) {
            validationService.validateCardPayment(request);
        } else {
            validationService.validateAccountPayment(request);
        }

        return saveAndMap(request, customer, TransferType.EXTERNAL, sourceType);
    }

    private TransferResponse saveAndMap(TransferRequest request, CustomerEntity customer, TransferType transferType, PaymentSourceType sourceType) {
        return mapToResponse(paymentRepository.save(buildPayment(request, customer, transferType, sourceType)));
    }
    private void validateNotSameAccount(TransferRequest request) {
        if (request.getFromAccountNumber().equals(request.getToAccountNumber())) {
            throw new InvalidTransferException(messageService.getMessage("transfer.same.account"));
        }
    }

    private PaymentEntity buildPayment(TransferRequest request, CustomerEntity customer, TransferType transferType, PaymentSourceType sourceType) {
        BigDecimal commission = commissionService.calculateCommission(request.getAmount(), request.getToAccountNumber());
        PaymentEntity payment = new PaymentEntity();
        payment.setCustomer(customer);
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        payment.setPaymentStatus(PaymentStatus.PROCESS);
        payment.setPaymentSourceType(sourceType);
        payment.setTransferType(transferType);
        payment.setFromAccountNumber(request.getFromAccountNumber());
        payment.setToAccountNumber(request.getToAccountNumber());
        payment.setCommissionAmount(commission);
        return payment;
    }

    private TransferResponse mapToResponse(PaymentEntity entity) {
        TransferResponse response = new TransferResponse();
        response.setId(entity.getId());
        response.setAmount(entity.getAmount());
        response.setCurrency(entity.getCurrency());
        response.setPaymentStatus(entity.getPaymentStatus());
        response.setTransferType(entity.getTransferType());
        response.setFromAccountNumber(entity.getFromAccountNumber());
        response.setToAccountNumber(entity.getToAccountNumber());
        response.setCreatedAt(entity.getCreatedAt());
        return response;
    }
}
