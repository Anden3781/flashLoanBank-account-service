package com.flash_loan.bank.account_service.domain.ports.out;

import com.flash_loan.bank.account_service.domain.model.enums.CustomerType;
import io.reactivex.rxjava3.core.Maybe;

/**
 * Port in the Hexagonal Architecture. 
 * Allows the Domain/Application layer to fetch Customer information 
 * (like verification and customerType) without coupling to WebClient implementations.
 */
public interface CustomerValidationPort {
    Maybe<CustomerType> getCustomerType(String customerId);
}
