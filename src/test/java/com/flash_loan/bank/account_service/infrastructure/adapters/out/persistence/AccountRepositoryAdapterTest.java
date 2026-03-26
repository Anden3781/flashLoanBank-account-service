package com.flash_loan.bank.account_service.infrastructure.adapters.out.persistence;

import com.flash_loan.bank.account_service.domain.exception.BusinessRuleException;
import com.flash_loan.bank.account_service.domain.model.Account;
import com.flash_loan.bank.account_service.domain.model.AccountType;
import com.flash_loan.bank.account_service.domain.model.SavingsAccount;
import com.flash_loan.bank.account_service.infrastructure.adapters.out.persistence.entity.AccountEntity;
import com.flash_loan.bank.account_service.infrastructure.adapters.out.persistence.entity.SavingsAccountEntity;
import com.flash_loan.bank.account_service.infrastructure.adapters.out.persistence.mapper.AccountPersistenceMapper;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.LENIENT)
class AccountRepositoryAdapterTest {

    private ReactiveAccountMongoRepository repository;
    private AccountPersistenceMapper mapper;
    private ReactiveMongoTemplate mongoTemplate;
    private AccountRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        repository = mock(ReactiveAccountMongoRepository.class);
        mapper = new AccountPersistenceMapper();
        mongoTemplate = mock(ReactiveMongoTemplate.class);
        adapter = new AccountRepositoryAdapter(repository, mapper, mongoTemplate);
    }

    @Test
    void countByCustomerIdAndType_ShouldReturnCount() {
        when(repository.countByCustomerIdAndType(anyString(), any(AccountType.class))).thenReturn(Mono.just(2L));

        Long result = adapter.countByCustomerIdAndType("cust-1", AccountType.SAVINGS).blockingGet();

        assertThat(result).isEqualTo(2L);
    }

    @Test
    void save_ShouldMapAndPersist() {
        SavingsAccount domain = new SavingsAccount();
        domain.setId("acc-1");
        domain.setCustomerId("cust-1");
        domain.setBalance(new BigDecimal("100"));

        SavingsAccountEntity savedEntity = new SavingsAccountEntity();
        savedEntity.setId("acc-1");
        savedEntity.setCustomerId("cust-1");
        savedEntity.setBalance(new BigDecimal("100"));

        when(repository.save(any(AccountEntity.class))).thenReturn(Mono.just(savedEntity));

        Account saved = adapter.save(domain).blockingGet();

        assertThat(saved.getId()).isEqualTo("acc-1");
        assertThat(saved.getCustomerId()).isEqualTo("cust-1");
    }

    @Test
    void findById_WhenExists_ShouldReturnMaybe() {
        SavingsAccountEntity entity = new SavingsAccountEntity();
        entity.setId("acc-1");
        entity.setCustomerId("cust-1");
        entity.setBalance(new BigDecimal("100"));

        when(repository.findById("acc-1")).thenReturn(Mono.just(entity));

        Account found = adapter.findById("acc-1").blockingGet();
        assertThat(found.getId()).isEqualTo("acc-1");
    }

    @Test
    void findAll_ShouldReturnFlowable() {
        SavingsAccountEntity entity = new SavingsAccountEntity();
        entity.setId("acc-1");
        entity.setCustomerId("cust-1");
        entity.setBalance(new BigDecimal("100"));

        when(repository.findAll()).thenReturn(Flux.just(entity));

        assertThat(adapter.findAll().toList().blockingGet()).hasSize(1);
    }

    @Test
    void findByCustomerId_ShouldReturnFlowable() {
        SavingsAccountEntity entity = new SavingsAccountEntity();
        entity.setId("acc-1");
        entity.setCustomerId("cust-1");
        entity.setBalance(new BigDecimal("100"));

        when(repository.findByCustomerId("cust-1")).thenReturn(Flux.just(entity));

        assertThat(adapter.findByCustomerId("cust-1").toList().blockingGet()).hasSize(1);
    }

    @Test
    void deleteById_WhenExists_ShouldReturnTrue() {
        when(repository.existsById("acc-1")).thenReturn(Mono.just(true));
        when(repository.deleteById("acc-1")).thenReturn(Mono.empty());

        Boolean result = adapter.deleteById("acc-1").blockingGet();
        assertThat(result).isTrue();
    }

    @Test
    void deleteById_WhenNotExists_ShouldReturnFalse() {
        when(repository.existsById("acc-1")).thenReturn(Mono.just(false));

        Boolean result = adapter.deleteById("acc-1").blockingGet();
        assertThat(result).isFalse();
    }

    @Test
    void findByCardNumber_WhenExists_ShouldReturnMaybe() {
        SavingsAccountEntity entity = new SavingsAccountEntity();
        entity.setId("acc-1");
        entity.setCustomerId("cust-1");
        entity.setBalance(new BigDecimal("100"));

        when(repository.findByDebitCardNumber("4111")).thenReturn(Mono.just(entity));

        Account found = adapter.findByCardNumber("4111").blockingGet();
        assertThat(found.getId()).isEqualTo("acc-1");
    }

    @Test
    void atomicDebit_WhenMongoReturnsEmpty_ShouldError() {
        when(mongoTemplate.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class), any(Class.class)))
                .thenReturn(Mono.empty());

        assertThatThrownBy(() -> adapter.atomicDebit("acc-1", new BigDecimal("10"), true).blockingGet())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Atomic debit failed");
    }

    @Test
    void atomicDebit_WhenMongoReturnsEntity_ShouldReturnUpdatedAccount() {
        SavingsAccountEntity entity = new SavingsAccountEntity();
        entity.setId("acc-1");
        entity.setCustomerId("cust-1");
        entity.setBalance(new BigDecimal("90"));

        when(mongoTemplate.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class), any(Class.class)))
                .thenReturn(Mono.just(entity));

        Account updated = adapter.atomicDebit("acc-1", new BigDecimal("10"), true).blockingGet();
        assertThat(updated.getBalance()).isEqualByComparingTo("90");
    }

    @Test
    void atomicCredit_WhenMongoReturnsEmpty_ShouldError() {
        when(mongoTemplate.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class), any(Class.class)))
                .thenReturn(Mono.empty());

        assertThatThrownBy(() -> adapter.atomicCredit("acc-1", new BigDecimal("10"), false).blockingGet())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Atomic credit failed");
    }

    @Test
    void atomicCredit_WhenMongoReturnsEntity_ShouldReturnUpdatedAccount() {
        SavingsAccountEntity entity = new SavingsAccountEntity();
        entity.setId("acc-1");
        entity.setCustomerId("cust-1");
        entity.setBalance(new BigDecimal("110"));

        when(mongoTemplate.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class), any(Class.class)))
                .thenReturn(Mono.just(entity));

        Account updated = adapter.atomicCredit("acc-1", new BigDecimal("10"), false).blockingGet();
        assertThat(updated.getBalance()).isEqualByComparingTo("110");
    }
}
