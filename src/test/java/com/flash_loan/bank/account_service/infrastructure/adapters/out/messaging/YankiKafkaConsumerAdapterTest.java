package com.flash_loan.bank.account_service.infrastructure.adapters.out.messaging;

import com.flash_loan.bank.account_service.domain.model.SavingsAccount;
import com.flash_loan.bank.account_service.domain.ports.out.AccountRepositoryPort;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class YankiKafkaConsumerAdapterTest {

    @Mock
    private AccountRepositoryPort accountRepository;

    private YankiKafkaConsumerAdapter consumer;

    private SavingsAccount account;

    @BeforeEach
    void setUp() {
        consumer = new YankiKafkaConsumerAdapter(accountRepository);
        account = new SavingsAccount();
        account.setId("acc-1");
    }

    @Test
    void consumeYankiTransaction_FromCard_ShouldDebit() {
        WalletTransactionEvent event = WalletTransactionEvent.builder()
                .fromCardNumber("4111")
                .amount(new BigDecimal("10"))
                .build();

        when(accountRepository.findByCardNumber("4111")).thenReturn(Maybe.just(account));
        when(accountRepository.atomicDebit(anyString(), any(BigDecimal.class), anyBoolean())).thenReturn(Single.just(account));

        consumer.consumeYankiTransaction(event);

        verify(accountRepository).atomicDebit(eq("acc-1"), eq(new BigDecimal("10")), eq(false));
        verify(accountRepository, never()).atomicCredit(anyString(), any(), anyBoolean());
    }

    @Test
    void consumeYankiTransaction_ToCard_ShouldCredit() {
        WalletTransactionEvent event = WalletTransactionEvent.builder()
                .toCardNumber("5222")
                .amount(new BigDecimal("10"))
                .build();

        when(accountRepository.findByCardNumber("5222")).thenReturn(Maybe.just(account));
        when(accountRepository.atomicCredit(anyString(), any(BigDecimal.class), anyBoolean())).thenReturn(Single.just(account));

        consumer.consumeYankiTransaction(event);

        verify(accountRepository).atomicCredit(eq("acc-1"), eq(new BigDecimal("10")), eq(false));
        verify(accountRepository, never()).atomicDebit(anyString(), any(), anyBoolean());
    }

    @Test
    void consumeYankiTransaction_BothCards_ShouldDebitAndCredit() {
        WalletTransactionEvent event = WalletTransactionEvent.builder()
                .fromCardNumber("4111")
                .toCardNumber("5222")
                .amount(new BigDecimal("10"))
                .build();

        when(accountRepository.findByCardNumber(anyString())).thenReturn(Maybe.just(account));
        when(accountRepository.atomicDebit(anyString(), any(BigDecimal.class), anyBoolean())).thenReturn(Single.just(account));
        when(accountRepository.atomicCredit(anyString(), any(BigDecimal.class), anyBoolean())).thenReturn(Single.just(account));

        consumer.consumeYankiTransaction(event);

        verify(accountRepository).atomicDebit(eq("acc-1"), eq(new BigDecimal("10")), eq(false));
        verify(accountRepository).atomicCredit(eq("acc-1"), eq(new BigDecimal("10")), eq(false));
    }

    @Test
    void consumeYankiTransaction_NoCards_ShouldDoNothing() {
        WalletTransactionEvent event = WalletTransactionEvent.builder()
                .amount(new BigDecimal("10"))
                .build();

        consumer.consumeYankiTransaction(event);

        verifyNoInteractions(accountRepository);
    }
}
