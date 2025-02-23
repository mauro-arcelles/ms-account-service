package com.project1.ms_account_service.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.project1.ms_account_service.model.AccountRequest;
import com.project1.ms_account_service.model.ResponseBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.MethodParameter;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GlobalExceptionHandlerTest {

    @Autowired
    private GlobalExceptionHandler handler;

    @Test
    void handleWebExchangeBindException() {
        MethodParameter methodParameter = new MethodParameter(
            this.getClass().getDeclaredMethods()[0], -1);
        BindingResult bindingResult = new BeanPropertyBindingResult(
            new Object(), "objectName");
        bindingResult.addError(new FieldError("object", "field", "message"));
        WebExchangeBindException ex = new WebExchangeBindException(
            methodParameter, bindingResult);

        ResponseEntity<Map<String, List<String>>> response = handler.handleValidationErrors(ex)
            .block();

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("message", response.getBody().get("field").get(0));
    }

    @Test
    void handleInvalidAccountTypeException() {
        InvalidAccountTypeException ex = new InvalidAccountTypeException();

        ResponseEntity<ResponseBase> response = handler.handleInvalidAccountTypeException(ex)
            .block();

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleException() {
        Exception ex = new Exception();

        ResponseEntity<String> response = handler.handleGenericError(ex)
            .block();

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void handleBadRequestException() {
        BadRequestException ex = new BadRequestException("Bad request exception");

        ResponseEntity<ResponseBase> response = handler.handleBadRequestException(ex)
            .block();

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
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

        ResponseEntity<Map<String, List<String>>> response =
            handler.handleServerWebInputException(ex).block();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
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

        ResponseEntity<Map<String, List<String>>> response =
            handler.handleServerWebInputException(ex).block();

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void handleNotFoundException_ShouldReturnNotFoundStatus() {
        NotFoundException ex = new NotFoundException("Not found");

        ResponseEntity<ResponseBase> response = handler.handleNotFoundException(ex)
            .block();

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Not found", response.getBody().getMessage());
    }
}
