package az.bank.paymentsystem.service;

import az.bank.paymentsystem.config.PaymentLimitsConfig;
import az.bank.paymentsystem.constant.Constant;
import az.bank.paymentsystem.enums.CardStatus;
import az.bank.paymentsystem.enums.CurrentAccountStatus;
import az.bank.paymentsystem.enums.CustomerStatus;
import az.bank.paymentsystem.enums.PaymentSourceType;
import az.bank.paymentsystem.exception.*;
import az.bank.paymentsystem.model.BasePaymentRequest;
import az.bank.paymentsystem.model.PaymentRequest;
import az.bank.paymentsystem.model.entity.CardEntity;
import az.bank.paymentsystem.model.entity.CurrentAccountEntity;
import az.bank.paymentsystem.model.entity.CustomerEntity;
import az.bank.paymentsystem.model.entity.PaymentEntity;
import az.bank.paymentsystem.repository.CardRepository;
import az.bank.paymentsystem.repository.CurrentAccountRepository;
import az.bank.paymentsystem.repository.CustomerRepository;
import az.bank.paymentsystem.repository.PaymentRepository;
import az.bank.paymentsystem.util.CurrencyConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
@Service
@RequiredArgsConstructor
public class PaymentValidationService {
    private final CustomerRepository customerRepository;
    private final CardRepository cardRepository;
    private final CurrentAccountRepository currentAccountRepository;
    private final PaymentRepository paymentRepository;
    private final CurrencyConverter currencyConverter;
    private final PaymentLimitsConfig limitsConfig;
    private final MessageService messageService;

    public CustomerEntity findAndValidateCustomer(Long customerId) {
        CustomerEntity customer = customerRepository.findById(customerId).orElseThrow(() -> new CustomerNotFoundException(messageService.getMessage("customer.not.found")));
        if (customer.getCustomerStatus() == CustomerStatus.SUSPICIOUS) {
            throw new CustomerStatusException(messageService.getMessage("customer.is.suspicious"));
        }
        if (customer.getCustomerStatus() == CustomerStatus.BLOCKED) {
            throw new CustomerStatusException(messageService.getMessage("customer.is.blocked"));
        }
        if (customer.getCustomerStatus() == CustomerStatus.DELETED) {
            throw new CustomerStatusException(messageService.getMessage("customer.is.deleted"));
        }
        return customer;
    }

    public void validatePaymentMethod(Long customerId, PaymentSourceType sourceType) {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        List<PaymentEntity> payments = paymentRepository.findAllByCustomerIdCreatedAtAfter(customerId, startOfToday);
        for (PaymentEntity payment : payments) {
            if (payment.getPaymentSourceType() != sourceType) {
                throw new PaymentMethodLimitException(messageService.getMessage("payment.method.limit"));
            }
        }
    }

    public void validatePaymentSource(PaymentRequest request) {
        if (request.getPaymentSourceType() == PaymentSourceType.CARD) {
            validateCardPayment(request);
        } else if (request.getPaymentSourceType() == PaymentSourceType.CURRENT_ACCOUNT) {
            validateAccountPayment(request);
        }
    }

    public void validateCardPayment(BasePaymentRequest request) {
        CardEntity card = cardRepository.findByPan(request.getFromAccountNumber()).orElseThrow(() -> new CardNotFoundException(messageService.getMessage("card.not.found")));
        if (!card.getCustomer().getId().equals(request.getCustomerId())) {
            throw new CardNotFoundException(messageService.getMessage("card.not.found"));
        }
        validateCardActiveStatus(card);
        if (card.getExpireDate() != null && card.getExpireDate().isBefore(LocalDate.now())) {
            throw new CardStatusException(messageService.getMessage("card.is.expired"));
        }
        BigDecimal cardBalanceInAZN = currencyConverter.convertToAZN(card.getBalance(), card.getCurrency());
        if (cardBalanceInAZN.compareTo(limitsConfig.getCardMinBalance()) < 0) {
            List<CurrentAccountEntity> accounts = currentAccountRepository.findByCustomerIdAndCurrentAccountStatusForUpdate(request.getCustomerId(), CurrentAccountStatus.ACTIVE);
            if (accounts.isEmpty()) {
                throw new InsufficientBalanceException(
                        messageService.getMessage("card.insufficient.balance"));
            }
        }
    }

