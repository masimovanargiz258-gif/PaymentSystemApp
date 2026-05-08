package az.bank.paymentsystem.service;

import az.bank.paymentsystem.enums.CardStatus;
import az.bank.paymentsystem.enums.Currency;
import az.bank.paymentsystem.exception.CardNotFoundException;
import az.bank.paymentsystem.exception.CardStatusException;
import az.bank.paymentsystem.exception.InsufficientAmountException;
import az.bank.paymentsystem.model.CardDepositRequest;
import az.bank.paymentsystem.model.CardResponse;
import az.bank.paymentsystem.model.entity.CardEntity;
import az.bank.paymentsystem.model.entity.CustomerEntity;
import az.bank.paymentsystem.repository.CardRepository;
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
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private MessageService messageService;

    @InjectMocks
    private CardService cardService;

    @Test
    void deposit_NewCard_BecomesActive() {
        CustomerEntity customer = new CustomerEntity();
        customer.setName("Aysel");
        customer.setSurname("Aliyeva");

        CardEntity card = new CardEntity();
        card.setId(1L);
        card.setCardStatus(CardStatus.NEW);
        card.setBalance(BigDecimal.ZERO);
        card.setCurrency(Currency.AZN);
        card.setExpireDate(LocalDate.now().plusYears(3));
        card.setCustomer(customer);

        CardDepositRequest request = new CardDepositRequest();
        request.setAmount(BigDecimal.valueOf(100));

        when(cardRepository.findWithCustomerById(1L))
                .thenReturn(Optional.of(card));
        when(cardRepository.save(any()))
                .thenReturn(card);

        CardResponse response = cardService.deposit(1L, request);

        assertNotNull(response);
        assertEquals(CardStatus.ACTIVE, card.getCardStatus());
        assertEquals(BigDecimal.valueOf(100), card.getBalance());
        verify(cardRepository).save(any());
    }

    @Test
    void deposit_NegativeAmount_ThrowsException() {
        CustomerEntity customer = new CustomerEntity();
        customer.setName("Aysel");
        customer.setSurname("Aliyeva");

        CardEntity card = new CardEntity();
        card.setId(1L);
        card.setCardStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.valueOf(100));
        card.setExpireDate(LocalDate.now().plusYears(3));
        card.setCustomer(customer);

        CardDepositRequest request = new CardDepositRequest();
        request.setAmount(BigDecimal.valueOf(-10));

        when(cardRepository.findWithCustomerById(1L))
                .thenReturn(Optional.of(card));
        when(messageService.getMessage(any()))
                .thenReturn("Amount must be greater than 0");

        assertThrows(InsufficientAmountException.class,
                () -> cardService.deposit(1L, request));
    }

    @Test
    void cancelCard_Success() {
        CustomerEntity customer = new CustomerEntity();
        customer.setName("Aysel");
        customer.setSurname("Aliyeva");

        CardEntity card = new CardEntity();
        card.setId(1L);
        card.setCardStatus(CardStatus.ACTIVE);
        card.setExpireDate(LocalDate.now().plusYears(3));
        card.setCustomer(customer);

        when(cardRepository.findWithCustomerById(1L))
                .thenReturn(Optional.of(card));
        when(cardRepository.save(any()))
                .thenReturn(card);

        cardService.cancelCard(1L);

        assertEquals(CardStatus.CANCELED, card.getCardStatus());
        verify(cardRepository).save(any());
    }

    @Test
    void cancelCard_AlreadyCanceled_ThrowsException() {
        CustomerEntity customer = new CustomerEntity();
        customer.setName("Aysel");
        customer.setSurname("Aliyeva");

        CardEntity card = new CardEntity();
        card.setId(1L);
        card.setCardStatus(CardStatus.CANCELED);
        card.setCustomer(customer);

        when(cardRepository.findWithCustomerById(1L))
                .thenReturn(Optional.of(card));
        when(messageService.getMessage(any()))
                .thenReturn("Card is already canceled");

        assertThrows(CardStatusException.class,
                () -> cardService.cancelCard(1L));
    }

    @Test
    void unblockCard_Success() {
        CustomerEntity customer = new CustomerEntity();
        customer.setName("Aysel");
        customer.setSurname("Aliyeva");

        CardEntity card = new CardEntity();
        card.setId(1L);
        card.setCardStatus(CardStatus.BLOCKED);
        card.setCustomer(customer);

        when(cardRepository.findWithCustomerById(1L))
                .thenReturn(Optional.of(card));
        when(cardRepository.save(any()))
                .thenReturn(card);

        cardService.unblockCard(1L);

        assertEquals(CardStatus.ACTIVE, card.getCardStatus());
        verify(cardRepository).save(any());
    }

    @Test
    void getCardById_NotFound_ThrowsException() {
        when(cardRepository.findWithCustomerById(999L))
                .thenReturn(Optional.empty());
        when(messageService.getMessage(any()))
                .thenReturn("Card not found");

        assertThrows(CardNotFoundException.class,
                () -> cardService.getCardById(999L));
    }
}