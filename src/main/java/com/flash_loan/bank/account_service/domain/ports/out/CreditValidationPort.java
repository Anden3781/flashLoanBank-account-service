package com.flash_loan.bank.account_service.domain.ports.out;

import io.reactivex.rxjava3.core.Single;

public interface CreditValidationPort {
    Single<Boolean> hasOverdueDebt(String customerId);
}
