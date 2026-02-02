package chatgpt.model;

import chatgpt.core.config.BankConfiguration;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Concrete subclass
 * extends abstract class
 */
public class AccountChecking extends Account {

    public AccountChecking(Builder builder) {
        super(builder);
    }

    @Override
    public boolean violatesRules(BigDecimal balance) {
        return balance.compareTo(BankConfiguration.getInstance().getDefaultCheckingOverdraftLimit()) < 0;
    }

    public static class Builder extends Account.Builder<Builder> {

        public Builder(String accountNumber, String iban, BigDecimal balance, Instant createdAt) {
            super(accountNumber, iban, balance, createdAt);
        }

        @Override
        public AccountChecking build() {
            return new AccountChecking(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
