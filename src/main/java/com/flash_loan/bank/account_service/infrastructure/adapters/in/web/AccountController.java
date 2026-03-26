package com.flash_loan.bank.account_service.infrastructure.adapters.in.web;

import com.flash_loan.bank.account_service.application.service.AccountManagementService;
import com.flash_loan.bank.account_service.domain.model.Account;
import com.flash_loan.bank.account_service.domain.ports.in.AccountTransferUseCase;
import com.flash_loan.bank.account_service.infrastructure.adapters.in.web.dto.request.*;
import com.flash_loan.bank.account_service.infrastructure.adapters.in.web.dto.response.AccountResponseDto;
import com.flash_loan.bank.account_service.infrastructure.adapters.in.web.mapper.AccountWebMapper;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountManagementService accountService;
    private final AccountTransferUseCase accountTransferUseCase;
    private final AccountWebMapper mapper;

    @PostMapping
    public Single<ResponseEntity<AccountResponseDto>> createAccount(@Valid @RequestBody AccountRequestDto request) {
        log.info("Creating new account of type: {}", request.getAccountType());
        return request.accept(new AccountRequestVisitor<Single<Account>>() {
            @Override
            public Single<Account> visit(SavingsAccountRequestDto savings) {
                return accountService.createSavingsAccount(savings.getCustomerId(), savings.getInitialBalance());
            }

            @Override
            public Single<Account> visit(CheckingAccountRequestDto checking) {
                return accountService.createCheckingAccount(checking.getCustomerId(), checking.getInitialBalance());
            }

            @Override
            public Single<Account> visit(FixedTermAccountRequestDto fixedTerm) {
                return accountService.createFixedTermAccount(fixedTerm.getCustomerId(), fixedTerm.getOperatingDay(), fixedTerm.getInitialBalance());
            }
        })
        .map(mapper::toDto)
        .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto));
    }

    @PostMapping("/{id}/debit")
    public Single<ResponseEntity<AccountResponseDto>> debit(@PathVariable String id,
                                                            @Valid @RequestBody AccountBalanceOperationRequestDto request) {
        log.info("Debiting account: {} by amount: {} tx: {}", id, request.getAmount(), request.getTransactionId());
        return accountTransferUseCase.debit(id, request.getAmount(), request.getTransactionId())
                .map(mapper::toDto)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/{id}/credit")
    public Single<ResponseEntity<AccountResponseDto>> credit(@PathVariable String id,
                                                             @Valid @RequestBody AccountBalanceOperationRequestDto request) {
        log.info("Crediting account: {} by amount: {} tx: {}", id, request.getAmount(), request.getTransactionId());
        return accountTransferUseCase.credit(id, request.getAmount(), request.getTransactionId())
                .map(mapper::toDto)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{id}")
    public Single<ResponseEntity<AccountResponseDto>> getAccountById(@PathVariable String id) {
        log.info("Fetching account by id: {}", id);
        return accountService.getAccountById(id)
                .map(mapper::toDto)
                .map(ResponseEntity::ok);
    }

    @GetMapping
    public Flowable<AccountResponseDto> getAllAccounts() {
        log.info("Fetching all accounts");
        return accountService.findAll()
                .map(mapper::toDto);
    }

    @PutMapping("/{id}")
    public Single<ResponseEntity<AccountResponseDto>> updateAccount(@PathVariable String id, @Valid @RequestBody AccountRequestDto request) {
        log.info("Updating account metadata for: {}", id);
        return request.accept(new AccountRequestVisitor<Single<Account>>() {
            @Override
            public Single<Account> visit(SavingsAccountRequestDto savings) {
                return accountService.updateAccount(id, mapper.toDomain(savings));
            }

            @Override
            public Single<Account> visit(CheckingAccountRequestDto checking) {
                return accountService.updateAccount(id, mapper.toDomain(checking));
            }

            @Override
            public Single<Account> visit(FixedTermAccountRequestDto fixedTerm) {
                return accountService.updateAccount(id, mapper.toDomain(fixedTerm));
            }
        })
        .map(mapper::toDto)
        .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Single<ResponseEntity<Void>> deleteAccount(@PathVariable String id) {
        log.info("Deleting account: {}", id);
        return accountService.deleteAccount(id)
                .toSingleDefault(ResponseEntity.status(HttpStatus.NO_CONTENT).<Void>build())
                .onErrorReturn(error -> ResponseEntity.notFound().build());
    }
}