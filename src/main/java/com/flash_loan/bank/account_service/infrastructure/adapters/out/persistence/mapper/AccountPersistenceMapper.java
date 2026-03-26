package com.flash_loan.bank.account_service.infrastructure.adapters.out.persistence.mapper;

import com.flash_loan.bank.account_service.domain.model.Account;
import com.flash_loan.bank.account_service.domain.model.CheckingAccount;
import com.flash_loan.bank.account_service.domain.model.FixedTermAccount;
import com.flash_loan.bank.account_service.domain.model.SavingsAccount;
import com.flash_loan.bank.account_service.infrastructure.adapters.out.persistence.entity.AccountEntity;
import com.flash_loan.bank.account_service.infrastructure.adapters.out.persistence.entity.CheckingAccountEntity;
import com.flash_loan.bank.account_service.infrastructure.adapters.out.persistence.entity.FixedTermAccountEntity;
import com.flash_loan.bank.account_service.infrastructure.adapters.out.persistence.entity.SavingsAccountEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@Component
public class AccountPersistenceMapper {

    private final Map<Class<? extends Account>, Supplier<AccountEntity>> entityFactories = Map.of(
            SavingsAccount.class, SavingsAccountEntity::new,
            CheckingAccount.class, CheckingAccountEntity::new,
            FixedTermAccount.class, FixedTermAccountEntity::new
    );

    private final Map<Class<? extends AccountEntity>, Supplier<Account>> domainFactories = Map.of(
            SavingsAccountEntity.class, SavingsAccount::new,
            CheckingAccountEntity.class, CheckingAccount::new,
            FixedTermAccountEntity.class, FixedTermAccount::new
    );

    public AccountEntity toEntity(Account account) {
        return Optional.ofNullable(account)
                .map(acc -> entityFactories.get(acc.getClass()))
                .map(Supplier::get)
                .map(entity -> {
                    BeanUtils.copyProperties(account, entity);
                    return entity;
                })
                .orElse(null);
    }

    public Account toDomain(AccountEntity entity) {
        return Optional.ofNullable(entity)
                .map(ent -> domainFactories.get(ent.getClass()))
                .map(Supplier::get)
                .map(domain -> {
                    BeanUtils.copyProperties(entity, domain);
                    return domain;
                })
                .orElse(null);
    }
}
