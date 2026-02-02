package chatgpt.service;

import chatgpt.dto.AccountDepositDTO;
import chatgpt.dto.AccountInsertDTO;
import chatgpt.dto.AccountReadOnlyDTO;
import chatgpt.dto.AccountWithdrawDTO;

import java.math.BigDecimal;
import java.util.List;

/**
 * Contract
 */
public interface IAccountService {
    AccountReadOnlyDTO createNewAccount(AccountInsertDTO insertDTO);
    void deposit(AccountDepositDTO depositDTO);
    void withdraw(AccountWithdrawDTO withdrawDTO);
    BigDecimal getBalance(String iban);
    List<AccountReadOnlyDTO> getAllAccounts();
    String generateReport(String iban);
    void closeAccount(String iban);
}