    public void validateAccountPayment(BasePaymentRequest request) {
        CurrentAccountEntity account = currentAccountRepository.findByAccountNumber(request.getFromAccountNumber()).orElseThrow(() -> new AccountNotFoundException(messageService.getMessage("account.not.found")));
        if (!account.getCustomer().getId().equals(request.getCustomerId())) {
            throw new AccountNotFoundException(messageService.getMessage("account.not.found"));
        }
        validateAccountActiveStatus(account);
        BigDecimal accountBalanceInUSD = currencyConverter.convertToUSD(account.getBalance(), account.getCurrency());
        if (accountBalanceInUSD.compareTo(limitsConfig.getAccountMinBalance()) < 0) {
            throw new InsufficientBalanceException(messageService.getMessage("account.insufficient.balance"));
        }
        BigDecimal amountToDeduct = currencyConverter.convert(request.getAmount(), request.getCurrency(), account.getCurrency());
        if (account.getBalance().compareTo(amountToDeduct) < 0) {
            throw new InsufficientBalanceException(messageService.getMessage("insufficient.funds"));
        }

    }
    public void validateToCard(String toAccountNumber) {
        if (!toAccountNumber.startsWith(Constant.BIN_PREFIX)) return;
        cardRepository.findByPan(toAccountNumber).ifPresent(card -> {
            if (card.getCardStatus() == CardStatus.BLOCKED) {
                throw new CardStatusException(messageService.getMessage("card.receiver.is.blocked"));
            }
            if (card.getCardStatus() == CardStatus.CANCELED) {
                throw new CardStatusException(messageService.getMessage("card.receiver.is.canceled"));
            }
            if (card.getCardStatus() == CardStatus.EXPIRE) {
                throw new CardStatusException(messageService.getMessage("card.receiver.is.expired"));
            }
            if (card.getCardStatus() == CardStatus.NEW) {
                throw new CardStatusException(messageService.getMessage("card.receiver.is.not.active"));
            }
        });
    }

    public void validateToAccount(String toAccountNumber) {
        if (!toAccountNumber.startsWith(Constant.ACCOUNT_PREFIX)) return;
        currentAccountRepository.findByAccountNumber(toAccountNumber).ifPresent(account -> {
            if (account.getCurrentAccountStatus() == CurrentAccountStatus.CANCELED) {
                throw new AccountStatusException(messageService.getMessage("account.receiver.is.canceled"));
            }
            if (account.getCurrentAccountStatus() == CurrentAccountStatus.BLOCKED) {
                throw new AccountStatusException(messageService.getMessage("account.receiver.is.blocked"));
            }
            if (account.getCurrentAccountStatus() == CurrentAccountStatus.EXPIRED) {
                throw new AccountStatusException(messageService.getMessage("account.receiver.is.expired"));
            }
            if (account.getCurrentAccountStatus() == CurrentAccountStatus.NEW) {
                throw new AccountStatusException(messageService.getMessage("account.receiver.is.not.active"));
            }
        });

    }

    private void validateCardActiveStatus(CardEntity card) {
        if (card.getCardStatus() == CardStatus.CANCELED) {
            throw new CardStatusException(messageService.getMessage("card.is.canceled"));
        }
        if (card.getCardStatus() == CardStatus.EXPIRE) {
            throw new CardStatusException(messageService.getMessage("card.is.expired"));
        }
        if (card.getCardStatus() == CardStatus.BLOCKED) {
            throw new CardStatusException(messageService.getMessage("card.is.blocked"));
        }
        if (card.getCardStatus() == CardStatus.NEW) {
            throw new CardStatusException(messageService.getMessage("card.is.new"));
        }
        if (card.getExpireDate() != null && card.getExpireDate().isBefore(LocalDate.now())) {
            throw new CardStatusException(messageService.getMessage("card.is.expired"));
        }
    }

    public void validateAccountActiveStatus(CurrentAccountEntity account) {
        if (account.getCurrentAccountStatus() == CurrentAccountStatus.CANCELED) {
            throw new AccountStatusException(messageService.getMessage("account.is.already.canceled"));
        }
        if (account.getCurrentAccountStatus() == CurrentAccountStatus.BLOCKED) {
            throw new AccountStatusException(messageService.getMessage("account.is.blocked"));
        }
        if (account.getCurrentAccountStatus() == CurrentAccountStatus.NEW) {
            throw new AccountStatusException(messageService.getMessage("account.is.not.active"));
        }
        if(account.getCurrentAccountStatus() == CurrentAccountStatus.EXPIRED) {
            throw new AccountStatusException(messageService.getMessage("account.is.expired"));
        }
        if (account.getExpireDate() != null && account.getExpireDate().isBefore(LocalDate.now())) {
            throw new AccountStatusException(messageService.getMessage("account.is.expired"));
        }

    }
}