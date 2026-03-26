package com.flash_loan.bank.account_service.infrastructure.adapters.in.web.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "accountType",
    visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = SavingsAccountRequestDto.class, name = "SAVINGS"),
    @JsonSubTypes.Type(value = CheckingAccountRequestDto.class, name = "CHECKING"),
    @JsonSubTypes.Type(value = FixedTermAccountRequestDto.class, name = "FIXED_TERM")
})
public abstract class AccountRequestDto {
    
    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotBlank(message = "Account Type is required (SAVINGS, CHECKING, FIXED_TERM)")
    private String accountType;
    
    @PositiveOrZero(message = "Initial balance cannot be negative")
    private BigDecimal initialBalance;
    
    public abstract <T> T accept(AccountRequestVisitor<T> visitor);
}