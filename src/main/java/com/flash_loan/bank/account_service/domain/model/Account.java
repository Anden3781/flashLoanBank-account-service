package com.flash_loan.bank.account_service.domain.model;

import com.flash_loan.bank.account_service.domain.model.enums.CustomerType;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public abstract class Account {
    private String id;
    private String customerId;
    private CustomerType customerType; // Replaced String with Domain Enum logically.
    private BigDecimal balance;
    private String currency;
    private LocalDateTime createdAt;
    private AccountType type;
    private Boolean isVip = false;
    private Boolean isPyme = false;
    private String debitCardNumber;
}
