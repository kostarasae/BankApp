package gr.aueb.cf.restbankapp.model;

import java.math.BigDecimal;

/**
 * Functional Interface / Strategy Design Pattern
 */
@FunctionalInterface
public interface FeeStrategy {
    BigDecimal calculateFee(BigDecimal amount);

    default String value() {
        return "";
    }
}
