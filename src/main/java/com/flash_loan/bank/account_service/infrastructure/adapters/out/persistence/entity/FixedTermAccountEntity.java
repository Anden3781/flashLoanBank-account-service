package com.flash_loan.bank.account_service.infrastructure.adapters.out.persistence.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.TypeAlias;

@Data
@EqualsAndHashCode(callSuper = true)
@TypeAlias("FIXED_TERM")
public class FixedTermAccountEntity extends AccountEntity {
    private Integer allowedTransactionDay;
}
