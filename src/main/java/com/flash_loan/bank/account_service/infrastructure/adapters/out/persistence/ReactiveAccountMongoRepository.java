package com.flash_loan.bank.account_service.infrastructure.adapters.out.persistence;

import com.flash_loan.bank.account_service.infrastructure.adapters.out.persistence.entity.AccountEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ReactiveAccountMongoRepository extends ReactiveMongoRepository<AccountEntity, String> {
    Mono<Long> countByCustomerIdAndType(String customerId, com.flash_loan.bank.account_service.domain.model.AccountType type);
    Mono<AccountEntity> findByDebitCardNumber(String debitCardNumber);
}
