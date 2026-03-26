package com.flash_loan.bank.account_service.application.service;

import com.flash_loan.bank.account_service.domain.exception.AccountNotFoundException;
import com.flash_loan.bank.account_service.domain.exception.BusinessRuleException;
import com.flash_loan.bank.account_service.domain.model.Account;
import com.flash_loan.bank.account_service.domain.model.FixedTermAccount;
import com.flash_loan.bank.account_service.domain.model.SavingsAccount;
import com.flash_loan.bank.account_service.domain.ports.in.AccountTransferUseCase;
import com.flash_loan.bank.account_service.domain.ports.out.AccountRepositoryPort;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AccountTransferService implements AccountTransferUseCase {

    private final AccountRepositoryPort accountRepositoryPort;

    @Override
    public Single<Account> debit(String accountId, BigDecimal amount, String transactionId) {
        return validateOperationData(amount, transactionId)
                .andThen(accountRepositoryPort.findById(accountId)
                        .switchIfEmpty(Maybe.error(new AccountNotFoundException("Account not found: " + accountId)))
                        .flatMapSingle(account -> validateDebitRules(account, amount)
                                .andThen(accountRepositoryPort.atomicDebit(
                                        accountId,
                                        amount,
                                        shouldIncreaseMovementCounter(account))))
                        .toSingle());
    }

    @Override
    public Single<Account> credit(String accountId, BigDecimal amount, String transactionId) {
        return validateOperationData(amount, transactionId)
                .andThen(accountRepositoryPort.findById(accountId)
                        .switchIfEmpty(Maybe.error(new AccountNotFoundException("Account not found: " + accountId)))
                        .flatMapSingle(account -> validateCreditRules(account)
                                .andThen(accountRepositoryPort.atomicCredit(
                                        accountId,
                                        amount,
                                        shouldIncreaseMovementCounter(account))))
                        .toSingle());
    }

    private Completable validateOperationData(BigDecimal amount, String transactionId) {
        boolean invalidAmount = amount == null || amount.compareTo(BigDecimal.ZERO) <= 0;
        if (invalidAmount) {
            return Completable.error(new BusinessRuleException("Amount must be greater than zero"));
        }
        if (transactionId == null || transactionId.isBlank()) {
            return Completable.error(new BusinessRuleException("transactionId is required"));
        }
        return Completable.complete();
    }

    private Completable validateDebitRules(Account account, BigDecimal amount) {
        if (account.getBalance() == null || account.getBalance().compareTo(amount) < 0) {
            return Completable.error(new BusinessRuleException("Insufficient balance"));
        }
        if (account instanceof SavingsAccount savingsAccount) {
            Integer maxMovements = savingsAccount.getMaxMonthlyMovements();
            Integer currentMovements = savingsAccount.getCurrentMovements();
            if (maxMovements != null && currentMovements != null && currentMovements >= maxMovements) {
                return Completable.error(new BusinessRuleException("Monthly transaction limit reached"));
            }
        }
        if (account instanceof FixedTermAccount fixedTermAccount) {
            Integer allowedDay = fixedTermAccount.getAllowedTransactionDay();
            int currentDay = LocalDate.now().getDayOfMonth();
            if (allowedDay == null || allowedDay != currentDay) {
                return Completable.error(new BusinessRuleException("Fixed-term account allows transactions only on day " + allowedDay));
            }
        }
        return Completable.complete();
    }

    private Completable validateCreditRules(Account account) {
        if (account instanceof SavingsAccount savingsAccount) {
            Integer maxMovements = savingsAccount.getMaxMonthlyMovements();
            Integer currentMovements = savingsAccount.getCurrentMovements();
            if (maxMovements != null && currentMovements != null && currentMovements >= maxMovements) {
                return Completable.error(new BusinessRuleException("Monthly transaction limit reached"));
            }
        }
        if (account instanceof FixedTermAccount fixedTermAccount) {
            Integer allowedDay = fixedTermAccount.getAllowedTransactionDay();
            int currentDay = LocalDate.now().getDayOfMonth();
            if (allowedDay == null || allowedDay != currentDay) {
                return Completable.error(new BusinessRuleException("Fixed-term account allows transactions only on day " + allowedDay));
            }
        }
        return Completable.complete();
    }

    private boolean shouldIncreaseMovementCounter(Account account) {
        return account instanceof SavingsAccount || account instanceof FixedTermAccount;
    }
}
