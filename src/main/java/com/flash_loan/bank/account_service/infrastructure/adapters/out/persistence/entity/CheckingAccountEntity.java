package com.flash_loan.bank.account_service.infrastructure.adapters.out.persistence.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.TypeAlias;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TypeAlias("CHECKING")
public class CheckingAccountEntity extends AccountEntity {
    private BigDecimal monthlyMaintenanceFee;
}
