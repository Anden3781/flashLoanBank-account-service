package com.flash_loan.bank.account_service.infrastructure.adapters.out;

import com.flash_loan.bank.account_service.domain.model.Account;
import com.flash_loan.bank.account_service.domain.model.AccountType;
import com.flash_loan.bank.account_service.domain.ports.out.AccountRepositoryPort;
import io.reactivex.rxjava3.core.Single;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class MockAccountRepositoryAdapter implements AccountRepositoryPort {

    private final List<Account> database = new ArrayList<>();

    @Override
    public Single<Long> countByCustomerIdAndType(String customerId, AccountType type) {
        long occurrences = database.stream()
                .filter(account -> account.getCustomerId().equals(customerId) && account.getType() == type)
                .count();
        return Single.just(occurrences);
    }

    @Override
    public Single<Account> save(Account account) {
        if (account.getId() == null) {
            account.setId(UUID.randomUUID().toString());
        }
        database.add(account);
        return Single.just(account);
    }
}
