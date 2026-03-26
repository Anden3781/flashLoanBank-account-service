package com.flash_loan.bank.account_service.domain.ports.out;

import com.flash_loan.bank.account_service.domain.model.Account;
import com.flash_loan.bank.account_service.domain.model.AccountType;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import java.math.BigDecimal;

public interface AccountRepositoryPort {
    Single<Long> countByCustomerIdAndType(String customerId, AccountType type);
    Single<Account> save(Account account);
    Maybe<Account> findById(String id);
    Flowable<Account> findAll();
    Single<Boolean> deleteById(String id);
    Single<Account> atomicDebit(String id, BigDecimal amount, boolean increaseMovementCounter);
    Single<Account> atomicCredit(String id, BigDecimal amount, boolean increaseMovementCounter);
    Maybe<Account> findByCardNumber(String cardNumber);
    Flowable<Account> findByCustomerId(String customerId);
}
