package com.flash_loan.bank.account_service.domain.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SavingsAccount extends Account {
    
    private Integer maxMonthlyMovements;
    private Integer currentMovements;
    
    public SavingsAccount() {
        this.setType(AccountType.SAVINGS);
        this.maxMonthlyMovements = 5; // Limite de movimientos sin comisión
        this.currentMovements = 0;
    }
}
