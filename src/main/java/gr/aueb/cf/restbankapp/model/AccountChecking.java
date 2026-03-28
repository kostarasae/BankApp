package gr.aueb.cf.restbankapp.model;

import gr.aueb.cf.restbankapp.core.config.BankConfiguration;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Concrete subclass
 * extends abstract class
 */
public class AccountChecking extends Account {

    private AccountChecking(Builder builder) {
        super(builder);
    }

    public static class Builder extends Account.Builder<Builder> {

        public Builder(String accountNumber, String iban, BigDecimal balance, Instant createdAt) {
            super(accountNumber, iban, balance, createdAt);
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public AccountChecking build() {
            if (super.feeStrategy == null) {
                throw new IllegalStateException("FeeStrategy must be set");
            }
            return new AccountChecking(this);
        }
    }

    @Override
    public boolean violatesRules(BigDecimal balance) {
        return balance.compareTo(
                BankConfiguration.getInstance().getDefaultCheckingOverdraftLimit()
        ) < 0;
    }
}
