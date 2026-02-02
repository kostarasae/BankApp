package chatgpt.core.logging;

import java.math.BigDecimal;

/**
 * Utility class
 */
public final class AccountLogging {

    private AccountLogging() {
        throw new AssertionError("No instances");
    }

    public static void logging(String iban, BigDecimal amount, BigDecimal balance, boolean isDeposit) {
        if (isDeposit) {
            System.out.println("In iban " + iban + " the current balance is " + balance + " after deposit of " + amount);
        } else {
            System.out.println("In iban " + iban + " the current balance is " + balance + " after withdrawal of " + amount);
        }
    }
}
