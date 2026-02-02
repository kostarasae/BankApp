package chatgpt.model;

import chatgpt.core.config.BankConfiguration;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Abstract Class / Skeletal Implementation / Builder Design Pattern
 */
public abstract class Account {

    private final String accountNumber;
    private final String iban;
    private String currency;
    private BigDecimal balance;
    private final Instant createdAt;
    private FeeStrategy feeStrategy;

    // constructor with builder parameter
    protected Account(Builder<?> builder) {
        this.accountNumber = builder.accountNumber;
        this.iban = builder.iban;
        this.currency = builder.currency;
        this.balance = builder.balance;
        this.createdAt = builder.createdAt;
        this.feeStrategy = builder.feeStrategy;
    }

    // using builder class to avoid telescoping
    public static abstract class Builder<T extends Builder<T>> {

        // get defaults
        BankConfiguration config = BankConfiguration.getInstance();

        // fields
        private final String accountNumber;
        private final String iban;
        private final BigDecimal balance;
        private final Instant createdAt;
        private String currency = config.getDefaultCurrency();
        private FeeStrategy feeStrategy;

        // constructor
        protected Builder(String accountNumber, String iban, BigDecimal balance, Instant createdAt) {
            this.accountNumber = accountNumber;
            this.iban = iban;
            this.balance = balance;
            this.createdAt = createdAt;
        }

        // fluent setters (allow chain call initialisations)
        public T currency(String currency) {
            this.currency = currency;
            return self();
        }
        public T feeStrategy(FeeStrategy feeStrategy) {
            this.feeStrategy = feeStrategy;
            return self();
        }

        // build method
        public abstract Account build();

        // fluent generics (to return AccountType)
        protected abstract T self();
    }

    // Template Method / Hook
    public abstract boolean violatesRules(BigDecimal balance);

    // getters
    public String getAccountNumber() {
        return accountNumber;
    }

    public String getIban() {
        return iban;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getCurrency() {
        return currency;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public FeeStrategy getFeeStrategy() {
        return feeStrategy;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setFeeStrategy(FeeStrategy feeStrategy) {
        this.feeStrategy = feeStrategy;
    }

    // concrete methods (full implementation, opposite of abstract)
    // override concrete methods
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Account account)) return false;
        return Objects.equals(this.iban, account.iban);
    }

    @Override
    public int hashCode() {
        return this.iban.hashCode();
    }

    @Override
    public String toString() {
        String feeStrategyInfo = "none";
        if (feeStrategy != null) {
            String value = feeStrategy.value();
            feeStrategyInfo = (value == null || value.isBlank())
                    ? feeStrategy.getClass().getSimpleName()
                    : value;
        }

        return "Account number " + this.accountNumber + " with iban " + this.iban + " has balance of " + this.balance
                + " in currency of " + this.currency + " and fee strategy " + feeStrategyInfo + " created at " + this.createdAt;
    }
}