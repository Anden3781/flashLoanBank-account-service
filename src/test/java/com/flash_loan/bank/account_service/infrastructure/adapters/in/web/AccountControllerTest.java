package com.flash_loan.bank.account_service.infrastructure.adapters.in.web;

import com.flash_loan.bank.account_service.application.service.AccountManagementService;
import com.flash_loan.bank.account_service.domain.model.Account;
import com.flash_loan.bank.account_service.domain.model.SavingsAccount;
import com.flash_loan.bank.account_service.domain.ports.in.AccountTransferUseCase;
import com.flash_loan.bank.account_service.infrastructure.adapters.in.web.dto.request.AccountBalanceOperationRequestDto;
import com.flash_loan.bank.account_service.infrastructure.adapters.in.web.dto.request.CheckingAccountRequestDto;
import com.flash_loan.bank.account_service.infrastructure.adapters.in.web.dto.request.FixedTermAccountRequestDto;
import com.flash_loan.bank.account_service.infrastructure.adapters.in.web.dto.request.SavingsAccountRequestDto;
import com.flash_loan.bank.account_service.infrastructure.adapters.in.web.dto.response.AccountResponseDto;
import com.flash_loan.bank.account_service.infrastructure.adapters.in.web.mapper.AccountWebMapper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    private AccountManagementService accountService;
    @Mock
    private AccountTransferUseCase accountTransferUseCase;
    @Mock
    private AccountWebMapper mapper;

    @InjectMocks
    private AccountController controller;

    private Account account;
    private AccountResponseDto responseDto;

    @BeforeEach
    void setUp() {
        account = new SavingsAccount();
        account.setId("acc-1");
        account.setCustomerId("cust-1");
        account.setBalance(new BigDecimal("100"));

        responseDto = new AccountResponseDto();
        responseDto.setId("acc-1");
        responseDto.setCustomerId("cust-1");
        responseDto.setBalance(new BigDecimal("100"));
        responseDto.setType("SAVINGS");
    }

    @Test
    void createAccount_Savings_ShouldReturnCreated() {
        SavingsAccountRequestDto request = new SavingsAccountRequestDto();
        request.setCustomerId("cust-1");
        request.setAccountType("SAVINGS");
        request.setInitialBalance(new BigDecimal("100"));

        when(accountService.createSavingsAccount(anyString(), any())).thenReturn(Single.just(account));
        when(mapper.toDto(any(Account.class))).thenReturn(responseDto);

        ResponseEntity<AccountResponseDto> response = controller.createAccount(request).blockingGet();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(responseDto);
    }

    @Test
    void createAccount_Checking_ShouldReturnCreated() {
        CheckingAccountRequestDto request = new CheckingAccountRequestDto();
        request.setCustomerId("cust-1");
        request.setAccountType("CHECKING");
        request.setInitialBalance(BigDecimal.ZERO);

        when(accountService.createCheckingAccount(anyString(), any())).thenReturn(Single.just(account));
        when(mapper.toDto(any(Account.class))).thenReturn(responseDto);

        ResponseEntity<AccountResponseDto> response = controller.createAccount(request).blockingGet();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void createAccount_FixedTerm_ShouldReturnCreated() {
        FixedTermAccountRequestDto request = new FixedTermAccountRequestDto();
        request.setCustomerId("cust-1");
        request.setAccountType("FIXED_TERM");
        request.setOperatingDay(10);
        request.setInitialBalance(new BigDecimal("50"));

        when(accountService.createFixedTermAccount(anyString(), any(), any())).thenReturn(Single.just(account));
        when(mapper.toDto(any(Account.class))).thenReturn(responseDto);

        ResponseEntity<AccountResponseDto> response = controller.createAccount(request).blockingGet();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void debit_ShouldReturnOk() {
        AccountBalanceOperationRequestDto request = new AccountBalanceOperationRequestDto();
        request.setAmount(new BigDecimal("10"));
        request.setTransactionId("tx-1");

        when(accountTransferUseCase.debit(anyString(), any(), anyString())).thenReturn(Single.just(account));
        when(mapper.toDto(any(Account.class))).thenReturn(responseDto);

        ResponseEntity<AccountResponseDto> response = controller.debit("acc-1", request).blockingGet();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void credit_ShouldReturnOk() {
        AccountBalanceOperationRequestDto request = new AccountBalanceOperationRequestDto();
        request.setAmount(new BigDecimal("10"));
        request.setTransactionId("tx-1");

        when(accountTransferUseCase.credit(anyString(), any(), anyString())).thenReturn(Single.just(account));
        when(mapper.toDto(any(Account.class))).thenReturn(responseDto);

        ResponseEntity<AccountResponseDto> response = controller.credit("acc-1", request).blockingGet();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getAccountById_ShouldReturnOk() {
        when(accountService.getAccountById(anyString())).thenReturn(Single.just(account));
        when(mapper.toDto(any(Account.class))).thenReturn(responseDto);

        ResponseEntity<AccountResponseDto> response = controller.getAccountById("acc-1").blockingGet();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(responseDto);
    }

    @Test
    void getAllAccounts_ShouldReturnFlowable() {
        when(accountService.findAll()).thenReturn(Flowable.just(account));
        when(mapper.toDto(any(Account.class))).thenReturn(responseDto);

        assertThat(controller.getAllAccounts().toList().blockingGet()).hasSize(1);
    }

    @Test
    void getAccountsByCustomerId_ShouldReturnFlowable() {
        when(accountService.findByCustomerId(anyString())).thenReturn(Flowable.just(account));
        when(mapper.toDto(any(Account.class))).thenReturn(responseDto);

        assertThat(controller.getAccountsByCustomerId("cust-1").toList().blockingGet()).hasSize(1);
    }

    @Test
    void updateAccount_Savings_ShouldReturnOk() {
        SavingsAccountRequestDto request = new SavingsAccountRequestDto();
        request.setCustomerId("cust-1");
        request.setAccountType("SAVINGS");
        request.setInitialBalance(new BigDecimal("100"));

        SavingsAccount mappedDomain = new SavingsAccount();
        mappedDomain.setCustomerId("cust-1");
        mappedDomain.setBalance(new BigDecimal("100"));

        when(mapper.toDomain(any(SavingsAccountRequestDto.class))).thenReturn(mappedDomain);
        when(accountService.updateAccount(anyString(), any(Account.class))).thenReturn(Single.just(account));
        when(mapper.toDto(any(Account.class))).thenReturn(responseDto);

        ResponseEntity<AccountResponseDto> response = controller.updateAccount("acc-1", request).blockingGet();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void deleteAccount_Success_ShouldReturnNoContent() {
        when(accountService.deleteAccount(anyString())).thenReturn(Completable.complete());

        ResponseEntity<Void> response = controller.deleteAccount("acc-1").blockingGet();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void deleteAccount_Error_ShouldReturnNotFound() {
        when(accountService.deleteAccount(anyString())).thenReturn(Completable.error(new RuntimeException("not found")));

        ResponseEntity<Void> response = controller.deleteAccount("acc-1").blockingGet();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
