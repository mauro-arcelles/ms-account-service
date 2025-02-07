package com.project1.ms_account_service.exception;

import com.project1.ms_account_service.model.ResponseBase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<Map<String, List<String>>>> handleValidationErrors(WebExchangeBindException ex) {
        Map<String, List<String>> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
                ));
        return Mono.just(ResponseEntity.badRequest().body(errors));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<String>> handleGenericError(Exception ex) {
        ex.printStackTrace();
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Internal Server Error"));
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public Mono<ResponseEntity<ResponseBase>> handleCustomerNotFoundException(Exception ex) {
        ResponseBase responseBase = new ResponseBase();
        responseBase.setMessage(ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(responseBase));
    }

    @ExceptionHandler(InvalidAccountTypeException.class)
    public Mono<ResponseEntity<ResponseBase>> handleInvalidAccountTypeException(Exception ex) {
        ResponseBase responseBase = new ResponseBase();
        responseBase.setMessage(ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(responseBase));
    }

    @ExceptionHandler(AccountCreationException.class)
    public Mono<ResponseEntity<ResponseBase>> handleAccountCreationException(Exception ex) {
        ResponseBase responseBase = new ResponseBase();
        responseBase.setMessage(ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(responseBase));
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public Mono<ResponseEntity<ResponseBase>> handleAccountNotFoundException(Exception ex) {
        ResponseBase responseBase = new ResponseBase();
        responseBase.setMessage(ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(responseBase));
    }
}