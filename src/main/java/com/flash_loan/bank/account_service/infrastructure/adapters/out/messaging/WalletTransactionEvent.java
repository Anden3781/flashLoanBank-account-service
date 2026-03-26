package com.flash_loan.bank.account_service.infrastructure.adapters.out.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransactionEvent {
    private String transactionId;
    private String fromPhoneNumber;
    private String toPhoneNumber;
    private BigDecimal amount;
    private LocalDateTime timestamp;
    private String status;
    private String fromCardNumber;
    private String toCardNumber;
}
