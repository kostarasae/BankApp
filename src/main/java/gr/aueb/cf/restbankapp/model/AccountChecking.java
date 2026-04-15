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
@DiscriminatorValue("CHECKING")
public class AccountChecking extends Account {

    protected AccountChecking() {}

    private AccountChecking(Builder builder) {
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
        public AccountChecking build() {
            if (super.feeStrategy == null) {
                throw new IllegalStateException("FeeStrategy must be set");
            }
            return new AccountChecking(this);
        }
    }

    @PostLoad
    private void initFeeStrategy() {
        this.feeStrategy = BankConfiguration.getInstance().getDefaultCheckingFeeStrategy();
    }

    @Override
    public boolean violatesRules(BigDecimal balance) {
        return balance.compareTo(
                BankConfiguration.getInstance().getDefaultCheckingOverdraftLimit()
        ) < 0;
    }
}
