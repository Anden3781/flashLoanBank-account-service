package com.flash_loan.bank.account_service.infrastructure.adapters.out.persistence.entity;

import com.flash_loan.bank.account_service.domain.model.AccountType;
import com.flash_loan.bank.account_service.domain.model.enums.CustomerType;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Document(collection = "accounts")
public abstract class AccountEntity {
    @Id
    private String id;
    private String customerId;
    private CustomerType customerType;
    private BigDecimal balance;
    private String currency;
    private LocalDateTime createdAt;
    private AccountType type;
    private Boolean isVip = false;
    private Boolean isPyme = false;
    private String debitCardNumber;
}
