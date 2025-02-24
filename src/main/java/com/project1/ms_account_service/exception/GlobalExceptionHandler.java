package com.project1.ms_account_service.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.project1.ms_account_service.model.AccountRequest;
import com.project1.ms_account_service.model.ResponseBase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<Map<String, List<String>>>> handleValidationErrors(WebExchangeBindException ex) {
        log.error("Error", ex);
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
        log.error("Error", ex);
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ex.getMessage()));
    }

    @ExceptionHandler(InvalidAccountTypeException.class)
    public Mono<ResponseEntity<ResponseBase>> handleInvalidAccountTypeException(Exception ex) {
        log.error("Error", ex);
        ResponseBase responseBase = new ResponseBase();
        responseBase.setMessage(ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(responseBase));
    }

    @ExceptionHandler(BadRequestException.class)
    public Mono<ResponseEntity<ResponseBase>> handleBadRequestException(Exception ex) {
        log.error("Error", ex);
        ResponseBase responseBase = new ResponseBase();
        responseBase.setMessage(ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(responseBase));
    }

    @ExceptionHandler(NotFoundException.class)
    public Mono<ResponseEntity<ResponseBase>> handleNotFoundException(Exception ex) {
        log.error("Error", ex);
        ResponseBase responseBase = new ResponseBase();
        responseBase.setMessage(ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(responseBase));
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public Mono<ResponseEntity<ResponseBase>> handleInternalServerErrorException(Exception ex) {
        log.error("Error", ex);
        ResponseBase responseBase = new ResponseBase();
        responseBase.setMessage(ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(responseBase));
    }

    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ResponseEntity<Map<String, List<String>>>> handleServerWebInputException(ServerWebInputException ex) {
        log.error("Error", ex);
        Map<String, List<String>> errors = new HashMap<>();

        if (ex.getCause() instanceof DecodingException) {
            DecodingException decodingException = (DecodingException) ex.getCause();
            if (decodingException.getCause() instanceof InvalidFormatException) {
                InvalidFormatException invalidFormatException = (InvalidFormatException) decodingException.getCause();
                String fieldName = invalidFormatException.getPath().get(0).getFieldName();
                String targetType = invalidFormatException.getTargetType().getSimpleName();
                errors.put(fieldName, List.of("Must be a valid " + targetType.toLowerCase()));
            }
            if (decodingException.getCause() instanceof InvalidTypeIdException) {
                if (ex.getMessage().contains(AccountRequest.class.getName())) {
                    errors.put("accountType", List.of("Invalid account type. Should be one of: CHECKING|SAVINGS|FIXED_TERM"));
                } else {
                    errors.put("accountType", List.of("Invalid type provided: " + ex.getMessage()));
                }
            }
        }

        return Mono.just(ResponseEntity.badRequest().body(errors));
    }
}
