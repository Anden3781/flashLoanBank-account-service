package com.flash_loan.bank.account_service.application.service;

import com.flash_loan.bank.account_service.domain.exception.BusinessRuleException;
import com.flash_loan.bank.account_service.domain.exception.CustomerNotFoundException;
import com.flash_loan.bank.account_service.domain.model.*;
import com.flash_loan.bank.account_service.domain.model.enums.CustomerType;
import com.flash_loan.bank.account_service.domain.ports.out.AccountRepositoryPort;
import com.flash_loan.bank.account_service.domain.ports.out.CustomerValidationPort;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AccountManagementService {

    private final CustomerValidationPort customerPort;
    private final AccountRepositoryPort accountPort;

    public Single<Account> createSavingsAccount(String customerId) {
        return customerPort.getCustomerType(customerId)
                .switchIfEmpty(Single.error(new CustomerNotFoundException("Customer does not exist")))
                .filter(type -> type == CustomerType.PERSONAL)
                .switchIfEmpty(Single.error(new BusinessRuleException("A BUSINESS customer cannot have a Savings Account")))
                .flatMap(type -> accountPort.countByCustomerIdAndType(customerId, AccountType.SAVINGS)
                        .filter(count -> count == 0L)
                        .switchIfEmpty(Single.error(new BusinessRuleException("A PERSONAL customer can only have ONE Savings Account")))
                )
                .map(unused -> {
                    SavingsAccount account = new SavingsAccount();
                    account.setCustomerId(customerId);
                    account.setCustomerType(CustomerType.PERSONAL);
                    account.setCreatedAt(LocalDateTime.now());
                    return account;
                })
                .flatMap(accountPort::save);
    }

    public Single<Account> createCheckingAccount(String customerId) {
        return customerPort.getCustomerType(customerId)
                .switchIfEmpty(Single.error(new CustomerNotFoundException("Customer does not exist")))
                .flatMap(type -> Single.just(type)
                        .filter(t -> t == CustomerType.PERSONAL)
                        .flatMap(t -> accountPort.countByCustomerIdAndType(customerId, AccountType.CHECKING).toMaybe()
                                .filter(count -> count == 0L)
                                .switchIfEmpty(Maybe.error(new BusinessRuleException("A PERSONAL customer can only have ONE Checking Account")))
                                .map(c -> type)
                        )
                        .switchIfEmpty(Single.just(type))
                )
                .map(validType -> {
                    CheckingAccount account = new CheckingAccount();
                    account.setCustomerId(customerId);
                    account.setCustomerType(validType);
                    account.setCreatedAt(LocalDateTime.now());
                    return account;
                })
                .flatMap(accountPort::save);
    }

    public Single<Account> createFixedTermAccount(String customerId, Integer operatingDay) {
        return customerPort.getCustomerType(customerId)
                .switchIfEmpty(Single.error(new CustomerNotFoundException("Customer does not exist")))
                .filter(type -> type == CustomerType.PERSONAL)
                .switchIfEmpty(Single.error(new BusinessRuleException("A BUSINESS customer cannot have a Fixed Term Account")))
                .map(type -> {
                    FixedTermAccount account = new FixedTermAccount();
                    account.setCustomerId(customerId);
                    account.setCustomerType(CustomerType.PERSONAL);
                    account.setAllowedTransactionDay(operatingDay);
                    account.setCreatedAt(LocalDateTime.now());
                    return account;
                })
                .flatMap(accountPort::save);
    }
}
