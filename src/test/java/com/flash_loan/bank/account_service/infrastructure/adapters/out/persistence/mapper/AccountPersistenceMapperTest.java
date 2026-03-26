package com.flash_loan.bank.account_service.infrastructure.adapters.out.persistence.mapper;

import com.flash_loan.bank.account_service.domain.model.CheckingAccount;
import com.flash_loan.bank.account_service.domain.model.FixedTermAccount;
import com.flash_loan.bank.account_service.domain.model.SavingsAccount;
import com.flash_loan.bank.account_service.infrastructure.adapters.out.persistence.entity.AccountEntity;
import com.flash_loan.bank.account_service.infrastructure.adapters.out.persistence.entity.CheckingAccountEntity;
import com.flash_loan.bank.account_service.infrastructure.adapters.out.persistence.entity.FixedTermAccountEntity;
import com.flash_loan.bank.account_service.infrastructure.adapters.out.persistence.entity.SavingsAccountEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class AccountPersistenceMapperTest {

    private final AccountPersistenceMapper mapper = new AccountPersistenceMapper();

    @Test
    void toEntity_and_toDomain_Savings_ShouldPreserveFields() {
        SavingsAccount domain = new SavingsAccount();
        domain.setId("acc-1");
        domain.setCustomerId("cust-1");
        domain.setBalance(new BigDecimal("100"));
        domain.setMaxMonthlyMovements(10);
        domain.setCurrentMovements(1);

        AccountEntity entity = mapper.toEntity(domain);
        assertThat(entity).isInstanceOf(SavingsAccountEntity.class);
        SavingsAccountEntity se = (SavingsAccountEntity) entity;
        assertThat(se.getCustomerId()).isEqualTo("cust-1");
        assertThat(se.getCurrentMovements()).isEqualTo(1);

        SavingsAccount back = (SavingsAccount) mapper.toDomain(se);
        assertThat(back.getId()).isEqualTo("acc-1");
        assertThat(back.getBalance()).isEqualByComparingTo("100");
        assertThat(back.getMaxMonthlyMovements()).isEqualTo(10);
    }

    @Test
    void toEntity_and_toDomain_Checking_ShouldPreserveFields() {
        CheckingAccount domain = new CheckingAccount();
        domain.setId("acc-2");
        domain.setCustomerId("cust-2");
        domain.setBalance(new BigDecimal("200"));
        domain.setMonthlyMaintenanceFee(new BigDecimal("15.00"));

        AccountEntity entity = mapper.toEntity(domain);
        assertThat(entity).isInstanceOf(CheckingAccountEntity.class);
        CheckingAccountEntity ce = (CheckingAccountEntity) entity;
        assertThat(ce.getMonthlyMaintenanceFee()).isEqualByComparingTo("15.00");

        CheckingAccount back = (CheckingAccount) mapper.toDomain(ce);
        assertThat(back.getId()).isEqualTo("acc-2");
        assertThat(back.getMonthlyMaintenanceFee()).isEqualByComparingTo("15.00");
    }

    @Test
    void toEntity_and_toDomain_FixedTerm_ShouldPreserveFields() {
        FixedTermAccount domain = new FixedTermAccount();
        domain.setId("acc-3");
        domain.setCustomerId("cust-3");
        domain.setBalance(new BigDecimal("300"));
        domain.setAllowedTransactionDay(7);

        AccountEntity entity = mapper.toEntity(domain);
        assertThat(entity).isInstanceOf(FixedTermAccountEntity.class);
        FixedTermAccountEntity fe = (FixedTermAccountEntity) entity;
        assertThat(fe.getAllowedTransactionDay()).isEqualTo(7);

        FixedTermAccount back = (FixedTermAccount) mapper.toDomain(fe);
        assertThat(back.getId()).isEqualTo("acc-3");
        assertThat(back.getAllowedTransactionDay()).isEqualTo(7);
    }

    @Test
    void toEntity_Null_ReturnsNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    void toDomain_Null_ReturnsNull() {
        assertThat(mapper.toDomain(null)).isNull();
    }
}
