package com.flash_loan.bank.account_service.infrastructure.adapters.in.web.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FixedTermAccountRequestDto extends AccountRequestDto {
    
    @NotNull(message = "Operating day is required for Fixed Term accounts")
    @Min(value = 1, message = "Operating day must be between 1 and 31")
    @Max(value = 31, message = "Operating day must be between 1 and 31")
    private Integer operatingDay;
    
    @Override
    public <T> T accept(AccountRequestVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
