package az.bank.paymentsystem.service;

import az.bank.paymentsystem.config.PaymentLimitsConfig;
import az.bank.paymentsystem.enums.CurrentAccountStatus;
import az.bank.paymentsystem.enums.OrderStatus;
import az.bank.paymentsystem.exception.AccountLimitException;
import az.bank.paymentsystem.exception.CustomerNotFoundException;
import az.bank.paymentsystem.model.AccountOrderRequest;
import az.bank.paymentsystem.model.CurrentAccountResponse;
import az.bank.paymentsystem.model.entity.AccountOrderEntity;
import az.bank.paymentsystem.model.entity.CurrentAccountEntity;
import az.bank.paymentsystem.model.entity.CustomerEntity;
import az.bank.paymentsystem.repository.AccountOrderRepository;
import az.bank.paymentsystem.repository.CurrentAccountRepository;
import az.bank.paymentsystem.repository.CustomerRepository;
import az.bank.paymentsystem.util.CustomerValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Random;

import static az.bank.paymentsystem.constant.Constant.ACCOUNT_NUMBER_LENGTH;
import static az.bank.paymentsystem.constant.Constant.ACCOUNT_PREFIX;

@Service
@RequiredArgsConstructor
public class AccountOrderService {
    private final AccountOrderRepository accountOrderRepository;
    private final CurrentAccountRepository currentAccountRepository;
    private final CurrentAccountService currentAccountService;
    private final CustomerRepository customerRepository;
    private final MessageService messageService;
    private final PaymentLimitsConfig limitsConfig;
    private final CustomerValidationUtils customerValidationUtils;
    @Transactional
    public CurrentAccountResponse orderCurrentAccount(AccountOrderRequest request) {
        CustomerEntity customer = customerRepository.findById(request.getCustomerId()).orElseThrow(() -> new CustomerNotFoundException(messageService.getMessage("customer.not.found")));
        customerValidationUtils.validateStatus(customer);
        customerValidationUtils.validateAge(customer);

        AccountOrderEntity order = new AccountOrderEntity();
        order.setCustomer(customer);
        order.setCurrency(request.getCurrency());

        long accountCount = currentAccountRepository.countByCustomerId(request.getCustomerId());
        if (accountCount >= limitsConfig.getAccountLimit()) {
            throw new AccountLimitException(messageService.getMessage("customer.account.limit"));
        }

        order.setOrderStatus(OrderStatus.APPROVED);
        CurrentAccountEntity account = createAccount(order, customer);
        accountOrderRepository.save(order);
        return currentAccountService.getCurrentAccountById(account.getId());
    }

    private CurrentAccountEntity createAccount(AccountOrderEntity order, CustomerEntity customer) {
        return currentAccountRepository.save(buildAccountEntity(order, customer));
    }

    private CurrentAccountEntity buildAccountEntity(AccountOrderEntity order, CustomerEntity customer) {
        CurrentAccountEntity account = new CurrentAccountEntity();
        account.setCustomer(customer);
        account.setCurrency(order.getCurrency());
        account.setBalance(BigDecimal.ZERO);
        account.setCurrentAccountStatus(CurrentAccountStatus.NEW);
        account.setActivationDate(LocalDate.now());
        account.setExpireDate(LocalDate.now().plusYears(3));
        account.setAccountNumber(generateUniqueAccountNumber());
        return account;
    }
    private String generateUniqueAccountNumber() {
        Random random = new Random();
        String accountNumber;
        boolean exists;
        do {
            StringBuilder sb = new StringBuilder(ACCOUNT_PREFIX);
            for (int i = 0; i < ACCOUNT_NUMBER_LENGTH; i++) {
                sb.append(random.nextInt(10));
            }
            accountNumber = sb.toString();
            exists = currentAccountRepository.existsByAccountNumber(accountNumber);
        } while (exists);
        return accountNumber;
    }
}