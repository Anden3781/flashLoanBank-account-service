package com.flash_loan.bank.account_service.application.service;

import com.flash_loan.bank.account_service.domain.exception.BusinessRuleException;
import com.flash_loan.bank.account_service.domain.model.Account;
import com.flash_loan.bank.account_service.domain.model.AccountType;
import com.flash_loan.bank.account_service.domain.model.CheckingAccount;
import com.flash_loan.bank.account_service.domain.model.enums.CustomerType;
import com.flash_loan.bank.account_service.domain.ports.out.AccountRepositoryPort;
import com.flash_loan.bank.account_service.domain.ports.out.CardValidationPort;
import com.flash_loan.bank.account_service.domain.ports.out.CreditValidationPort;
import com.flash_loan.bank.account_service.domain.ports.out.CustomerValidationPort;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountManagementServiceTest {

    @Mock
    private CustomerValidationPort customerPort;
    @Mock
    private AccountRepositoryPort accountPort;
    @Mock
    private CreditValidationPort creditPort;
    @Mock
    private CardValidationPort cardPort;

    @InjectMocks
    private AccountManagementService accountService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void createCheckingAccount_BusinessWithCard_ShouldBePymeAndZeroFee() {
        // Arrange
        String customerId = "cust-123";
        when(creditPort.hasOverdueDebt(customerId)).thenReturn(Single.just(false));
        when(customerPort.getCustomerType(customerId)).thenReturn(Maybe.just(CustomerType.BUSINESS));
        when(cardPort.hasCreditCard(customerId)).thenReturn(Single.just(true));
        when(accountPort.save(any(Account.class))).thenAnswer(i -> Single.just(i.getArgument(0)));

        // Act
        Account result = accountService.createCheckingAccount(customerId, BigDecimal.ZERO).blockingGet();

        // Assert
        assertTrue(result instanceof CheckingAccount);
        CheckingAccount checking = (CheckingAccount) result;
        assertTrue(checking.getIsPyme());
        assertEquals(BigDecimal.ZERO, checking.getMonthlyMaintenanceFee());
        verify(accountPort).save(any(Account.class));
    }

    @Test
    void createCheckingAccount_BusinessWithoutCard_ShouldNotBePymeAndHaveFee() {
        // Arrange
        String customerId = "cust-123";
        when(creditPort.hasOverdueDebt(customerId)).thenReturn(Single.just(false));
        when(customerPort.getCustomerType(customerId)).thenReturn(Maybe.just(CustomerType.BUSINESS));
        when(cardPort.hasCreditCard(customerId)).thenReturn(Single.just(false));
        when(accountPort.save(any(Account.class))).thenAnswer(i -> Single.just(i.getArgument(0)));

        // Act
        Account result = accountService.createCheckingAccount(customerId, BigDecimal.ZERO).blockingGet();

        // Assert
        assertTrue(result instanceof CheckingAccount);
        CheckingAccount checking = (CheckingAccount) result;
        assertFalse(checking.getIsPyme());
        assertEquals(new BigDecimal("15.00"), checking.getMonthlyMaintenanceFee());
    }

    @Test
    void createCheckingAccount_WithOverdueDebt_ShouldThrowException() {
        // Arrange
        String customerId = "cust-123";
        when(creditPort.hasOverdueDebt(customerId)).thenReturn(Single.just(true));

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> 
            accountService.createCheckingAccount(customerId, BigDecimal.ZERO).blockingGet()
        );
        verify(accountPort, never()).save(any());
    }
}
