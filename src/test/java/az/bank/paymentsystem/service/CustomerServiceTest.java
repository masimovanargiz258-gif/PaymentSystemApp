package az.bank.paymentsystem.service;
import az.bank.paymentsystem.enums.CustomerStatus;
import az.bank.paymentsystem.enums.CustomerType;
import az.bank.paymentsystem.exception.CustomerAlreadyExistsException;
import az.bank.paymentsystem.exception.FinRequiredException;
import az.bank.paymentsystem.exception.CustomerNotFoundException;
import az.bank.paymentsystem.exception.CustomerStatusException;
import az.bank.paymentsystem.model.CustomerRequest;
import az.bank.paymentsystem.model.CustomerResponse;
import az.bank.paymentsystem.model.entity.CustomerEntity;
import az.bank.paymentsystem.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {
    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private MessageService messageService;

    @InjectMocks
    private CustomerService customerService;
    @Test
    void createCustomer_Individual_Success() {
        CustomerRequest request = new CustomerRequest();
        request.setName("Aysel");
        request.setSurname("Aliyeva");
        request.setFin("1234567");
        request.setBirthDate(java.time.LocalDate.of(1995, 5, 15));
        request.setPhoneNumber("+994501234567");
        request.setEmail("aysel@gmail.com");
        request.setCustomerType(CustomerType.INDIVIDUAL);

        CustomerEntity savedEntity = new CustomerEntity();
        savedEntity.setId(1L);
        savedEntity.setName("Aysel");
        savedEntity.setSurname("Aliyeva");
        savedEntity.setCustomerStatus(CustomerStatus.ACTIVE);
        savedEntity.setCustomerType(CustomerType.INDIVIDUAL);

        when(customerRepository.existsByFin("1234567")).thenReturn(false);
        when(customerRepository.save(any())).thenReturn(savedEntity);

        CustomerResponse response = customerService.createCustomer(request);

        assertNotNull(response);
        verify(customerRepository).save(any());
    }
    @Test
    void createCustomer_WithoutFin_ThrowsException() {
        CustomerRequest request = new CustomerRequest();
        request.setName("Aysel");
        request.setCustomerType(CustomerType.INDIVIDUAL);

        when(messageService.getMessage(any())).thenReturn("FIN is required");
        assertThrows(FinRequiredException.class,
                () -> customerService.createCustomer(request));
    }
    @Test
    void createCustomer_DuplicateFin_ThrowsException() {
        CustomerRequest request = new CustomerRequest();
        request.setName("Aysel");
        request.setFin("1234567");
        request.setCustomerType(CustomerType.INDIVIDUAL);

        when(customerRepository.existsByFin("1234567"))
                .thenReturn(true);
        when(messageService.getMessage(any()))
                .thenReturn("Customer already exists");

        assertThrows(CustomerAlreadyExistsException.class,
                () -> customerService.createCustomer(request));
    }
    @Test
    void blockCustomer_Success() {
        CustomerEntity customer = new CustomerEntity();
        customer.setId(1L);
        customer.setCustomerStatus(CustomerStatus.ACTIVE);

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));
        when(customerRepository.save(any()))
                .thenReturn(customer);

        customerService.blockCustomer(1L);

        verify(customerRepository).save(any());
        assertEquals(CustomerStatus.BLOCKED, customer.getCustomerStatus());
    }
    @Test
    void blockCustomer_AlreadyBlocked_ThrowsException() {
        CustomerEntity customer = new CustomerEntity();
        customer.setId(1L);
        customer.setCustomerStatus(CustomerStatus.BLOCKED);

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));
        when(messageService.getMessage(any()))
                .thenReturn("Customer is blocked");

        assertThrows(CustomerStatusException.class,
                () -> customerService.blockCustomer(1L));
    }
    @Test
    void getCustomerById_NotFound_ThrowsException() {
        when(customerRepository.findById(999L))
                .thenReturn(Optional.empty());
        when(messageService.getMessage(any()))
                .thenReturn("Customer not found");

        assertThrows(CustomerNotFoundException.class,
                () -> customerService.getCustomerById(999L));
    }
}