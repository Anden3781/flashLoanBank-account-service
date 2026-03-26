package com.flash_loan.bank.account_service.infrastructure.adapters.in.web.exception;

import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.flash_loan.bank.account_service.domain.exception.AccountNotFoundException;
import com.flash_loan.bank.account_service.domain.exception.BusinessRuleException;
import com.flash_loan.bank.account_service.domain.exception.CustomerNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleCustomerNotFound_ShouldReturn404() {
        var response = handler.handleCustomerNotFound(new CustomerNotFoundException("missing"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("message", "missing");
    }

    @Test
    void handleAccountNotFound_ShouldReturn404() {
        var response = handler.handleAccountNotFound(new AccountNotFoundException("missing"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void handleBusinessRule_ShouldReturn400() {
        var response = handler.handleBusinessRule(new BusinessRuleException("rule"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleIllegalArgument_ShouldReturn400() {
        var response = handler.handleIllegalArgument(new IllegalArgumentException("bad"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleJsonErrors_InvalidTypeId_ShouldReturnBadRequestWithAllowedValues() {
        InvalidTypeIdException cause = mock(InvalidTypeIdException.class);
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("bad", cause, (HttpInputMessage) null);

        var response = handler.handleJsonErrors(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("message", "Invalid accountType provided. Allowed values: SAVINGS, CHECKING, FIXED_TERM");
    }

    @Test
    void handleJsonErrors_OtherCause_ShouldReturnBadRequestMalformed() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("bad", new RuntimeException("x"), (HttpInputMessage) null);

        var response = handler.handleJsonErrors(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("message", "Malformed JSON request");
    }

    @Test
    void handleGeneralException_ShouldReturn500() {
        var response = handler.handleGeneralException(new RuntimeException("boom"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        Map<String, Object> body = response.getBody();
        assertThat(body).containsKey("timestamp");
        assertThat(body).containsEntry("status", 500);
    }
}
