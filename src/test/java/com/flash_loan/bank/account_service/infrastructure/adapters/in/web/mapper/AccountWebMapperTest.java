package com.flash_loan.bank.account_service.infrastructure.adapters.in.web.mapper;

import com.flash_loan.bank.account_service.domain.model.AccountType;
import com.flash_loan.bank.account_service.domain.model.CheckingAccount;
import com.flash_loan.bank.account_service.domain.model.FixedTermAccount;
import com.flash_loan.bank.account_service.domain.model.SavingsAccount;
import com.flash_loan.bank.account_service.domain.model.enums.CustomerType;
import com.flash_loan.bank.account_service.infrastructure.adapters.in.web.dto.request.CheckingAccountRequestDto;
import com.flash_loan.bank.account_service.infrastructure.adapters.in.web.dto.request.FixedTermAccountRequestDto;
import com.flash_loan.bank.account_service.infrastructure.adapters.in.web.dto.request.SavingsAccountRequestDto;
import com.flash_loan.bank.account_service.infrastructure.adapters.in.web.dto.response.AccountResponseDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AccountWebMapperTest {

    private final AccountWebMapper mapper = new AccountWebMapper();

    @Test
    void toDto_SavingsAccount_ShouldMapSpecificFields() {
        SavingsAccount account = new SavingsAccount();
        account.setId("acc-1");
        account.setCustomerId("cust-1");
        account.setType(AccountType.SAVINGS);
        account.setCustomerType(CustomerType.PERSONAL);
        account.setBalance(new BigDecimal("100"));
        account.setCreatedAt(LocalDateTime.now());
        account.setIsVip(true);
        account.setMaxMonthlyMovements(10);
        account.setCurrentMovements(2);

        AccountResponseDto dto = mapper.toDto(account);

        assertThat(dto.getId()).isEqualTo("acc-1");
        assertThat(dto.getType()).isEqualTo("SAVINGS");
        assertThat(dto.getCustomerType()).isEqualTo("PERSONAL");
        assertThat(dto.getIsVip()).isTrue();
        assertThat(dto.getMaxMonthlyMovements()).isEqualTo(10);
        assertThat(dto.getCurrentMovements()).isEqualTo(2);
    }

    @Test
    void toDto_CheckingAccount_ShouldMapMonthlyMaintenanceFee() {
        CheckingAccount account = new CheckingAccount();
        account.setId("acc-2");
        account.setCustomerId("cust-2");
        account.setType(AccountType.CHECKING);
        account.setBalance(new BigDecimal("200"));
        account.setIsPyme(true);
        account.setMonthlyMaintenanceFee(new BigDecimal("15.00"));

        AccountResponseDto dto = mapper.toDto(account);

        assertThat(dto.getType()).isEqualTo("CHECKING");
        assertThat(dto.getIsPyme()).isTrue();
        assertThat(dto.getMonthlyMaintenanceFee()).isEqualByComparingTo("15.00");
    }

    @Test
    void toDto_FixedTermAccount_ShouldMapAllowedTransactionDay() {
        FixedTermAccount account = new FixedTermAccount();
        account.setId("acc-3");
        account.setCustomerId("cust-3");
        account.setType(AccountType.FIXED_TERM);
        account.setAllowedTransactionDay(15);

        AccountResponseDto dto = mapper.toDto(account);

        assertThat(dto.getType()).isEqualTo("FIXED_TERM");
        assertThat(dto.getAllowedTransactionDay()).isEqualTo(15);
    }

    @Test
    void toDomain_SavingsAccountRequest_ShouldSetCreatedAtAndCustomerTypePersonal() {
        SavingsAccountRequestDto dto = new SavingsAccountRequestDto();
        dto.setCustomerId("cust-1");
        dto.setAccountType("SAVINGS");
        dto.setInitialBalance(new BigDecimal("100"));

        SavingsAccount account = mapper.toDomain(dto);

        assertThat(account.getCustomerId()).isEqualTo("cust-1");
        assertThat(account.getCreatedAt()).isNotNull();
        assertThat(account.getCustomerType()).isEqualTo(CustomerType.PERSONAL);
    }

    @Test
    void toDomain_CheckingAccountRequest_ShouldSetCreatedAt() {
        CheckingAccountRequestDto dto = new CheckingAccountRequestDto();
        dto.setCustomerId("cust-1");
        dto.setAccountType("CHECKING");
        dto.setInitialBalance(new BigDecimal("0"));

        CheckingAccount account = mapper.toDomain(dto);

        assertThat(account.getCustomerId()).isEqualTo("cust-1");
        assertThat(account.getCreatedAt()).isNotNull();
    }

    @Test
    void toDomain_FixedTermAccountRequest_ShouldSetCreatedAtAndCustomerTypePersonal() {
        FixedTermAccountRequestDto dto = new FixedTermAccountRequestDto();
        dto.setCustomerId("cust-1");
        dto.setAccountType("FIXED_TERM");
        dto.setOperatingDay(10);
        dto.setInitialBalance(new BigDecimal("50"));

        FixedTermAccount account = mapper.toDomain(dto);

        assertThat(account.getCustomerId()).isEqualTo("cust-1");
        assertThat(account.getCreatedAt()).isNotNull();
        assertThat(account.getCustomerType()).isEqualTo(CustomerType.PERSONAL);
    }
}
