package chatgpt.controller;

import chatgpt.core.exceptions.AccountNotFoundException;
import chatgpt.core.exceptions.InsufficientBalanceException;
import chatgpt.core.exceptions.ValidationException;
import chatgpt.dto.AccountDepositDTO;
import chatgpt.dto.AccountInsertDTO;
import chatgpt.dto.AccountReadOnlyDTO;
import chatgpt.dto.AccountWithdrawDTO;
import chatgpt.model.AccountType;
import chatgpt.service.IAccountService;
import chatgpt.validation.Validator;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Facade Design Pattern (simplifies access to subsystems)
 */
public class AccountController {

    private final IAccountService accountService;

    public AccountController(IAccountService accountService) {
        this.accountService = accountService;
    }

    public AccountReadOnlyDTO createNewAccount(AccountType type, BigDecimal balance) {

        // Create insert DTO
        AccountInsertDTO insertDTO = new AccountInsertDTO(type, balance);

        // Validate insert DTO
        Map<String, String> errors = Validator.validateInsertDTO(insertDTO);
        if (!errors.isEmpty()) { throw new ValidationException(errors.toString()); }

        // Proceed to service and return read DTO
        AccountReadOnlyDTO readOnlyDTO;
        readOnlyDTO = accountService.createNewAccount(insertDTO);
        return readOnlyDTO;
    }


    public void deposit(String iban, BigDecimal amount) {

        AccountDepositDTO depositDTO = new AccountDepositDTO(iban, amount);

        Map<String, String> errors;
        errors = Validator.validateDepositDTO(depositDTO);
        if (!errors.isEmpty()) { throw new ValidationException(errors.toString()); }

        accountService.deposit(depositDTO);
    }


    public void withdraw(String iban, BigDecimal amount)  {
        AccountWithdrawDTO withdrawDTO = new AccountWithdrawDTO(iban, amount);

        Map<String, String> errors;
        errors = Validator.validateWithdrawDTO(withdrawDTO);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors.toString());
        }

        accountService.withdraw(withdrawDTO);
    }


    public void closeAccount(String iban) {

        Map<String, String> errors;
        errors = Validator.validateIban(iban);;
        if (!errors.isEmpty()) {
            throw new ValidationException(errors.toString());
        }

        accountService.closeAccount(iban);
    }


    public BigDecimal getBalance(String iban) {

        Map<String, String> errors;
        errors = Validator.validateIban(iban);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors.toString());
        }

        return accountService.getBalance(iban);
    }


    public List<AccountReadOnlyDTO> getAllAccounts() {
        return accountService.getAllAccounts();
    }


    public String generateReport(String iban) {

        Map<String, String> errors;
        errors = Validator.validateIban(iban);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors.toString());
        }

        return accountService.generateReport(iban);
    }
}
