package com.flash_loan.bank.account_service.infrastructure.adapters.out.web;

import com.flash_loan.bank.account_service.domain.exception.CustomerNotFoundException;
import com.flash_loan.bank.account_service.domain.model.enums.CustomerType;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WebClientAdaptersTest {

    @Test
    void customerWebClientAdapter_Success_ShouldReturnCustomerType() {
        ExchangeFunction exchangeFunction = request -> jsonResponse(HttpStatus.OK, "{\"customerType\":\"PERSONAL\"}");
        WebClient client = WebClient.builder().exchangeFunction(exchangeFunction).baseUrl("http://customer").build();

        CustomerWebClientAdapter adapter = new CustomerWebClientAdapter(client);

        CustomerType type = adapter.getCustomerType("cust-1").blockingGet();
        assertThat(type).isEqualTo(CustomerType.PERSONAL);
    }

    @Test
    void customerWebClientAdapter_NotFound_ShouldThrowCustomerNotFound() {
        ExchangeFunction exchangeFunction = request -> Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build());
        WebClient client = WebClient.builder().exchangeFunction(exchangeFunction).baseUrl("http://customer").build();

        CustomerWebClientAdapter adapter = new CustomerWebClientAdapter(client);

        assertThatThrownBy(() -> adapter.getCustomerType("cust-1").blockingGet())
                .isInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    void customerWebClientAdapter_ServerError_ShouldThrowRuntimeException() {
        ExchangeFunction exchangeFunction = request -> Mono.just(ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR).build());
        WebClient client = WebClient.builder().exchangeFunction(exchangeFunction).baseUrl("http://customer").build();

        CustomerWebClientAdapter adapter = new CustomerWebClientAdapter(client);

        assertThatThrownBy(() -> adapter.getCustomerType("cust-1").blockingGet())
                .hasMessageContaining("Error communicating with Customer Service API");
    }

    @Test
    void customerWebClientAdapter_InvalidCustomerType_ShouldThrowRuntimeException() {
        ExchangeFunction exchangeFunction = request -> jsonResponse(HttpStatus.OK, "{\"customerType\":\"UNKNOWN\"}");
        WebClient client = WebClient.builder().exchangeFunction(exchangeFunction).baseUrl("http://customer").build();

        CustomerWebClientAdapter adapter = new CustomerWebClientAdapter(client);

        assertThatThrownBy(() -> adapter.getCustomerType("cust-1").blockingGet())
                .hasMessageContaining("Invalid customer type format received");
    }

    @Test
    void cardValidationAdapter_WhenCreditCardExists_ShouldReturnTrue() {
        AtomicReference<String> requestedPath = new AtomicReference<>();
        ExchangeFunction exchangeFunction = request -> {
            requestedPath.set(request.url().getPath());
            return jsonResponse(HttpStatus.OK, "[{\"type\":\"CREDIT\"}]");
        };

        CardValidationAdapter adapter = new CardValidationAdapter(WebClient.builder().exchangeFunction(exchangeFunction), "http://card");

        Boolean hasCard = adapter.hasCreditCard("cust-1").blockingGet();
        assertThat(hasCard).isTrue();
        assertThat(requestedPath.get()).contains("/api/v1/cards/customer/");
    }

    @Test
    void cardValidationAdapter_WhenOnlyDebitCards_ShouldReturnFalse() {
        ExchangeFunction exchangeFunction = request -> jsonResponse(HttpStatus.OK, "[{\"type\":\"DEBIT\"}]");
        CardValidationAdapter adapter = new CardValidationAdapter(WebClient.builder().exchangeFunction(exchangeFunction), "http://card");

        Boolean hasCard = adapter.hasCreditCard("cust-1").blockingGet();
        assertThat(hasCard).isFalse();
    }

    @Test
    void cardValidationAdapter_WhenError_ShouldReturnFalse() {
        ExchangeFunction exchangeFunction = request -> Mono.error(new RuntimeException("down"));
        CardValidationAdapter adapter = new CardValidationAdapter(WebClient.builder().exchangeFunction(exchangeFunction), "http://card");

        Boolean hasCard = adapter.hasCreditCard("cust-1").blockingGet();
        assertThat(hasCard).isFalse();
    }

    @Test
    void creditValidationAdapter_WhenOverdueExists_ShouldReturnTrue() {
        ExchangeFunction exchangeFunction = request -> jsonResponse(HttpStatus.OK, "[{\"status\":\"OVERDUE\"}]");
        CreditValidationAdapter adapter = new CreditValidationAdapter(WebClient.builder().exchangeFunction(exchangeFunction), "http://credit");

        Boolean hasOverdue = adapter.hasOverdueDebt("cust-1").blockingGet();
        assertThat(hasOverdue).isTrue();
    }

    @Test
    void creditValidationAdapter_WhenNoOverdue_ShouldReturnFalse() {
        ExchangeFunction exchangeFunction = request -> jsonResponse(HttpStatus.OK, "[{\"status\":\"ACTIVE\"}]");
        CreditValidationAdapter adapter = new CreditValidationAdapter(WebClient.builder().exchangeFunction(exchangeFunction), "http://credit");

        Boolean hasOverdue = adapter.hasOverdueDebt("cust-1").blockingGet();
        assertThat(hasOverdue).isFalse();
    }

    @Test
    void creditValidationAdapter_WhenError_ShouldReturnFalse() {
        ExchangeFunction exchangeFunction = request -> Mono.error(new RuntimeException("down"));
        CreditValidationAdapter adapter = new CreditValidationAdapter(WebClient.builder().exchangeFunction(exchangeFunction), "http://credit");

        Boolean hasOverdue = adapter.hasOverdueDebt("cust-1").blockingGet();
        assertThat(hasOverdue).isFalse();
    }

    private static Mono<ClientResponse> jsonResponse(HttpStatus status, String json) {
        return Mono.just(
                ClientResponse.create(status)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .body(json)
                        .build()
        );
    }
}
