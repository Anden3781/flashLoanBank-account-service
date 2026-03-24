package com.flash_loan.bank.account_service.domain.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class CheckingAccount extends Account {
    
    private BigDecimal monthlyMaintenanceFee;
    
    public CheckingAccount() {
        this.setType(AccountType.CHECKING);
        this.monthlyMaintenanceFee = new BigDecimal("15.00"); // Comisión mensual
    }
}
