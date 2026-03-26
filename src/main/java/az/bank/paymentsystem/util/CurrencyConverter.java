package az.bank.paymentsystem.util;

import az.bank.paymentsystem.enums.Currency;
import az.bank.paymentsystem.service.CurrencyRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
public class CurrencyConverter {

    private final CurrencyRateService currencyRateService;

    public BigDecimal convertToAZN(BigDecimal amount, Currency fromCurrency) {
        BigDecimal rate = currencyRateService.getRateToAZN(fromCurrency.name());
        return amount.multiply(rate);
    }

    public BigDecimal convertToUSD(BigDecimal amount, Currency fromCurrency) {
        BigDecimal amountInAZN = convertToAZN(amount, fromCurrency);
        BigDecimal usdRate = currencyRateService.getRateToAZN("USD");
        return amountInAZN.divide(usdRate, 2, RoundingMode.HALF_UP);
    }

    public BigDecimal convert(BigDecimal amount, Currency from, Currency to) {
        BigDecimal amountInAZN = convertToAZN(amount, from);
        BigDecimal toRate = currencyRateService.getRateToAZN(to.name());
        return amountInAZN.divide(toRate, 2, RoundingMode.HALF_UP);
    }
}