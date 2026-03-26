package com.flash_loan.bank.account_service.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.core.convert.converter.Converter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class MongoConfig {

    @Bean
    public MongoCustomConversions customConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(new BigDecimalToDecimal128Converter());
        converters.add(new Decimal128ToBigDecimalConverter());
        return new MongoCustomConversions(converters);
    }

    public static class BigDecimalToDecimal128Converter implements Converter<BigDecimal, org.bson.types.Decimal128> {
        @Override
        public org.bson.types.Decimal128 convert(BigDecimal source) {
            return new org.bson.types.Decimal128(source);
        }
    }

    public static class Decimal128ToBigDecimalConverter implements Converter<org.bson.types.Decimal128, BigDecimal> {
        @Override
        public BigDecimal convert(org.bson.types.Decimal128 source) {
            return source.bigDecimalValue();
        }
    }
}
