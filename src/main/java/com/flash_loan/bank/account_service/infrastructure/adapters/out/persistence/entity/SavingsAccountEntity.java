package com.flash_loan.bank.account_service.infrastructure.adapters.out.persistence.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.TypeAlias;

@Data
@EqualsAndHashCode(callSuper = true)
@TypeAlias("SAVINGS")
public class SavingsAccountEntity extends AccountEntity {
    private Integer maxMonthlyMovements;
    private Integer currentMovements;
}
