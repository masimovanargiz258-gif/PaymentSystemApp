package az.bank.paymentsystem.service;
import az.bank.paymentsystem.model.*;
import az.bank.paymentsystem.model.entity.CardEntity;
import az.bank.paymentsystem.enums.CardStatus;
import az.bank.paymentsystem.exception.*;
import az.bank.paymentsystem.repository.CardRepository;
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
public class CardService {
    private final CardRepository cardRepository;
    private final CustomerRepository customerRepository;
    private final MessageService messageService;

    public CardResponse getCardById(Long id) {
        return mapToResponse(findCardById(id));
    }

    public List<CardResponse> getCardByCustomerId(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException(messageService.getMessage("customer.not.found"));
        }
        return cardRepository.findByCustomerId(customerId).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public CardResponse updateCard(Long id, CardUpdateRequest request) {
        CardEntity card = findCardById(id);
        if (request.getCardName() != null) {
            card.setCardName(request.getCardName());
        }
        if (request.getIsVisible() != null) {
            card.setIsVisible(request.getIsVisible());
        }
        return mapToResponse(cardRepository.save(card));
    }

    @Transactional
    public CardResponse cancelCard(Long id) {
        CardEntity card = findCardById(id);
        validateCardStatus(card);
        card.setCardStatus(CardStatus.CANCELED);
        return mapToResponse(cardRepository.save(card));
    }

    @Transactional
    public CardResponse deposit(Long id, CardDepositRequest request) {
        CardEntity card = findCardById(id);
        validateCardStatus(card);
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InsufficientAmountException(messageService.getMessage("insufficient.amount"));
        }
        if (card.getCardStatus() == CardStatus.NEW) {
            card.setCardStatus(CardStatus.ACTIVE);
        }
        card.setBalance(card.getBalance().add(request.getAmount()));
        return mapToResponse(cardRepository.save(card));
    }
    @Transactional
    public CardResponse unblockCard(Long id) {
        CardEntity card = findCardById(id);
        if (card.getCardStatus() != CardStatus.BLOCKED) {
            throw new CardStatusException(messageService.getMessage("card.is.not.blocked"));
        }
        card.setCardStatus(CardStatus.ACTIVE);
        return mapToResponse(cardRepository.save(card));
    }

    private CardEntity findCardById(Long id) {
        return cardRepository.findWithCustomerById(id).orElseThrow(() -> new CardNotFoundException(messageService.getMessage("card.not.found")));
    }

    private void validateCardStatus(CardEntity card) {
        if (card.getCardStatus() == CardStatus.CANCELED) {
            throw new CardStatusException(messageService.getMessage("card.is.already.canceled"));
        }
        if (card.getCardStatus() == CardStatus.EXPIRE) {
            throw new CardStatusException(messageService.getMessage("card.is.already.expired"));
        }
        if (card.getCardStatus() == CardStatus.BLOCKED) {
            throw new CardStatusException(messageService.getMessage("card.is.blocked"));
        }
        if (card.getExpireDate() != null && card.getExpireDate().isBefore(LocalDate.now())) {
            throw new CardStatusException(messageService.getMessage("card.is.expired"));
        }
    }
    @Transactional
    public void expireCards() {
        List<CardEntity> expiredCards = cardRepository.findExpiredCards(LocalDate.now(),CardStatus.CANCELED, CardStatus.EXPIRE);
        expiredCards.forEach(card -> {
            card.setCardStatus(CardStatus.EXPIRE);
            cardRepository.save(card);
        });
    }

    private CardResponse mapToResponse(CardEntity entity) {
        CardResponse response = new CardResponse();
        response.setId(entity.getId());
        response.setPan(entity.getPan());
        response.setCardName(entity.getCardName());
        response.setCardStatus(entity.getCardStatus());
        response.setCardType(entity.getCardType());
        response.setCurrency(entity.getCurrency());
        response.setBalance(entity.getBalance());
        response.setExpireDate(entity.getExpireDate());
        response.setCardHolderName(entity.getCustomer().getName() + " " + entity.getCustomer().getSurname());
        return response;
    }
}