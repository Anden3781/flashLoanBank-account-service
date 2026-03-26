package com.flash_loan.bank.account_service.application.service;

import com.flash_loan.bank.account_service.domain.exception.AccountNotFoundException;
import com.flash_loan.bank.account_service.domain.exception.BusinessRuleException;
import com.flash_loan.bank.account_service.domain.model.Account;
import com.flash_loan.bank.account_service.domain.model.FixedTermAccount;
import com.flash_loan.bank.account_service.domain.model.SavingsAccount;
import com.flash_loan.bank.account_service.domain.ports.out.AccountRepositoryPort;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountTransferServiceTest {

    @Mock
    private AccountRepositoryPort accountRepositoryPort;

    @InjectMocks
    private AccountTransferService service;

    private SavingsAccount savings;

    @BeforeEach
    void setUp() {
        savings = new SavingsAccount();
        savings.setId("acc-1");
        savings.setBalance(new BigDecimal("100"));
        savings.setMaxMonthlyMovements(10);
        savings.setCurrentMovements(0);
    }

    @Test
    void debit_Success_ShouldCallAtomicDebit() {
        when(accountRepositoryPort.findById("acc-1")).thenReturn(Maybe.just(savings));
        when(accountRepositoryPort.atomicDebit(anyString(), any(BigDecimal.class), anyBoolean())).thenReturn(Single.just(savings));

        Account result = service.debit("acc-1", new BigDecimal("10"), "tx-1").blockingGet();

        assertThat(result).isNotNull();
        verify(accountRepositoryPort).atomicDebit("acc-1", new BigDecimal("10"), true);
    }

    @Test
    void debit_Error_InvalidAmount() {
        when(accountRepositoryPort.findById(anyString())).thenReturn(Maybe.just(savings));
        assertThatThrownBy(() -> service.debit("acc-1", BigDecimal.ZERO, "tx-1").blockingGet())
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void debit_Error_MissingTransactionId() {
        when(accountRepositoryPort.findById(anyString())).thenReturn(Maybe.just(savings));
        assertThatThrownBy(() -> service.debit("acc-1", new BigDecimal("1"), " ").blockingGet())
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void debit_Error_AccountNotFound() {
        when(accountRepositoryPort.findById("acc-1")).thenReturn(Maybe.empty());

        assertThatThrownBy(() -> service.debit("acc-1", new BigDecimal("1"), "tx-1").blockingGet())
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void debit_Error_InsufficientBalance() {
        savings.setBalance(BigDecimal.ZERO);
        when(accountRepositoryPort.findById("acc-1")).thenReturn(Maybe.just(savings));
        when(accountRepositoryPort.atomicDebit(anyString(), any(BigDecimal.class), anyBoolean())).thenReturn(Single.just(savings));

        assertThatThrownBy(() -> service.debit("acc-1", new BigDecimal("1"), "tx-1").blockingGet())
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Insufficient balance");
    }

    @Test
    void debit_Error_SavingsMonthlyLimitReached() {
        savings.setCurrentMovements(10);
        when(accountRepositoryPort.findById("acc-1")).thenReturn(Maybe.just(savings));
        when(accountRepositoryPort.atomicDebit(anyString(), any(BigDecimal.class), anyBoolean())).thenReturn(Single.just(savings));

        assertThatThrownBy(() -> service.debit("acc-1", new BigDecimal("1"), "tx-1").blockingGet())
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Monthly transaction limit reached");
    }

    @Test
    void debit_Error_FixedTermWrongDay() {
        FixedTermAccount fixed = new FixedTermAccount();
        fixed.setId("acc-2");
        fixed.setBalance(new BigDecimal("100"));
        fixed.setAllowedTransactionDay(LocalDate.now().getDayOfMonth() + 1);

        when(accountRepositoryPort.findById("acc-2")).thenReturn(Maybe.just(fixed));
        when(accountRepositoryPort.atomicDebit(anyString(), any(BigDecimal.class), anyBoolean())).thenReturn(Single.just(fixed));

        assertThatThrownBy(() -> service.debit("acc-2", new BigDecimal("1"), "tx-1").blockingGet())
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Fixed-term account allows transactions only on day");
    }

    @Test
    void credit_Success_FixedTermCorrectDay_ShouldCallAtomicCredit() {
        FixedTermAccount fixed = new FixedTermAccount();
        fixed.setId("acc-2");
        fixed.setBalance(new BigDecimal("100"));
        fixed.setAllowedTransactionDay(LocalDate.now().getDayOfMonth());

        when(accountRepositoryPort.findById("acc-2")).thenReturn(Maybe.just(fixed));
        when(accountRepositoryPort.atomicCredit(anyString(), any(BigDecimal.class), anyBoolean())).thenReturn(Single.just(fixed));

        Account result = service.credit("acc-2", new BigDecimal("1"), "tx-1").blockingGet();

        assertThat(result).isNotNull();
        verify(accountRepositoryPort).atomicCredit("acc-2", new BigDecimal("1"), true);
    }
}
