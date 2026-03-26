package com.flash_loan.bank.account_service.infrastructure.adapters.out.web;

import com.flash_loan.bank.account_service.domain.ports.out.CreditValidationPort;
import io.reactivex.rxjava3.core.Single;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class CreditValidationAdapter implements CreditValidationPort {

    private final WebClient webClient;

    public CreditValidationAdapter(WebClient.Builder webClientBuilder,
                                  @Value("${services.credit.url:http://localhost:8083}") String creditUrl) {
        this.webClient = webClientBuilder.baseUrl(creditUrl).build();
    }

    @Override
    public Single<Boolean> hasOverdueDebt(String customerId) {
        return Single.fromPublisher(webClient.get()
                .uri("/api/v1/credits/customer/{customerId}", customerId)
                .retrieve()
                .bodyToFlux(CreditResponse.class)
                .filter(c -> "OVERDUE".equals(c.status))
                .hasElements())
                .onErrorReturnItem(false);
    }

    private static class CreditResponse {
        public String status;
    }
}
