package az.bank.paymentsystem.exception;

public class PaymentMethodLimitException extends RuntimeException {
    public PaymentMethodLimitException(String message) {
        super(message);
    }
}
