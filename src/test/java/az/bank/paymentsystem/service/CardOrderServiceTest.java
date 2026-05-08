package az.bank.paymentsystem.service;
import az.bank.paymentsystem.config.PaymentLimitsConfig;
import az.bank.paymentsystem.enums.*;
import az.bank.paymentsystem.exception.*;
import az.bank.paymentsystem.model.CardResponse;
import az.bank.paymentsystem.model.entity.CardEntity;
import az.bank.paymentsystem.model.entity.CustomerEntity;
import az.bank.paymentsystem.model.CardOrderRequest;
import az.bank.paymentsystem.repository.CardOrderRepository;
import az.bank.paymentsystem.repository.CardRepository;
import az.bank.paymentsystem.repository.CustomerRepository;
import az.bank.paymentsystem.util.CustomerValidationUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardOrderServiceTest {

    @Mock
    private CardOrderRepository cardOrderRepository;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private CardService cardService;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private MessageService messageService;
    @Mock
    private PaymentLimitsConfig limitsConfig;
    @Mock
    private CustomerValidationUtils customerValidationUtils;

    @InjectMocks
    private CardOrderService cardOrderService;

    @Test
    void orderCard_Success() {

        CardOrderRequest request = new CardOrderRequest();
        request.setCustomerId(1L);
        request.setCardType(CardType.DEBIT);
        request.setCurrency(Currency.AZN);
        request.setCardName("My Card");

        CustomerEntity customer = new CustomerEntity();
        customer.setId(1L);
        customer.setName("Aysel");
        customer.setSurname("Aliyeva");
        customer.setCustomerStatus(CustomerStatus.ACTIVE);
        customer.setBirthDate(LocalDate.of(1995, 1, 1));

        CardEntity savedCard = new CardEntity();
        savedCard.setId(1L);
        savedCard.setCustomer(customer);
        savedCard.setCardStatus(CardStatus.NEW);
        savedCard.setBalance(BigDecimal.ZERO);

        CardResponse cardResponse = new CardResponse();
        cardResponse.setId(1L);


        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));
        doNothing().when(customerValidationUtils).validateStatus(any());
        doNothing().when(customerValidationUtils).validateAge(any());
        when(cardRepository.countByCustomerId(1L))
                .thenReturn(0L);
        when(limitsConfig.getCardLimit())
                .thenReturn(2);
        when(cardRepository.existsByPan(any()))
                .thenReturn(false);
        when(cardRepository.save(any()))
                .thenReturn(savedCard);
        when(cardService.getCardById(1L))
                .thenReturn(cardResponse);


        CardResponse response = cardOrderService.orderCard(request);

        assertNotNull(response);
        verify(cardRepository).save(any());
        verify(cardOrderRepository).save(any());
    }

    @Test
    void orderCard_CustomerNotFound_ThrowsException() {

        CardOrderRequest request = new CardOrderRequest();
        request.setCustomerId(999L);

        when(customerRepository.findById(999L))
                .thenReturn(Optional.empty());
        when(messageService.getMessage(any()))
                .thenReturn("Customer not found");


        assertThrows(CustomerNotFoundException.class,
                () -> cardOrderService.orderCard(request));
    }

    @Test
    void orderCard_CardLimitExceeded_ThrowsException() {

        CardOrderRequest request = new CardOrderRequest();
        request.setCustomerId(1L);
        request.setCardType(CardType.DEBIT);
        request.setCurrency(Currency.AZN);
        request.setCardName("Third Card");

        CustomerEntity customer = new CustomerEntity();
        customer.setId(1L);
        customer.setCustomerStatus(CustomerStatus.ACTIVE);
        customer.setBirthDate(LocalDate.of(1995, 1, 1));

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));
        doNothing().when(customerValidationUtils).validateStatus(any());
        doNothing().when(customerValidationUtils).validateAge(any());
        when(cardRepository.countByCustomerId(1L))
                .thenReturn(2L);
        when(limitsConfig.getCardLimit())
                .thenReturn(2);
        when(messageService.getMessage(any()))
                .thenReturn("Card limit exceeded");


        assertThrows(CardLimitException.class,
                () -> cardOrderService.orderCard(request));
    }
}