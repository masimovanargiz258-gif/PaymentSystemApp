package az.bank.paymentsystem.exception;

public class AccountLimitException extends RuntimeException {
    public AccountLimitException(String message) {
        super(message);
    }
}



