package com.flash_loan.bank.account_service.infrastructure.adapters.in.web.mapper;

import com.flash_loan.bank.account_service.domain.model.Account;
import com.flash_loan.bank.account_service.domain.model.CheckingAccount;
import com.flash_loan.bank.account_service.domain.model.FixedTermAccount;
import com.flash_loan.bank.account_service.domain.model.SavingsAccount;
import com.flash_loan.bank.account_service.domain.model.enums.CustomerType;
import com.flash_loan.bank.account_service.infrastructure.adapters.in.web.dto.request.CheckingAccountRequestDto;
import com.flash_loan.bank.account_service.infrastructure.adapters.in.web.dto.request.FixedTermAccountRequestDto;
import com.flash_loan.bank.account_service.infrastructure.adapters.in.web.dto.request.SavingsAccountRequestDto;
import com.flash_loan.bank.account_service.infrastructure.adapters.in.web.dto.response.AccountResponseDto;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class AccountWebMapper {

    public AccountResponseDto toDto(Account account) {
        AccountResponseDto dto = new AccountResponseDto();
        BeanUtils.copyProperties(account, dto);
        
        Optional.ofNullable(account.getType()).ifPresent(t -> dto.setType(t.name()));
        Optional.ofNullable(account.getCustomerType()).ifPresent(ct -> dto.setCustomerType(ct.name()));
        
        // Use polymorphism-safe mapping
        mapSpecificFields(account, dto);
        
        dto.setIsVip(account.getIsVip());
        dto.setIsPyme(account.getIsPyme());
        
        return dto;
    }

    private void mapSpecificFields(Account account, AccountResponseDto dto) {
        Optional.of(account)
                .filter(SavingsAccount.class::isInstance)
                .map(SavingsAccount.class::cast)
                .ifPresent(s -> {
                    dto.setMaxMonthlyMovements(s.getMaxMonthlyMovements());
                    dto.setCurrentMovements(s.getCurrentMovements());
                });

        Optional.of(account)
                .filter(CheckingAccount.class::isInstance)
                .map(CheckingAccount.class::cast)
                .ifPresent(c -> dto.setMonthlyMaintenanceFee(c.getMonthlyMaintenanceFee()));

        Optional.of(account)
                .filter(FixedTermAccount.class::isInstance)
                .map(FixedTermAccount.class::cast)
                .ifPresent(f -> dto.setAllowedTransactionDay(f.getAllowedTransactionDay()));
    }

    public SavingsAccount toDomain(SavingsAccountRequestDto dto) {
        SavingsAccount account = new SavingsAccount();
        BeanUtils.copyProperties(dto, account);
        account.setCustomerType(CustomerType.PERSONAL); 
        account.setCreatedAt(LocalDateTime.now());
        return account;
    }

    public CheckingAccount toDomain(CheckingAccountRequestDto dto) {
        CheckingAccount account = new CheckingAccount();
        BeanUtils.copyProperties(dto, account);
        account.setCreatedAt(LocalDateTime.now());
        return account;
    }

    public FixedTermAccount toDomain(FixedTermAccountRequestDto dto) {
        FixedTermAccount account = new FixedTermAccount();
        BeanUtils.copyProperties(dto, account);
        account.setCustomerType(CustomerType.PERSONAL);
        account.setCreatedAt(LocalDateTime.now());
        return account;
    }
}