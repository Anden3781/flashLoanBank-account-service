package com.flash_loan.bank.account_service;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.mockito.Mockito.mock;

class AccountServiceApplicationMainTest {

    @Test
    void main_ShouldInvokeSpringApplicationRun() {
        ConfigurableApplicationContext context = mock(ConfigurableApplicationContext.class);
        try (MockedStatic<SpringApplication> mocked = Mockito.mockStatic(SpringApplication.class)) {
            mocked.when(() -> SpringApplication.run(AccountServiceApplication.class, new String[]{}))
                    .thenReturn(context);

            AccountServiceApplication.main(new String[]{});

            mocked.verify(() -> SpringApplication.run(AccountServiceApplication.class, new String[]{}));
        }
    }
}
