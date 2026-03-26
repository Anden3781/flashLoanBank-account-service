package com.flash_loan.bank.account_service.domain.ports.in;

import com.flash_loan.bank.account_service.domain.model.Account;
import io.reactivex.rxjava3.core.Single;

import java.math.BigDecimal;

public interface AccountTransferUseCase {

    Single<Account> debit(String accountId, BigDecimal amount, String transactionId);

    Single<Account> credit(String accountId, BigDecimal amount, String transactionId);
}
