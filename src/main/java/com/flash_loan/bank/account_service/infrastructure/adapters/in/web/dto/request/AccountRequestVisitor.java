package com.flash_loan.bank.account_service.infrastructure.adapters.in.web.dto.request;

public interface AccountRequestVisitor<T> {
    T visit(SavingsAccountRequestDto request);
    T visit(CheckingAccountRequestDto request);
    T visit(FixedTermAccountRequestDto request);
}
