package az.bank.paymentsystem.service;

import az.bank.paymentsystem.config.PaymentLimitsConfig;
import az.bank.paymentsystem.enums.CardStatus;
import az.bank.paymentsystem.enums.OrderStatus;
import az.bank.paymentsystem.exception.CardLimitException;
import az.bank.paymentsystem.exception.CustomerNotFoundException;
import az.bank.paymentsystem.model.CardOrderRequest;
import az.bank.paymentsystem.model.CardResponse;
import az.bank.paymentsystem.model.entity.CardEntity;
import az.bank.paymentsystem.model.entity.CardOrderEntity;
import az.bank.paymentsystem.model.entity.CustomerEntity;
import az.bank.paymentsystem.repository.CardOrderRepository;
import az.bank.paymentsystem.repository.CardRepository;
import az.bank.paymentsystem.repository.CustomerRepository;
import az.bank.paymentsystem.util.CustomerValidationUtils;
import az.bank.paymentsystem.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Random;

import static az.bank.paymentsystem.constant.Constant.BIN_PREFIX;

@Service
@RequiredArgsConstructor
public class CardOrderService {
    private final CardOrderRepository cardOrderRepository;
    private final CardRepository cardRepository;
    private final CardService cardService;
    private final CustomerRepository customerRepository;
    private final MessageService messageService;
    private final PaymentLimitsConfig limitsConfig;
    private final CustomerValidationUtils customerValidationUtils;
    private final SecurityUtils securityUtils;

    @Transactional
    public CardResponse orderCard(CardOrderRequest request) {
        securityUtils.checkOwnership(request.getCustomerId());
        CustomerEntity customer = customerRepository.findById(request.getCustomerId()).orElseThrow(() -> new CustomerNotFoundException(messageService.getMessage("customer.not.found")));
        customerValidationUtils.validateStatus(customer);
        customerValidationUtils.validateAge(customer);

        CardOrderEntity order = new CardOrderEntity();
        order.setCustomer(customer);
        order.setCardType(request.getCardType());
        order.setCurrency(request.getCurrency());
        order.setCardName(request.getCardName());

        long cardCount = cardRepository.countByCustomerId(request.getCustomerId());
        if (cardCount >= limitsConfig.getCardLimit()) {
            throw new CardLimitException(messageService.getMessage("customer.card.limit"));
        }

        order.setOrderStatus(OrderStatus.APPROVED);
        CardEntity card = createCard(order, customer);
        cardOrderRepository.save(order);
        return cardService.getCardById(card.getId());
    }
    private CardEntity createCard(CardOrderEntity order, CustomerEntity customer) {
        return cardRepository.save(buildCardEntity(order, customer));
    }

    private CardEntity buildCardEntity(CardOrderEntity order, CustomerEntity customer) {
        CardEntity card = new CardEntity();
        card.setCustomer(customer);
        card.setCardType(order.getCardType());
        card.setCardName(order.getCardName());
        card.setCurrency(order.getCurrency());
        card.setCardStatus(CardStatus.NEW);
        card.setBalance(BigDecimal.ZERO);
        card.setActivationDate(LocalDate.now());
        card.setExpireDate(LocalDate.now().plusYears(3));
        card.setPan(generateUniquePan());
        card.setCvv(generateUniqueCvv());
        card.setIsVisible(true);
        return card;
    }
    private String generateUniquePan() {
        Random random = new Random();
        String pan;
        boolean exists;
        do {
            StringBuilder sb = new StringBuilder(BIN_PREFIX);
            for (int i = 0; i < 12; i++) {
                sb.append(random.nextInt(10));
            }
            pan = sb.toString();
            exists = cardRepository.existsByPan(pan);
        } while (exists);
        return pan;
    }
    private String generateUniqueCvv() {
        Random random = new Random();
        return String.format("%03d", random.nextInt(1000));
    }

}