package az.bank.paymentsystem.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.springframework.web.bind.MethodArgumentNotValidException;
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleCustomerNotFoundException(CustomerNotFoundException e) {
        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(e.getMessage());
        exceptionResponse.setStatus(404);
        exceptionResponse.setTimestamp(java.time.LocalDateTime.now());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(CustomerAlreadyExistsException.class)
    public ResponseEntity<ExceptionResponse> handleCustomerAlreadyExistsException(CustomerAlreadyExistsException e) {
        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(e.getMessage());
        exceptionResponse.setStatus(409);
        exceptionResponse.setTimestamp(java.time.LocalDateTime.now());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.CONFLICT);
    }
    @ExceptionHandler(CardNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleCardNotFoundException(CardNotFoundException e) {
        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(e.getMessage());
        exceptionResponse.setStatus(404);
        exceptionResponse.setTimestamp(java.time.LocalDateTime.now());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(CardLimitException.class)
    public ResponseEntity<ExceptionResponse> handleCardLimitException(CardLimitException e) {
        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(e.getMessage());
        exceptionResponse.setStatus(409);
        exceptionResponse.setTimestamp(java.time.LocalDateTime.now());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CardStatusException.class)
    public ResponseEntity<ExceptionResponse> handleCardStatusException(CardStatusException e) {
        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(e.getMessage());
        exceptionResponse.setStatus(400);
        exceptionResponse.setTimestamp(java.time.LocalDateTime.now());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(AccountStatusException.class)
    public ResponseEntity<ExceptionResponse> handleAccountStatusException(AccountStatusException e) {
        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(e.getMessage());
        exceptionResponse.setStatus(400);
        exceptionResponse.setTimestamp(java.time.LocalDateTime.now());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleAccountNotFoundException(AccountNotFoundException e) {
        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(e.getMessage());
        exceptionResponse.setStatus(404);
        exceptionResponse.setTimestamp(java.time.LocalDateTime.now());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(AccountLimitException.class)
    public ResponseEntity<ExceptionResponse> handleAccountLimitException(AccountLimitException e) {
        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(e.getMessage());
        exceptionResponse.setStatus(409);
        exceptionResponse.setTimestamp(java.time.LocalDateTime.now());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.CONFLICT);
    }
    @ExceptionHandler(FinRequiredException.class)
    public ResponseEntity<ExceptionResponse> handleFinRequiredException(FinRequiredException e) {
        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(e.getMessage());
        exceptionResponse.setStatus(400);
        exceptionResponse.setTimestamp(java.time.LocalDateTime.now());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(VoenRequiredException.class)
    public ResponseEntity<ExceptionResponse> handleVoenRequiredException(VoenRequiredException e) {
        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(e.getMessage());
        exceptionResponse.setStatus(400);
        exceptionResponse.setTimestamp(java.time.LocalDateTime.now());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(CustomerStatusException.class)
    public ResponseEntity<ExceptionResponse> handleCustomerStatusException(CustomerStatusException e) {
        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(e.getMessage());
        exceptionResponse.setStatus(400);
        exceptionResponse.setTimestamp(java.time.LocalDateTime.now());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(PaymentMethodLimitException.class)
    public ResponseEntity<ExceptionResponse> handlePaymentMethodLimitException(PaymentMethodLimitException e) {
        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(e.getMessage());
        exceptionResponse.setStatus(409);
        exceptionResponse.setTimestamp(java.time.LocalDateTime.now());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.CONFLICT);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(message);
        exceptionResponse.setStatus(400);
        exceptionResponse.setTimestamp(LocalDateTime.now());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(PaymentAmountException.class)
    public ResponseEntity<ExceptionResponse> handlePaymentAmountException(PaymentAmountException e) {
        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(e.getMessage());
        exceptionResponse.setStatus(400);
        exceptionResponse.setTimestamp(java.time.LocalDateTime.now());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(InvalidTransferException.class)
    public ResponseEntity<ExceptionResponse> handleInvalidTransferException(InvalidTransferException e) {
        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(e.getMessage());
        exceptionResponse.setStatus(400);
        exceptionResponse.setTimestamp(java.time.LocalDateTime.now());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(AgeRestrictionException.class)
    public ResponseEntity<ExceptionResponse> handleAgeRestrictionException(AgeRestrictionException e) {
        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(e.getMessage());
        exceptionResponse.setStatus(400);
        exceptionResponse.setTimestamp(java.time.LocalDateTime.now());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(InsufficientAmountException.class)
    public ResponseEntity<ExceptionResponse> handleInsufficientAmountException(InsufficientAmountException e) {
        ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(e.getMessage());
        exceptionResponse.setStatus(400);
        exceptionResponse.setTimestamp(java.time.LocalDateTime.now());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<ExceptionResponse> handleInternalServerException(InternalServerException e) {
        ExceptionResponse response = new ExceptionResponse();
        response.setMessage(e.getMessage());
        response.setStatus(500);
        response.setTimestamp(LocalDateTime.now());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ExceptionResponse> handleInsufficientBalanceException(InsufficientBalanceException e) {
        ExceptionResponse response = new ExceptionResponse();
        response.setMessage(e.getMessage());
        response.setStatus(400);
        response.setTimestamp(LocalDateTime.now());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }


}
