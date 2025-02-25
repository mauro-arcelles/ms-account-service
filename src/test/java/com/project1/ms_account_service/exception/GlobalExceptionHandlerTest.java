package com.project1.ms_account_service.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.project1.ms_account_service.model.AccountRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.MethodParameter;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

@SpringBootTest
class GlobalExceptionHandlerTest {

    @Autowired
    private GlobalExceptionHandler handler;

    @Autowired
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    void handleWebExchangeBindException() {
        MethodParameter methodParameter = new MethodParameter(
            this.getClass().getDeclaredMethods()[0], -1);
        BindingResult bindingResult = new BeanPropertyBindingResult(
            new Object(), "objectName");
        bindingResult.addError(new FieldError("object", "field", "message"));
        WebExchangeBindException ex = new WebExchangeBindException(
            methodParameter, bindingResult);

        StepVerifier.create(globalExceptionHandler.handleValidationErrors(ex))
            .expectNextMatches(response -> {
                Map<String, List<String>> errors = response.getBody();
                return response.getStatusCode() == HttpStatus.BAD_REQUEST &&
                    errors.containsKey("field") &&
                    errors.get("field").contains("message");
            })
            .verifyComplete();
    }

    @Test
    void handleInvalidAccountTypeException() {
        InvalidAccountTypeException ex = new InvalidAccountTypeException();

        StepVerifier.create(handler.handleInvalidAccountTypeException(ex))
            .expectNextMatches(response ->
                response != null &&
                    response.getStatusCode() == HttpStatus.BAD_REQUEST)
            .verifyComplete();
    }

    @Test
    void handleException() {
        Exception ex = new Exception();

        StepVerifier.create(handler.handleGenericError(ex))
            .expectNextMatches(response ->
                response != null &&
                    response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR)
            .verifyComplete();
    }

    @Test
    void handleBadRequestException() {
        BadRequestException ex = new BadRequestException("Bad request exception");

        StepVerifier.create(handler.handleBadRequestException(ex))
            .expectNextMatches(response ->
                response != null &&
                    response.getStatusCode() == HttpStatus.BAD_REQUEST)
            .verifyComplete();
    }

    @Test
    void handleServerWebInputException_WithInvalidFormatException() {
        MethodParameter methodParameter = new MethodParameter(
            this.getClass().getDeclaredMethods()[0], -1);
        InvalidFormatException invalidFormatException = new InvalidFormatException(
            null, "Invalid format", "value", Integer.class);
        JsonMappingException.Reference ref = new JsonMappingException.Reference(null, "testField");
        invalidFormatException.prependPath(ref);

        DecodingException decodingException = new DecodingException(
            "Decoding error", invalidFormatException);

        ServerWebInputException ex = new ServerWebInputException(
            AccountRequest.class.getName(), methodParameter, decodingException);

        StepVerifier.create(handler.handleServerWebInputException(ex))
            .expectNextMatches(response ->
                response != null &&
                    response.getStatusCode() == HttpStatus.BAD_REQUEST &&
                    response.getBody() != null)
            .verifyComplete();
    }

    @Test
    void handleServerWebInputException_WithInvalidTypeIdException() {
        MethodParameter methodParameter = new MethodParameter(
            this.getClass().getDeclaredMethods()[0], -1);

        InvalidTypeIdException invalidTypeIdException = new InvalidTypeIdException(
            null, "Invalid type", null, AccountRequest.class.getName());

        DecodingException decodingException = new DecodingException(
            "Decoding error", invalidTypeIdException);

        ServerWebInputException ex = new ServerWebInputException(
            AccountRequest.class.getName(), methodParameter, decodingException);

        StepVerifier.create(handler.handleServerWebInputException(ex))
            .expectNextMatches(response ->
                response != null &&
                    response.getStatusCode() == HttpStatus.BAD_REQUEST &&
                    response.getBody() != null)
            .verifyComplete();
    }

    @Test
    void handleNotFoundException_ShouldReturnNotFoundStatus() {
        NotFoundException ex = new NotFoundException("Not found");

        StepVerifier.create(handler.handleNotFoundException(ex))
            .expectNextMatches(response ->
                response != null &&
                    response.getStatusCode() == HttpStatus.NOT_FOUND &&
                    response.getBody() != null &&
                    response.getBody().getMessage().equals("Not found"))
            .verifyComplete();
    }

    @Test
    void handleInternalServerErrorException() {
        InternalServerErrorException ex = new InternalServerErrorException("Internal server error");

        StepVerifier.create(handler.handleInternalServerErrorException(ex))
            .expectNextMatches(response ->
                response != null &&
                    response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR &&
                    response.getBody() != null &&
                    response.getBody().getMessage() != null &&
                    response.getBody().getMessage().equals("Internal server error"))
            .verifyComplete();
    }
}
