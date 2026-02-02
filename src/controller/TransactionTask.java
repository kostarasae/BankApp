package chatgpt.controller;

import chatgpt.core.exceptions.AccountNotFoundException;
import chatgpt.core.exceptions.InsufficientBalanceException;
import chatgpt.core.exceptions.ValidationException;

import java.math.BigDecimal;

public class TransactionTask implements Runnable {

    private final AccountController controller;
    private final String iban;
    private final BigDecimal amount;
    private final boolean deposit;

    public TransactionTask(AccountController controller, String iban, BigDecimal amount, boolean deposit) {
        this.controller = controller;
        this.iban = iban;
        this.amount = amount;
        this.deposit = deposit;
    }

    @Override
    public void run() {
        try {
            if (deposit) {
                controller.deposit(iban, amount);
            } else {
                controller.withdraw(iban, amount);
            }
        } catch (ValidationException | AccountNotFoundException | InsufficientBalanceException e) {
            //System.err.println(Thread.currentThread().getName() + ": Transaction failed: " + e.getMessage());
            System.err.println("Transaction failed: " + e.getMessage());
        }
    }
}
