package com.flash_loan.bank.account_service.infrastructure.adapters.out.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerResponseDto {
    private String customerType;
}
