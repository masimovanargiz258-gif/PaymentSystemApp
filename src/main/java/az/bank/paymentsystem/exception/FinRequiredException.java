package az.bank.paymentsystem.exception;

public class FinRequiredException extends RuntimeException {
    public FinRequiredException(String message) {
        super(message);
    }
}
