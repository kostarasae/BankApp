package gr.aueb.cf.restbankapp.config;


import gr.aueb.cf.restbankapp.model.FeeStrategy;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Singleton Design Pattern
 */
@Getter
@Setter
public final class BankConfiguration {

    private String defaultCurrency;
    private BigDecimal defaultCheckingOverdraftLimit;
    private BigDecimal defaultSavingsOverdraftLimit;
    private FeeStrategy defaultCheckingFeeStrategy;
    private FeeStrategy defaultSavingsFeeStrategy;

    private final BigDecimal defaultCheckingFee = BigDecimal.valueOf(0.5);
    private final BigDecimal defaultSavingsFee = BigDecimal.valueOf(1.0);

    private BankConfiguration() {
        loadDefaults ();
    }

    private void loadDefaults () {
        defaultCurrency = "EURO";
        defaultCheckingOverdraftLimit = BigDecimal.valueOf(-500);
        defaultSavingsOverdraftLimit = BigDecimal.ZERO;
        defaultCheckingFeeStrategy = new FeeStrategy() {
            @Override
            public BigDecimal calculateFee(BigDecimal amount) {
                return amount.add(defaultCheckingFee);
            }

            @Override
            public String value() {
                return defaultCheckingFee.toPlainString();
            }
        };

        defaultSavingsFeeStrategy = new FeeStrategy() {
            @Override
            public BigDecimal calculateFee(BigDecimal amount) {
                return amount.add(defaultSavingsFee);
            }

            @Override
            public String value() {
                return defaultSavingsFee.toPlainString();
            }
        };
    }

    private static final BankConfiguration INSTANCE  = new BankConfiguration();

    public static BankConfiguration getInstance() {
        return INSTANCE;
    }
}
