package az.bank.paymentsystem.service;

import az.bank.paymentsystem.config.PaymentLimitsConfig;
import az.bank.paymentsystem.constant.Constant;
import az.bank.paymentsystem.enums.*;
import az.bank.paymentsystem.exception.CustomerNotFoundException;
import az.bank.paymentsystem.model.TransactionResponse;
import az.bank.paymentsystem.model.entity.*;
import az.bank.paymentsystem.repository.*;
import az.bank.paymentsystem.util.CurrencyConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
@Service
@RequiredArgsConstructor
public class TransactionService {
    private final PaymentRepository paymentRepository;
    private final TransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;
    private final CardRepository cardRepository;
    private final CurrentAccountRepository currentAccountRepository;
    private final CurrencyConverter currencyConverter;
    private final PaymentLimitsConfig limitsConfig;
    private final MessageService messageService;
    private TransactionService self;
    @Autowired
    @Lazy
    public void setSelf(TransactionService self){
        this.self = self;
    }
    public void processAllPendingPayments() {
        List<PaymentEntity> payments = paymentRepository.findAllByPaymentStatus(PaymentStatus.PROCESS);
        payments.forEach(payment -> self.processPayment(payment));
    }
    @Transactional
    public void processPayment(PaymentEntity payment) {
        TransactionEntity transaction = buildTransaction(payment);
        try {
            if (payment.getPaymentSourceType() == PaymentSourceType.CARD) {
                processCardPayment(payment, transaction);
            } else if (payment.getPaymentSourceType() == PaymentSourceType.CURRENT_ACCOUNT) {
                processAccountPayment(payment, transaction);
            }
            if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
                updateMonthlyTurnover(payment);
            }
        } catch (Exception e) {
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            payment.setPaymentStatus(PaymentStatus.REJECTED);
        }
        transactionRepository.save(transaction);
        paymentRepository.save(payment);
    }

    private TransactionEntity buildTransaction(PaymentEntity payment) {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setPayment(payment);
        transaction.setAmount(payment.getAmount());
        transaction.setCurrency(payment.getCurrency());
        transaction.setPaymentSourceType(payment.getPaymentSourceType());
        transaction.setFromAccountNumber(payment.getFromAccountNumber());
        transaction.setToAccountNumber(payment.getToAccountNumber());
        transaction.setAmountInAzn(currencyConverter.convertToAZN(payment.getAmount(), payment.getCurrency()));
        transaction.setCommissionAmount(payment.getCommissionAmount());
        return transaction;
    }

    private void processCardPayment(PaymentEntity payment, TransactionEntity transaction) {
        CardEntity card = cardRepository.findByPanForUpdate(payment.getFromAccountNumber()).orElse(null);
        if (card == null || card.getCardStatus() != CardStatus.ACTIVE) {
            handleFailure(transaction, payment);
            return;
        }
        if (card.getExpireDate() != null && card.getExpireDate().isBefore(LocalDate.now())) {
            handleFailure(transaction, payment);
            return;
        }
        BigDecimal cardBalanceInAZN = currencyConverter.convertToAZN(card.getBalance(), card.getCurrency());
        BigDecimal totalAmount = payment.getAmount().add(payment.getCommissionAmount());
        BigDecimal amountToDeduct = currencyConverter.convert(totalAmount, payment.getCurrency(), card.getCurrency());
        boolean isCardLimitPassed = cardBalanceInAZN.compareTo(limitsConfig.getCardMinBalance()) >= 0;
        boolean hasEnoughMoney = card.getBalance().compareTo(amountToDeduct) >= 0;
        if (!isCardLimitPassed || !hasEnoughMoney) {
            processFallback(payment, transaction);
        } else {
            card.setBalance(card.getBalance().subtract(amountToDeduct));
            cardRepository.save(card);
            transaction.setBalanceAfter(card.getBalance());
            handleSuccess(transaction, payment);
        }
    }


    private void processAccountPayment(PaymentEntity payment, TransactionEntity transaction) {
        CurrentAccountEntity account = currentAccountRepository.findByAccountNumberForUpdate(payment.getFromAccountNumber()).orElse(null);
        if (account == null || account.getCurrentAccountStatus() != CurrentAccountStatus.ACTIVE) {
            handleFailure(transaction, payment);
            return;
        }
        if (account.getExpireDate() != null && account.getExpireDate().isBefore(LocalDate.now())) {
            handleFailure(transaction, payment);
            return;
        }
        BigDecimal accountBalanceInUSD = currencyConverter.convertToUSD(account.getBalance(), account.getCurrency());
        BigDecimal totalAmount = payment.getAmount().add(payment.getCommissionAmount());
        BigDecimal amountToDeduct = currencyConverter.convert(totalAmount, payment.getCurrency(), account.getCurrency());
        boolean isLimitPassed = accountBalanceInUSD.compareTo(limitsConfig.getAccountMinBalance()) >= 0;
        boolean hasEnoughMoney = account.getBalance().compareTo(amountToDeduct) >= 0;

        if (!isLimitPassed || !hasEnoughMoney) {
            handleFailure(transaction, payment);
        } else {
            account.setBalance(account.getBalance().subtract(amountToDeduct));
            currentAccountRepository.save(account);
            transaction.setBalanceAfter(account.getBalance());
            handleSuccess(transaction, payment);
        }
    }


    private void processFallback(PaymentEntity payment, TransactionEntity transaction) {
        List<CurrentAccountEntity> accounts = currentAccountRepository.findByCustomerIdAndCurrentAccountStatusForUpdate(payment.getCustomer().getId(), CurrentAccountStatus.ACTIVE);
        BigDecimal totalAmount = payment.getAmount().add(payment.getCommissionAmount());
        boolean success = false;
        for (CurrentAccountEntity account : accounts) {
            BigDecimal accountBalanceInUSD = currencyConverter.convertToUSD(account.getBalance(), account.getCurrency());
            BigDecimal amountToDeduct = currencyConverter.convert(totalAmount, payment.getCurrency(), account.getCurrency());
            boolean isLimitPassed = accountBalanceInUSD.compareTo(limitsConfig.getAccountMinBalance()) >= 0;
            boolean hasEnoughMoney = account.getBalance().compareTo(amountToDeduct) >= 0;
            if (isLimitPassed && hasEnoughMoney) {
                account.setBalance(account.getBalance().subtract(amountToDeduct));
                currentAccountRepository.save(account);
                transaction.setBalanceAfter(account.getBalance());
                transaction.setIsFallback(true);
                handleSuccess(transaction, payment);
                success = true;
                break;
            }
        }
        if (!success) {
            handleFailure(transaction, payment);
        }
    }
    private void creditToDestination(PaymentEntity payment){
        String toAccountNumber = payment.getToAccountNumber();
        if (toAccountNumber.startsWith(Constant.BIN_PREFIX)) {
            cardRepository.findByPanForUpdate(toAccountNumber).ifPresent(card -> {
                if (card.getCardStatus() == CardStatus.ACTIVE) {
                    BigDecimal amountToCredit = currencyConverter.convert(payment.getAmount(), payment.getCurrency(), card.getCurrency());
                    card.setBalance(card.getBalance().add(amountToCredit));
                    cardRepository.save(card);
                }
            });
        } else if (toAccountNumber.startsWith(Constant.ACCOUNT_PREFIX)) {
            currentAccountRepository.findByAccountNumberForUpdate(toAccountNumber).ifPresent(account -> {
                if (account.getCurrentAccountStatus() == CurrentAccountStatus.ACTIVE || account.getCurrentAccountStatus() == CurrentAccountStatus.NEW) {
                    BigDecimal amountToCredit = currencyConverter.convert(payment.getAmount(), payment.getCurrency(), account.getCurrency());
                    if (account.getCurrentAccountStatus() == CurrentAccountStatus.NEW) {
                        account.setCurrentAccountStatus(CurrentAccountStatus.ACTIVE);
                    }
                    account.setBalance(account.getBalance().add(amountToCredit));
                    currentAccountRepository.save(account);
                }
            });
        }
    }

    private void handleSuccess(TransactionEntity transaction, PaymentEntity payment) {
        transaction.setTransactionStatus(TransactionStatus.SUCCESS);
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        creditToDestination(payment);
    }

    private void handleFailure(TransactionEntity transaction, PaymentEntity payment) {
        transaction.setTransactionStatus(TransactionStatus.REJECTED);
        payment.setPaymentStatus(PaymentStatus.REJECTED);
    }

    private void updateMonthlyTurnover(PaymentEntity payment) {
        CustomerEntity customer = customerRepository.findByIdForUpdate(payment.getCustomer().getId()).orElseThrow(() -> new CustomerNotFoundException(messageService.getMessage("customer.not.found")));
        BigDecimal monthlyTotal;
        if (customer.getMonthlyTurnover() != null) {
            monthlyTotal = customer.getMonthlyTurnover();
        } else {
            monthlyTotal = BigDecimal.ZERO;
        }
        BigDecimal currentPaymentAzn = currencyConverter.convertToAZN(payment.getAmount(), payment.getCurrency());
        monthlyTotal = monthlyTotal.add(currentPaymentAzn);
        if (monthlyTotal.compareTo(limitsConfig.getMonthlyLimit()) > 0) {
            blockCustomerAndAssets(customer);
        }
        customer.setMonthlyTurnover(monthlyTotal);
        customerRepository.save(customer);
    }


    private void blockCustomerAndAssets(CustomerEntity customer) {
        customer.setCustomerStatus(CustomerStatus.SUSPICIOUS);
        if (customer.getCards() != null && !customer.getCards().isEmpty()) {
            List<CardEntity> cardsToBlock = customer.getCards().stream()
                    .filter(c -> c.getCardStatus() != CardStatus.BLOCKED
                            && c.getCardStatus() != CardStatus.CANCELED)
                    .peek(c -> c.setCardStatus(CardStatus.BLOCKED))
                    .toList();
            cardRepository.saveAll(cardsToBlock);
        }
        if (customer.getCurrentAccounts() != null && !customer.getCurrentAccounts().isEmpty()) {
            List<CurrentAccountEntity> accountsToBlock = customer.getCurrentAccounts().stream()
                    .filter(a -> a.getCurrentAccountStatus() != CurrentAccountStatus.CANCELED
                            && a.getCurrentAccountStatus() != CurrentAccountStatus.BLOCKED)
                    .peek(a -> a.setCurrentAccountStatus(CurrentAccountStatus.BLOCKED))
                    .toList();
            currentAccountRepository.saveAll(accountsToBlock);
        }
        customerRepository.save(customer);
    }

    public List<TransactionResponse> getLast100ByCard(String pan) {
        return transactionRepository.findTop100ByAccountNumber(pan, PaymentSourceType.CARD).stream().map(this::mapToResponse).toList();
    }

    public List<TransactionResponse> getLast100ByCurrentAccount(String accountNumber) {
        return transactionRepository.findTop100ByAccountNumber(accountNumber, PaymentSourceType.CURRENT_ACCOUNT).stream().map(this::mapToResponse).toList();
    }

    private TransactionResponse mapToResponse(TransactionEntity entity) {
        TransactionResponse response = new TransactionResponse();
        response.setId(entity.getId());
        response.setAmount(entity.getAmount());
        response.setCurrency(entity.getCurrency());
        response.setPaymentSourceType(entity.getPaymentSourceType());
        response.setTransactionStatus(entity.getTransactionStatus());
        response.setTransactionDate(entity.getTransactionDate());
        response.setFromAccountNumber(entity.getFromAccountNumber());
        response.setToAccountNumber(entity.getToAccountNumber());
        response.setIsFallback(entity.getIsFallback());
        response.setBalanceAfter(entity.getBalanceAfter());
        response.setCommissionAmount(entity.getCommissionAmount());
        return response;
    }
}