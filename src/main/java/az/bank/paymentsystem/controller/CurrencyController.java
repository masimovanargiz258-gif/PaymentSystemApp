package az.bank.paymentsystem.controller;

import az.bank.paymentsystem.model.CbarRatesResponse;
import az.bank.paymentsystem.service.CurrencyRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/currency")
@RequiredArgsConstructor
@Tag(name = "Currency", description = "Currency API")
public class CurrencyController {
    private final CurrencyRateService currencyRateService;

    @GetMapping("/cbar/{date}")
    @Operation(summary = "Get currency rates by date")
    public CbarRatesResponse getCbarRates(@PathVariable String date) {
        return currencyRateService.getCbarRatesByDate(date);
    }

    @GetMapping("/cbar/{date}/{code}")
    @Operation(summary = "Get currency rate by date and code")
    public CbarRatesResponse.Valute getCbarRateByCode(@PathVariable String date, @PathVariable String code) {
        return currencyRateService.getCurrencyRate(date, code);
    }
}