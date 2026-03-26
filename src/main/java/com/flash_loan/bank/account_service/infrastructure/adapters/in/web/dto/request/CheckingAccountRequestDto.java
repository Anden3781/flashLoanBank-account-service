package com.flash_loan.bank.account_service.infrastructure.adapters.in.web.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CheckingAccountRequestDto extends AccountRequestDto {
    @Override
    public <T> T accept(AccountRequestVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
