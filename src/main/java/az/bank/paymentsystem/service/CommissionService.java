package az.bank.paymentsystem.service;

import az.bank.paymentsystem.config.PaymentLimitsConfig;
import az.bank.paymentsystem.constant.Constant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class CommissionService {
    private final PaymentLimitsConfig limitsConfig;

    public BigDecimal calculateCommission(BigDecimal amount, String toAccountNumber) {
        if (isExternal(toAccountNumber)) {
            return amount.multiply(limitsConfig.getExternalCommissionRate())
                    .setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
    private boolean isExternal(String toAccountNumber) {
        return !toAccountNumber.startsWith(Constant.BIN_PREFIX) &&
                !toAccountNumber.startsWith(Constant.ACCOUNT_PREFIX);
    }
}

