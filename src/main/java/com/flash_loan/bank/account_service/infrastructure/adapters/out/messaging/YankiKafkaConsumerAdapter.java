package com.flash_loan.bank.account_service.infrastructure.adapters.out.messaging;

import com.flash_loan.bank.account_service.domain.ports.out.AccountRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class YankiKafkaConsumerAdapter {

    private final AccountRepositoryPort accountRepository;

    @KafkaListener(topics = "yanki-transactions", groupId = "account-service-group")
    public void consumeYankiTransaction(WalletTransactionEvent event) {
        log.info("Consuming Yanki transaction event: {}", event);

        if (event.getFromCardNumber() != null) {
            updateAccountBalance(event.getFromCardNumber(), event.getAmount().negate(), "Debit from Yanki");
        }

        if (event.getToCardNumber() != null) {
            updateAccountBalance(event.getToCardNumber(), event.getAmount(), "Credit from Yanki");
        }
    }

    private void updateAccountBalance(String cardNumber, java.math.BigDecimal amount, String action) {
        accountRepository.findByCardNumber(cardNumber)
                .flatMapSingle(account -> {
                    log.info("{} for account associated with card: {}", action, cardNumber);
                    if (amount.signum() < 0) {
                        return accountRepository.atomicDebit(account.getId(), amount.abs(), false);
                    } else {
                        return accountRepository.atomicCredit(account.getId(), amount, false);
                    }
                })
                .subscribe(
                    success -> log.info("Successfully updated account for card: {}", cardNumber),
                    error -> log.error("Failed to update account for card: {}. Error: {}", cardNumber, error.getMessage()));
    }
}
