package gr.aueb.cf.restbankapp.core.factory;

import gr.aueb.cf.restbankapp.core.config.BankConfiguration;
import gr.aueb.cf.restbankapp.model.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Factory Design Pattern
 */
public class AccountFactory {

    private AccountFactory() {}

    public static Account create(AccountType type, BigDecimal balance) {

        // Generate account number and iban internally via a counter
        String accountNumber = Generator.generateAccountNumber();
        String iban = Generator.generateIban(accountNumber);
        Instant createdAt = Instant.now();
        BankConfiguration config = BankConfiguration.getInstance();

        // Build and return the requested account type
        return switch (type) {
            case CHECKING ->
                    new AccountChecking.Builder(accountNumber, iban, balance, createdAt)
                            .feeStrategy(config.getDefaultCheckingFeeStrategy())
                            .build();

            case SAVINGS ->
                    new AccountSavings.Builder(accountNumber, iban, balance, createdAt)
                            .feeStrategy(config.getDefaultSavingsFeeStrategy())
                            .build();
        };
    }
}
