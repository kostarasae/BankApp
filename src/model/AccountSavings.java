package chatgpt.model;

import chatgpt.core.config.BankConfiguration;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Concrete subclass
 * extends abstract class
 */
public class AccountSavings extends Account implements Reportable {

    public AccountSavings(Builder builder) {
        super(builder);
    }

    @Override
    public boolean violatesRules(BigDecimal balance) {
        return balance.compareTo(BankConfiguration.getInstance().getDefaultSavingsOverdraftLimit()) < 0;
    }

    public static class Builder extends Account.Builder<Builder> {

        public Builder(String accountNumber, String iban, BigDecimal balance, Instant createdAt) {
            super(accountNumber, iban, balance, createdAt);
        }

        @Override
        public AccountSavings build() {
            return new AccountSavings(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
