package chatgpt;

import chatgpt.controller.AccountController;
import chatgpt.controller.TransactionTask;
import chatgpt.dao.AccountDAOImpl;
import chatgpt.dto.AccountReadOnlyDTO;
import chatgpt.model.AccountType;
import chatgpt.service.AccountServiceImpl;
import chatgpt.service.IAccountService;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        // Infrastructure
        AccountDAOImpl dao = new AccountDAOImpl();
        IAccountService service = new AccountServiceImpl(dao);
        AccountController controller = new AccountController(service);

        // Create accounts
        AccountReadOnlyDTO checkingAccount = controller.createNewAccount(AccountType.CHECKING, BigDecimal.ZERO);
        AccountReadOnlyDTO savingsAccount = controller.createNewAccount(AccountType.SAVINGS, BigDecimal.ZERO);
        System.out.println("Created accounts:");
        System.out.println(checkingAccount);
        System.out.println(savingsAccount);

        // Executor for concurrent transactions
        ExecutorService executor = Executors.newFixedThreadPool(4);
        executor.execute(new TransactionTask(controller, checkingAccount.iban(), BigDecimal.valueOf(200), true));
        executor.execute(new TransactionTask(controller, checkingAccount.iban(), BigDecimal.valueOf(100), false));
        executor.execute(new TransactionTask(controller, savingsAccount.iban(), BigDecimal.valueOf(300), true));
        executor.execute(new TransactionTask(controller, savingsAccount.iban(), BigDecimal.valueOf(400), false));
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // Final balances
        System.out.println("\nFinal balances:");
        System.out.println("Checking: " + controller.getBalance(checkingAccount.iban()));
        System.out.println("Savings: " + controller.getBalance(savingsAccount.iban()));

        // Report
        System.out.println("\nSavings report: ");
        System.out.println(controller.generateReport(savingsAccount.iban()));
//        try {
//            System.out.println(controller.generateReport(checkingAccount.iban()));
//        } catch (AccountNotReportableException | ValidationException e) {
//            System.err.println(e.getMessage());
//        }
    }
}
