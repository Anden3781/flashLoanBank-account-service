package com.flash_loan.bank.account_service.domain.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FixedTermAccount extends Account {
    
    private Integer allowedTransactionDay;
    
    public FixedTermAccount() {
        this.setType(AccountType.FIXED_TERM);
    }
}
