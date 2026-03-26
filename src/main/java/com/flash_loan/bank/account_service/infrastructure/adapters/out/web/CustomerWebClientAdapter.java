package com.flash_loan.bank.account_service.infrastructure.adapters.out.web;

import com.flash_loan.bank.account_service.domain.exception.CustomerNotFoundException;
import com.flash_loan.bank.account_service.domain.model.enums.CustomerType;
import com.flash_loan.bank.account_service.domain.ports.out.CustomerValidationPort;
import com.flash_loan.bank.account_service.infrastructure.adapters.out.web.dto.CustomerResponseDto;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CustomerWebClientAdapter implements CustomerValidationPort {

    private final WebClient customerWebClient;

    @Override
    public Maybe<CustomerType> getCustomerType(String customerId) {
        
        Mono<CustomerType> reactorMono = customerWebClient.get()
                .uri("/{id}", customerId)
                .retrieve()
                .onStatus(status -> status.value() == HttpStatus.NOT_FOUND.value(), 
                          response -> Mono.error(new CustomerNotFoundException("Customer with ID " + customerId + " not found in external service.")))
                .onStatus(status -> status.is5xxServerError(),
                          response -> Mono.error(new RuntimeException("Error communicating with Customer Service API")))
                .bodyToMono(CustomerResponseDto.class)
                .map(dto -> {
                    try {
                        return CustomerType.valueOf(dto.getCustomerType().toUpperCase());
                    } catch (Exception e) {
                        throw new RuntimeException("Invalid customer type format received from Customer Service.");
                    }
                });

        // Bridge: Reactive Streams Interoperability (Mono -> Publisher -> Flowable -> Maybe)
        return Flowable.fromPublisher(reactorMono).firstElement();
    }
}
