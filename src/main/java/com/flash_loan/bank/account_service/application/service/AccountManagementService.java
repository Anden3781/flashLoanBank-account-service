package com.flash_loan.bank.account_service.application.service;

import com.flash_loan.bank.account_service.domain.exception.AccountNotFoundException;
import com.flash_loan.bank.account_service.domain.exception.BusinessRuleException;
import com.flash_loan.bank.account_service.domain.exception.CustomerNotFoundException;
import com.flash_loan.bank.account_service.domain.model.Account;
import com.flash_loan.bank.account_service.domain.model.AccountType;
import com.flash_loan.bank.account_service.domain.model.CheckingAccount;
import com.flash_loan.bank.account_service.domain.model.FixedTermAccount;
import com.flash_loan.bank.account_service.domain.model.SavingsAccount;
import com.flash_loan.bank.account_service.domain.model.enums.CustomerType;
import com.flash_loan.bank.account_service.domain.ports.out.AccountRepositoryPort;
import com.flash_loan.bank.account_service.domain.ports.out.CardValidationPort;
import com.flash_loan.bank.account_service.domain.ports.out.CreditValidationPort;
import com.flash_loan.bank.account_service.domain.ports.out.CustomerValidationPort;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountManagementService {

    private final CustomerValidationPort customerPort;
    private final AccountRepositoryPort accountPort;
    private final CreditValidationPort creditPort;
    private final CardValidationPort cardPort;

    private static final BigDecimal VIP_MIN_BALANCE = new BigDecimal("500.00");

    public Single<Account> createSavingsAccount(String customerId, BigDecimal initialBalance) {
        return validateNoOverdueDebt(customerId)
                .andThen(customerPort.getCustomerType(customerId)
                        .switchIfEmpty(Maybe.error(new CustomerNotFoundException("Customer does not exist")))
                        .filter(type -> type == CustomerType.PERSONAL)
                        .switchIfEmpty(Maybe.error(new BusinessRuleException("A BUSINESS customer cannot have a Savings Account")))
                        .flatMap(type -> accountPort.countByCustomerIdAndType(customerId, AccountType.SAVINGS).toMaybe())
                        .filter(count -> count == 0)
                        .switchIfEmpty(Maybe.error(new BusinessRuleException("A PERSONAL customer can only have ONE Savings Account")))
                        .map(unused -> createInitialSavingsAccount(customerId, initialBalance))
                        .flatMapSingle(this::applyVipLogic)
                        .flatMapSingle(accountPort::save)
                        .toSingle());
    }

    private SavingsAccount createInitialSavingsAccount(String customerId, BigDecimal initialBalance) {
        SavingsAccount account = new SavingsAccount();
        account.setCustomerId(customerId);
        account.setCustomerType(CustomerType.PERSONAL);
        account.setBalance(java.util.Optional.ofNullable(initialBalance).orElse(BigDecimal.ZERO));
        account.setCreatedAt(LocalDateTime.now());
        account.setType(AccountType.SAVINGS);
        return account;
    }

    private Single<Account> applyVipLogic(SavingsAccount account) {
        return cardPort.hasCreditCard(account.getCustomerId())
                .map(hasCard -> {
                    account.setIsVip(hasCard && account.getBalance().compareTo(VIP_MIN_BALANCE) >= 0);
                    return (Account) account;
                });
    }

    public Single<Account> createCheckingAccount(String customerId, BigDecimal initialBalance) {
        return validateNoOverdueDebt(customerId)
                .andThen(customerPort.getCustomerType(customerId)
                        .switchIfEmpty(Maybe.error(new CustomerNotFoundException("Customer does not exist")))
                        .flatMap(type -> validateCheckingAccountLimit(customerId, type))
                        .map(type -> createInitialCheckingAccount(customerId, type, initialBalance))
                        .flatMapSingle(this::applyPymeLogic)
                        .flatMapSingle(accountPort::save)
                        .toSingle());
    }

    private Maybe<CustomerType> validateCheckingAccountLimit(String customerId, CustomerType type) {
        return Maybe.just(type)
                .filter(t -> t != CustomerType.PERSONAL)
                .switchIfEmpty(accountPort.countByCustomerIdAndType(customerId, AccountType.CHECKING)
                        .filter(count -> count == 0)
                        .switchIfEmpty(Maybe.error(new BusinessRuleException("A PERSONAL customer can only have ONE Checking Account")))
                        .map(unused -> CustomerType.PERSONAL));
    }

    private CheckingAccount createInitialCheckingAccount(String customerId, CustomerType type, BigDecimal initialBalance) {
        CheckingAccount account = new CheckingAccount();
        account.setCustomerId(customerId);
        account.setCustomerType(type);
        account.setBalance(java.util.Optional.ofNullable(initialBalance).orElse(BigDecimal.ZERO));
        account.setCreatedAt(LocalDateTime.now());
        account.setType(AccountType.CHECKING);
        return account;
    }

    private Single<Account> applyPymeLogic(CheckingAccount account) {
        if (account.getCustomerType() != CustomerType.BUSINESS) {
            return Single.just(account);
        }

        return cardPort.hasCreditCard(account.getCustomerId())
                .map(hasCard -> {
                    if (hasCard) {
                        account.setIsPyme(true);
                        account.setMonthlyMaintenanceFee(BigDecimal.ZERO);
                    } else {
                        account.setIsPyme(false);
                        account.setMonthlyMaintenanceFee(new BigDecimal("15.00"));
                    }
                    return (Account) account;
                });
    }

    public Single<Account> createFixedTermAccount(String customerId, Integer operatingDay, BigDecimal initialBalance) {
        return validateNoOverdueDebt(customerId)
                .andThen(customerPort.getCustomerType(customerId)
                        .switchIfEmpty(Maybe.error(new CustomerNotFoundException("Customer does not exist")))
                        .filter(type -> type == CustomerType.PERSONAL)
                        .switchIfEmpty(Maybe.error(new BusinessRuleException("A BUSINESS customer cannot have a Fixed Term Account")))
                        .flatMap(type -> accountPort.countByCustomerIdAndType(customerId, AccountType.FIXED_TERM).toMaybe())
                        .filter(count -> count == 0)
                        .switchIfEmpty(Maybe.error(new BusinessRuleException("A PERSONAL customer can only have ONE Fixed Term Account")))
                        .map(unused -> {
                            FixedTermAccount account = new FixedTermAccount();
                            account.setCustomerId(customerId);
                            account.setCustomerType(CustomerType.PERSONAL);
                            account.setAllowedTransactionDay(operatingDay);
                            account.setBalance(java.util.Optional.ofNullable(initialBalance).orElse(BigDecimal.ZERO));
                            account.setCreatedAt(LocalDateTime.now());
                            account.setType(AccountType.FIXED_TERM);
                            return (Account) account;
                        })
                        .flatMapSingle(accountPort::save)
                        .toSingle());
    }

    public Single<Account> updateBalance(String id, BigDecimal newBalance) {
        return accountPort.findById(id)
                .switchIfEmpty(Maybe.error(new AccountNotFoundException("Account not found: " + id)))
                .map(account -> {
                    account.setBalance(newBalance);
                    return account;
                })
                .flatMapSingle(accountPort::save)
                .toSingle();
    }

    public Single<Account> getAccountById(String id) {
        return accountPort.findById(id)
                .switchIfEmpty(Maybe.error(new AccountNotFoundException("Account not found: " + id)))
                .toSingle();
    }

    public Flowable<Account> findAll() {
        return accountPort.findAll();
    }

    public Flowable<Account> findByCustomerId(String customerId) {
        log.info("Fetching all accounts for customer: {}", customerId);
        return accountPort.findByCustomerId(customerId);
    }

    public Single<Account> updateAccount(String id, Account account) {
        return accountPort.findById(id)
                .switchIfEmpty(Maybe.error(new AccountNotFoundException("Account not found for update: " + id)))
                .map(existing -> {
                    account.setId(id);
                    account.setCreatedAt(existing.getCreatedAt());
                    account.setBalance(java.util.Optional.ofNullable(account.getBalance()).orElse(existing.getBalance()));
                    return account;
                })
                .flatMapSingle(accountPort::save)
                .toSingle();
    }

    public Completable deleteAccount(String id) {
        return accountPort.deleteById(id)
                .filter(success -> success)
                .switchIfEmpty(Maybe.error(new AccountNotFoundException("Account not found for deletion: " + id)))
                .ignoreElement();
    }

    private Completable validateNoOverdueDebt(String customerId) {
        return creditPort.hasOverdueDebt(customerId)
                .filter(hasDebt -> !hasDebt)
                .switchIfEmpty(Maybe.error(new BusinessRuleException("Customer has overdue debt. Operation blocked.")))
                .ignoreElement();
    }
}