package com.flash_loan.bank.account_service.infrastructure.adapters.in.web.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AccountResponseDto {
    private String id;
    private String customerId;
    private String customerType;
    private BigDecimal balance;
    private String currency;
    private LocalDateTime createdAt;
    private String type;
    
    private Integer maxMonthlyMovements;
    private Integer currentMovements;
    private BigDecimal monthlyMaintenanceFee;
    private Boolean isVip;
    private Boolean isPyme;
    private Integer allowedTransactionDay;
}
