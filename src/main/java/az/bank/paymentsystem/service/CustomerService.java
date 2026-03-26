package az.bank.paymentsystem.service;

import az.bank.paymentsystem.enums.CardStatus;
import az.bank.paymentsystem.enums.CurrentAccountStatus;
import az.bank.paymentsystem.model.entity.CardEntity;
import az.bank.paymentsystem.model.entity.CurrentAccountEntity;
import az.bank.paymentsystem.model.entity.CustomerEntity;
import az.bank.paymentsystem.enums.CustomerStatus;
import az.bank.paymentsystem.enums.CustomerType;
import az.bank.paymentsystem.exception.*;
import az.bank.paymentsystem.model.CustomerRequest;
import az.bank.paymentsystem.model.CustomerResponse;
import az.bank.paymentsystem.model.CustomerUpdateRequest;
import az.bank.paymentsystem.repository.CardRepository;
import az.bank.paymentsystem.repository.CurrentAccountRepository;
import az.bank.paymentsystem.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final CardRepository cardRepository;
    private final CurrentAccountRepository currentAccountRepository;
    private final MessageService messageService;


    public CustomerResponse getCustomerById(Long id) {
        return mapToResponse(findCustomerById(id));
    }


    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAllByIsVisibleTrue().stream().map(this::mapToResponse).collect(Collectors.toList());
    }


    public CustomerResponse createCustomer(CustomerRequest request) {
        if (request.getCustomerType() == CustomerType.INDIVIDUAL) {
            if (request.getFin() == null || request.getFin().isEmpty()) {
                throw new FinRequiredException(messageService.getMessage("fin.required"));
            }
            if (customerRepository.existsByFin(request.getFin())) {
                throw new CustomerAlreadyExistsException(messageService.getMessage("customer.already.exists"));
            }
        }
        if (request.getCustomerType() == CustomerType.COMPANY) {
            if (request.getVoen() == null || request.getVoen().isEmpty()) {
                throw new VoenRequiredException(messageService.getMessage("voen.required"));
            }
            if (customerRepository.existsByVoen(request.getVoen())) {
                throw new CustomerAlreadyExistsException(messageService.getMessage("customer.already.exists"));
            }
        }
        return mapToResponse(customerRepository.save(buildCustomerEntity(request)));
    }


    public CustomerResponse updateFullCustomer(Long id, CustomerRequest request) {
        CustomerEntity customer = findCustomerById(id);
        validateCustomerStatus(customer);
        customer.setName(request.getName());
        customer.setSurname(request.getSurname());
        customer.setFullName(request.getName() + " " + request.getSurname());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setEmail(request.getEmail());
        return mapToResponse(customerRepository.save(customer));
    }


    public CustomerResponse updateHalfCustomer(Long id, CustomerUpdateRequest request) {
        CustomerEntity customer = findCustomerById(id);
        validateCustomerStatus(customer);
        if (request.getName() != null) {
            customer.setName(request.getName());
        }
        if (request.getSurname() != null) {
            customer.setSurname(request.getSurname());
        }
        if (request.getName() != null || request.getSurname() != null) {
            customer.setFullName(customer.getName() + " " + customer.getSurname());
        }
        if (request.getPhoneNumber() != null) {
            customer.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getEmail() != null) {
            customer.setEmail(request.getEmail());
        }
        return mapToResponse(customerRepository.save(customer));
    }

    @Transactional
    public void deleteCustomer(Long id) {
        CustomerEntity customer = findCustomerById(id);
        if (customer.getCustomerStatus() == CustomerStatus.DELETED) {
            throw new CustomerStatusException(messageService.getMessage("customer.is.deleted"));
        }
        if (customer.getCustomerStatus() == CustomerStatus.BLOCKED) {
            throw new CustomerStatusException(messageService.getMessage("customer.is.blocked"));
        }
        customer.setCustomerStatus(CustomerStatus.DELETED);
        customer.setIsVisible(false);
        customerRepository.save(customer);
    }


    public void blockCustomer(Long id) {
        CustomerEntity customer = findCustomerById(id);
        if (customer.getCustomerStatus() == CustomerStatus.BLOCKED) {
            throw new CustomerStatusException(messageService.getMessage("customer.is.blocked"));
        }
        if (customer.getCustomerStatus() == CustomerStatus.DELETED) {
            throw new CustomerStatusException(messageService.getMessage("customer.is.deleted"));
        }
        customer.setCustomerStatus(CustomerStatus.BLOCKED);
        customerRepository.save(customer);
    }


    public void unblockCustomer(Long id) {
        CustomerEntity customer = customerRepository.findWithCardAndAccountsById(id).orElseThrow(() -> new CustomerNotFoundException(messageService.getMessage("customer.not.found")));
        if (customer.getCustomerStatus() != CustomerStatus.BLOCKED &&
                customer.getCustomerStatus() != CustomerStatus.SUSPICIOUS) {
            throw new CustomerStatusException(messageService.getMessage("customer.is.not.blocked"));
        }
        customer.setCustomerStatus(CustomerStatus.ACTIVE);
        List<CardEntity> cardsToUnblock = customer.getCards().stream()
                .filter(c -> c.getCardStatus() == CardStatus.BLOCKED)
                .peek(c -> c.setCardStatus(CardStatus.ACTIVE))
                .toList();
        cardRepository.saveAll(cardsToUnblock);
        List<CurrentAccountEntity> accountsToUnblock = customer.getCurrentAccounts().stream()
                .filter(a -> a.getCurrentAccountStatus() == CurrentAccountStatus.BLOCKED)
                .peek(a -> a.setCurrentAccountStatus(CurrentAccountStatus.ACTIVE))
                .toList();
        currentAccountRepository.saveAll(accountsToUnblock);
        customerRepository.save(customer);
    }


    private CustomerEntity findCustomerById(Long id) {
        return customerRepository.findById(id).orElseThrow(() -> new CustomerNotFoundException(messageService.getMessage("customer.not.found")));
    }

    private void validateCustomerStatus(CustomerEntity customer) {
        if (customer.getCustomerStatus() == CustomerStatus.DELETED) {
            throw new CustomerStatusException(messageService.getMessage("customer.is.deleted"));
        }
        if (customer.getCustomerStatus() == CustomerStatus.SUSPICIOUS) {
            throw new CustomerStatusException(messageService.getMessage("customer.is.suspicious"));
        }
        if (customer.getCustomerStatus() == CustomerStatus.BLOCKED) {
            throw new CustomerStatusException(messageService.getMessage("customer.is.blocked"));
        }
    }

    private CustomerResponse mapToResponse(CustomerEntity entity) {
        CustomerResponse response = new CustomerResponse();
        response.setId(entity.getId());
        response.setFullName(entity.getName() + " " + entity.getSurname());
        response.setFin(entity.getFin());
        response.setVoen(entity.getVoen());
        response.setCustomerType(entity.getCustomerType());
        response.setCustomerStatus(entity.getCustomerStatus());
        response.setPhoneNumber(entity.getPhoneNumber());
        response.setBirthDate(entity.getBirthDate());
        response.setEmail(entity.getEmail());
        return response;
    }

    private CustomerEntity buildCustomerEntity(CustomerRequest request) {
        CustomerEntity entity = new CustomerEntity();
        entity.setName(request.getName());
        entity.setSurname(request.getSurname());
        entity.setFullName(request.getName() + " " + request.getSurname());
        entity.setEmail(request.getEmail());
        entity.setBirthDate(request.getBirthDate());
        entity.setPhoneNumber(request.getPhoneNumber());
        entity.setFin(request.getFin());
        entity.setVoen(request.getVoen());
        entity.setIsVisible(true);
        entity.setCustomerType(request.getCustomerType());
        entity.setCustomerStatus(CustomerStatus.ACTIVE);
        entity.setMonthlyTurnover(BigDecimal.ZERO);
        return entity;
    }
}