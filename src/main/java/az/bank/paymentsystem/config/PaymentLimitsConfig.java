package az.bank.paymentsystem.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConfigurationProperties(prefix = "payment.limits")
@Getter
@Setter
public class PaymentLimitsConfig {
    private BigDecimal cardMinBalance;
    private BigDecimal accountMinBalance;
    private BigDecimal monthlyLimit;
    private int cardLimit;
    private int accountLimit;
    private BigDecimal externalCommissionRate;
}
