package com.flash_loan.bank.account_service.infrastructure.adapters.out;

import com.flash_loan.bank.account_service.domain.model.enums.CustomerType;
import com.flash_loan.bank.account_service.domain.ports.out.CustomerValidationPort;
import io.reactivex.rxjava3.core.Maybe;
import org.springframework.stereotype.Component;

@Component
public class MockCustomerValidationAdapter implements CustomerValidationPort {

    @Override
    public Maybe<CustomerType> getCustomerType(String customerId) {
        if ("personal-client".equalsIgnoreCase(customerId)) {
            return Maybe.just(CustomerType.PERSONAL);
        } else if ("business-client".equalsIgnoreCase(customerId)) {
            return Maybe.just(CustomerType.BUSINESS);
        }
        return Maybe.empty();
    }
}
