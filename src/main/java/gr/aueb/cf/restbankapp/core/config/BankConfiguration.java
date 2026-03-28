package gr.aueb.cf.restbankapp.core.config;


import gr.aueb.cf.restbankapp.model.FeeStrategy;

import java.math.BigDecimal;

/**
 * Singleton Design Pattern
 */
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

    public String getDefaultCurrency() {
        return defaultCurrency;
    }

    public void setDefaultCurrency(String defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }

    public BigDecimal getDefaultCheckingOverdraftLimit() {
        return defaultCheckingOverdraftLimit;
    }

    public void setDefaultCheckingOverdraftLimit(BigDecimal defaultCheckingOverdraftLimit) {
        this.defaultCheckingOverdraftLimit = defaultCheckingOverdraftLimit;
    }

    public BigDecimal getDefaultSavingsOverdraftLimit() {
        return defaultSavingsOverdraftLimit;
    }

    public void setDefaultSavingsOverdraftLimit(BigDecimal defaultSavingsOverdraftLimit) {
        this.defaultSavingsOverdraftLimit = defaultSavingsOverdraftLimit;
    }

    public FeeStrategy getDefaultCheckingFeeStrategy() {
        return defaultCheckingFeeStrategy;
    }

    public FeeStrategy getDefaultSavingsFeeStrategy() {
        return defaultSavingsFeeStrategy;
    }

    public void setDefaultCheckingFeeStrategy(FeeStrategy defaultCheckingFeeStrategy) {
        this.defaultCheckingFeeStrategy = defaultCheckingFeeStrategy;
    }

    public void setDefaultSavingsFeeStrategy(FeeStrategy defaultSavingsFeeStrategy) {
        this.defaultSavingsFeeStrategy = defaultSavingsFeeStrategy;
    }

    public BigDecimal getDefaultCheckingFee() {
        return defaultCheckingFee;
    }

    public BigDecimal getDefaultSavingsFee() {
        return defaultSavingsFee;
    }
}
