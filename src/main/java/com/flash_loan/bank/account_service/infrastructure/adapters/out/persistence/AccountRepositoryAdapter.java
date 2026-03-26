package com.flash_loan.bank.account_service.infrastructure.adapters.out.persistence;

import com.flash_loan.bank.account_service.domain.model.Account;
import com.flash_loan.bank.account_service.domain.model.AccountType;
import com.flash_loan.bank.account_service.domain.exception.BusinessRuleException;
import com.flash_loan.bank.account_service.domain.ports.out.AccountRepositoryPort;
import com.flash_loan.bank.account_service.infrastructure.adapters.out.persistence.entity.AccountEntity;
import com.flash_loan.bank.account_service.infrastructure.adapters.out.persistence.mapper.AccountPersistenceMapper;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountRepositoryAdapter implements AccountRepositoryPort {

    private final ReactiveAccountMongoRepository repository;
    private final AccountPersistenceMapper mapper;
    private final ReactiveMongoTemplate mongoTemplate;

    @Override
    public Single<Long> countByCustomerIdAndType(String customerId, AccountType type) {
        // Adaptación nativa transparente de Reactor Mono a RxJava3 Single usando el estándar Publisher
        return Single.fromPublisher(repository.countByCustomerIdAndType(customerId, type));
    }

    @Override
    public Single<Account> save(Account account) {
        AccountEntity entity = mapper.toEntity(account);
        return Single.fromPublisher(repository.save(entity))
                .map(mapper::toDomain);
    }

    @Override
    public Maybe<Account> findById(String id) {
        log.info("Repository - fetching account by id: {}", id);
        return Maybe.fromPublisher(repository.findById(id))
                .doOnSuccess(entity -> log.debug("Repository - successfully retrieved entity for id: {}", id))
                .map(mapper::toDomain);
    }

    @Override
    public Flowable<Account> findAll() {
        log.info("Repository - listing all accounts");
        return Flowable.fromPublisher(repository.findAll())
                .map(mapper::toDomain);
    }

    @Override
    public Single<Boolean> deleteById(String id) {
        return repository.existsById(id)
                .filter(Boolean::booleanValue)
                .flatMap(unused -> repository.deleteById(id).thenReturn(true))
                .defaultIfEmpty(false)
                .as(io.reactivex.rxjava3.core.Single::fromPublisher);
    }

    @Override
    public Single<Account> atomicDebit(String id, BigDecimal amount, boolean increaseMovementCounter) {
        Query query = Query.query(Criteria.where("id").is(id).and("balance").gte(amount));
        Update update = new Update().inc("balance", amount.negate());
        if (increaseMovementCounter) {
            update.inc("currentMovements", 1);
        }
        return Maybe.fromPublisher(
                mongoTemplate.findAndModify(
                        query,
                        update,
                        FindAndModifyOptions.options().returnNew(true),
                        AccountEntity.class
                )
        ).switchIfEmpty(Maybe.error(new BusinessRuleException("Atomic debit failed due to insufficient balance or account state")))
                .toSingle()
                .map(mapper::toDomain);
    }

    @Override
    public Single<Account> atomicCredit(String id, BigDecimal amount, boolean increaseMovementCounter) {
        Query query = Query.query(Criteria.where("id").is(id));
        Update update = new Update().inc("balance", amount);
        if (increaseMovementCounter) {
            update.inc("currentMovements", 1);
        }
        return Maybe.fromPublisher(
                mongoTemplate.findAndModify(
                        query,
                        update,
                        FindAndModifyOptions.options().returnNew(true),
                        AccountEntity.class
                )
        ).switchIfEmpty(Maybe.error(new BusinessRuleException("Atomic credit failed due to account state")))
                .toSingle()
                .map(mapper::toDomain);
    }

    @Override
    public Maybe<Account> findByCardNumber(String cardNumber) {
        log.info("Repository - fetching account by card number: {}", cardNumber);
        return Maybe.fromPublisher(repository.findByDebitCardNumber(cardNumber))
                .map(mapper::toDomain);
    }
}
