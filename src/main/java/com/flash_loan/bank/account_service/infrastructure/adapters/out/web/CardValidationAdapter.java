package com.flash_loan.bank.account_service.infrastructure.adapters.out.web;

import com.flash_loan.bank.account_service.domain.ports.out.CardValidationPort;
import io.reactivex.rxjava3.core.Single;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class CardValidationAdapter implements CardValidationPort {

    private final WebClient webClient;

    public CardValidationAdapter(WebClient.Builder webClientBuilder,
                                @Value("${services.card.url:http://localhost:8085}") String cardUrl) {
        this.webClient = webClientBuilder.baseUrl(cardUrl).build();
    }

    @Override
    public Single<Boolean> hasCreditCard(String customerId) {
        return Single.fromPublisher(webClient.get()
                .uri("/api/v1/cards/customer/{customerId}", customerId)
                .retrieve()
                .bodyToFlux(CardResponse.class)
                .filter(c -> "CREDIT".equals(c.type))
                .hasElements())
                .onErrorReturnItem(false);
    }

    private static class CardResponse {
        public String type;
    }
}
