package gr.aueb.cf.restbankapp.core.factory;

import gr.aueb.cf.restbankapp.config.BankConfiguration;
import gr.aueb.cf.restbankapp.model.*;

import java.math.BigDecimal;

/**
 * Factory Design Pattern
 */
public class AccountFactory {

    private AccountFactory() {}

    public static Account create(AccountType type, BigDecimal balance, String customerUuid) {

        // Generate account number and iban internally via a counter
        String accountNumber = Generator.generateAccountNumber();
        String iban = Generator.generateIban(accountNumber);
        BankConfiguration config = BankConfiguration.getInstance();

        // Build and return the requested account type
        return switch (type) {
            case CHECKING ->
                    new AccountChecking.Builder(accountNumber, iban, balance, customerUuid)
                            .feeStrategy(config.getDefaultCheckingFeeStrategy())
                            .build();

            case SAVINGS ->
                    new AccountSavings.Builder(accountNumber, iban, balance, customerUuid)
                            .feeStrategy(config.getDefaultSavingsFeeStrategy())
                            .build();
        };
    }
}
