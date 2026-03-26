package az.bank.paymentsystem.service;

import az.bank.paymentsystem.model.CurrentAccountDepositRequest;
import az.bank.paymentsystem.model.entity.CurrentAccountEntity;
import az.bank.paymentsystem.enums.CurrentAccountStatus;
import az.bank.paymentsystem.exception.*;
import az.bank.paymentsystem.model.CurrentAccountResponse;
import az.bank.paymentsystem.repository.CurrentAccountRepository;
import az.bank.paymentsystem.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class CurrentAccountService {
    private final CurrentAccountRepository currentAccountRepository;
    private final CustomerRepository customerRepository;
    private final MessageService messageService;

    public CurrentAccountResponse getCurrentAccountById(Long id) {
        return mapToResponse(findAccountById(id));
    }

    public List<CurrentAccountResponse> getAllCurrentAccounts(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException(messageService.getMessage("customer.not.found"));
        }
        return currentAccountRepository.findByCustomerId(customerId).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

   @Transactional
    public CurrentAccountResponse cancelCurrentAccount(Long id) {
        CurrentAccountEntity account = findAccountById(id);
        validateAccountStatus(account);
        account.setCurrentAccountStatus(CurrentAccountStatus.CANCELED);
        return mapToResponse(currentAccountRepository.save(account));
    }

    @Transactional
    public CurrentAccountResponse deposit(Long id, CurrentAccountDepositRequest request) {
        CurrentAccountEntity account = findAccountById(id);
        validateAccountStatus(account);
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InsufficientAmountException(messageService.getMessage("insufficient.amount"));
        }
        if (account.getCurrentAccountStatus() == CurrentAccountStatus.NEW) {
            account.setCurrentAccountStatus(CurrentAccountStatus.ACTIVE);
        }
        account.setBalance(account.getBalance().add(request.getAmount()));
        return mapToResponse(currentAccountRepository.save(account));
    }

    @Transactional
    public CurrentAccountResponse unblockAccount(Long id) {
        CurrentAccountEntity account = findAccountById(id);
        if (account.getCurrentAccountStatus() != CurrentAccountStatus.BLOCKED) {
            throw new AccountStatusException(messageService.getMessage("account.is.not.blocked"));
        }
        account.setCurrentAccountStatus(CurrentAccountStatus.ACTIVE);
        return mapToResponse(currentAccountRepository.save(account));
    }

    private CurrentAccountEntity findAccountById(Long id) {
        return currentAccountRepository.findWithCustomerById(id).orElseThrow(() -> new AccountNotFoundException(messageService.getMessage("account.not.found")));
    }

    public void validateAccountStatus(CurrentAccountEntity account) {
        if (account.getCurrentAccountStatus() == CurrentAccountStatus.CANCELED) {
            throw new AccountStatusException(messageService.getMessage("account.is.already.canceled"));
        }
        if (account.getCurrentAccountStatus() == CurrentAccountStatus.BLOCKED) {
            throw new AccountStatusException(messageService.getMessage("account.is.blocked"));
        }
        if(account.getCurrentAccountStatus() == CurrentAccountStatus.EXPIRED) {
            throw new AccountStatusException(messageService.getMessage("account.is.expired"));
        }
        if (account.getExpireDate() != null && account.getExpireDate().isBefore(LocalDate.now())) {
            throw new AccountStatusException(messageService.getMessage("account.is.expired"));
        }

    }
    @Transactional
    public void expireAccounts() {
        List<CurrentAccountEntity> expiredAccounts = currentAccountRepository.findExpiredAccounts(LocalDate.now(), CurrentAccountStatus.CANCELED, CurrentAccountStatus.EXPIRED);
        expiredAccounts.forEach(account -> {
            account.setCurrentAccountStatus(CurrentAccountStatus.EXPIRED);
            currentAccountRepository.save(account);
        });
    }

    private CurrentAccountResponse mapToResponse(CurrentAccountEntity entity) {
        CurrentAccountResponse response = new CurrentAccountResponse();
        response.setId(entity.getId());
        response.setAccountNumber(entity.getAccountNumber());
        response.setCurrentAccountStatus(entity.getCurrentAccountStatus());
        response.setCurrency(entity.getCurrency());
        response.setBalance(entity.getBalance());
        response.setActivationDate(entity.getActivationDate());
        response.setExpirationDate(entity.getExpireDate());
        response.setCustomerFullName(entity.getCustomer().getName() + " " + entity.getCustomer().getSurname());
        return response;
    }
}