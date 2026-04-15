package gr.aueb.cf.restbankapp.model;

import gr.aueb.cf.restbankapp.config.BankConfiguration;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PostLoad;

import java.math.BigDecimal;

/**
 * Concrete subclass
 * extends abstract class
 */
@Entity
@DiscriminatorValue("SAVINGS")
public class AccountSavings extends Account {

    protected AccountSavings() {}

    private AccountSavings(Builder builder) {
        super(builder);
    }

    public static class Builder extends Account.Builder<Builder> {

        public Builder(String accountNumber, String iban, BigDecimal balance, String customerUuid) {
            super(accountNumber, iban, balance, customerUuid);
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public AccountSavings build() {
            if (super.feeStrategy == null) {
                throw new IllegalStateException("FeeStrategy must be set");
            }
            return new AccountSavings(this);
        }
    }

    @PostLoad
    private void initFeeStrategy() {
        this.feeStrategy = BankConfiguration.getInstance().getDefaultSavingsFeeStrategy();
    }

    @Override
    public boolean violatesRules(BigDecimal balance) {
        return balance.compareTo(
                BankConfiguration.getInstance().getDefaultSavingsOverdraftLimit()
        ) < 0;
    }
}
