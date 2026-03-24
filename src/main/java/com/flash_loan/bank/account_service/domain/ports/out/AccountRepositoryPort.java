package com.flash_loan.bank.account_service.domain.ports.out;

import com.flash_loan.bank.account_service.domain.model.Account;
import com.flash_loan.bank.account_service.domain.model.AccountType;
import io.reactivex.rxjava3.core.Single;

public interface AccountRepositoryPort {
    Single<Long> countByCustomerIdAndType(String customerId, AccountType type);
    Single<Account> save(Account account);
}
