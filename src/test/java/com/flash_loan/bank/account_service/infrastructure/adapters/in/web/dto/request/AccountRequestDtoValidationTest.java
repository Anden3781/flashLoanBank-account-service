package com.flash_loan.bank.account_service.infrastructure.adapters.in.web.dto.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class AccountRequestDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void savingsAccountRequest_MissingFields_ShouldHaveViolations() {
        SavingsAccountRequestDto dto = new SavingsAccountRequestDto();
        var violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void fixedTermAccountRequest_InvalidOperatingDay_ShouldHaveViolations() {
        FixedTermAccountRequestDto dto = new FixedTermAccountRequestDto();
        dto.setCustomerId("cust-1");
        dto.setAccountType("FIXED_TERM");
        dto.setInitialBalance(BigDecimal.ZERO);
        dto.setOperatingDay(0);

        var violations = validator.validate(dto);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("operatingDay"));
    }

    @Test
    void accountBalanceOperationRequest_InvalidAmountAndBlankTx_ShouldHaveViolations() {
        AccountBalanceOperationRequestDto dto = new AccountBalanceOperationRequestDto();
        dto.setAmount(BigDecimal.ZERO);
        dto.setTransactionId("");

        var violations = validator.validate(dto);
        assertThat(violations).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void accept_ShouldDispatchToCorrectVisitorMethod() {
        AccountRequestVisitor<String> visitor = new AccountRequestVisitor<>() {
            @Override
            public String visit(SavingsAccountRequestDto request) {
                return "SAVINGS";
            }

            @Override
            public String visit(CheckingAccountRequestDto request) {
                return "CHECKING";
            }

            @Override
            public String visit(FixedTermAccountRequestDto request) {
                return "FIXED_TERM";
            }
        };

        assertThat(new SavingsAccountRequestDto().accept(visitor)).isEqualTo("SAVINGS");
        assertThat(new CheckingAccountRequestDto().accept(visitor)).isEqualTo("CHECKING");
        assertThat(new FixedTermAccountRequestDto().accept(visitor)).isEqualTo("FIXED_TERM");
    }
}
