package az.bank.paymentsystem.service;

import az.bank.paymentsystem.client.CbarCurrencyClient;
import az.bank.paymentsystem.model.CbarRatesResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrencyRateService {

    private final CbarCurrencyClient cbarCurrencyClient;

    public CbarRatesResponse getCbarRatesByDate(String date) {
        return cbarCurrencyClient.getRatesByDate(date);
    }

    public CbarRatesResponse.Valute getCurrencyRate(String date, String code) {
        CbarRatesResponse response = cbarCurrencyClient.getRatesByDate(date);
        String normalizedCode = code.toUpperCase(Locale.ROOT);

        Optional<CbarRatesResponse.Valute> matchedValute = Optional
                .ofNullable(response.getValTypes())
                .stream()
                .flatMap(List::stream)
                .filter(valType -> "Xarici valyutalar"
                        .equalsIgnoreCase(valType.getType()))
                .flatMap(valType -> Optional
                        .ofNullable(valType.getValutes())
                        .stream()
                        .flatMap(List::stream))
                .filter(valute -> normalizedCode
                        .equalsIgnoreCase(valute.getCode()))
                .findFirst();

        if (matchedValute.isEmpty()) {
            throw new IllegalArgumentException("Currency code not found: " + normalizedCode);
        }
        return matchedValute.get();
    }

    public BigDecimal getRateToAZN(String currencyCode) {
        if ("AZN".equalsIgnoreCase(currencyCode)) {
            return BigDecimal.ONE;
        }
        try {
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            return getCurrencyRate(today, currencyCode).getValue();
        } catch (Exception e) {
            log.error("Failed to get rate for {}: {}", currencyCode, e.getMessage());
            return getDefaultRate(currencyCode);
        }
    }

    private BigDecimal getDefaultRate(String currencyCode) {
        return switch (currencyCode.toUpperCase()) {
            case "USD" -> new BigDecimal("1.70");
            case "EUR" -> new BigDecimal("1.85");
            default -> BigDecimal.ONE;
        };
    }
}
