package chatgpt.service;

import chatgpt.core.exceptions.InsufficientBalanceException;
import chatgpt.core.exceptions.AccountNotFoundException;
import chatgpt.core.exceptions.AccountNotReportableException;
import chatgpt.core.mapper.Mapper;
import chatgpt.core.logging.AccountLogging;
import chatgpt.dao.IAccountDAO;
import chatgpt.dto.AccountDepositDTO;
import chatgpt.dto.AccountInsertDTO;
import chatgpt.dto.AccountReadOnlyDTO;
import chatgpt.dto.AccountWithdrawDTO;
import chatgpt.model.Account;
import chatgpt.model.Reportable;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class AccountServiceImpl implements IAccountService {

    private final IAccountDAO accountDAO;

    public AccountServiceImpl(IAccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }


    @Override
    public AccountReadOnlyDTO createNewAccount(AccountInsertDTO insertDTO) {
        // Map insert DTO to account model
        Account account = Mapper.mapToModelEntity(insertDTO);
        // Apply account on model
        account = accountDAO.saveOrUpdate(account);
        // Return read only DTO
        return Mapper.mapToReadOnlyDTO(account);
    }


    @Override
    public final synchronized void deposit(AccountDepositDTO depositDTO) throws AccountNotFoundException {
        try {
            Account account = accountDAO.findByIban(depositDTO.iban())
                    .orElseThrow(() -> new AccountNotFoundException("Account with iban " + depositDTO.iban() + " not found!"));
            account.setBalance(account.getBalance().add(depositDTO.amount()));
            accountDAO.saveOrUpdate(account);
            AccountLogging.logging(account.getIban(), depositDTO.amount(), account.getBalance(), true);
        } catch (AccountNotFoundException e) {
            System.err.printf("The account with iban=%s was not found!\n", depositDTO.iban());
            throw e;
        }
    }

    @Override
    public final synchronized void withdraw(AccountWithdrawDTO withdrawDTO) throws AccountNotFoundException, InsufficientBalanceException {
        try {
            Account account = accountDAO.findByIban(withdrawDTO.iban())
                    .orElseThrow(() -> new AccountNotFoundException("Account with iban " + withdrawDTO.iban() + " not found!"));
            BigDecimal finalAmount = account.getFeeStrategy().calculateFee(withdrawDTO.amount());
            BigDecimal predictedBalance = account.getBalance().subtract(finalAmount);
            if (account.violatesRules(predictedBalance))
                throw new InsufficientBalanceException("Invalid withdrawal for account with iban " + withdrawDTO.iban()
                        + ". Amount of " + withdrawDTO.amount() + " exceeds balance!");
            account.setBalance(predictedBalance);
            accountDAO.saveOrUpdate(account);
            AccountLogging.logging(account.getIban(), finalAmount, account.getBalance(), false);
        } catch (AccountNotFoundException e) {
            System.err.printf("The account with iban=%s was not found!\n", withdrawDTO.iban());
            throw e;
        } catch (InsufficientBalanceException e) {
            System.err.printf("The amount=%s is greater than the balance of the account with iban=%s. \n",
                    withdrawDTO.amount(), withdrawDTO.iban());
            throw e;
        }
    }

    @Override
    public final synchronized BigDecimal getBalance(String iban) throws AccountNotFoundException {
        try {
            Account account = accountDAO.findByIban(iban)
                    .orElseThrow(()-> new AccountNotFoundException("Account with iban " + iban + " not found!"));
            return account.getBalance();
        } catch (AccountNotFoundException e) {
            System.err.printf("The account with iban=%s was not found!\n", iban);
            throw e;
        }
    }

    @Override
    public final synchronized List<AccountReadOnlyDTO> getAllAccounts() {
        return accountDAO.getAllAccounts().stream().map(Mapper::mapToReadOnlyDTO).toList();
    }


    @Override
    public final synchronized String generateReport(String iban) {

        Account account = accountDAO.findByIban(iban).orElseThrow(()-> new AccountNotFoundException(iban));

        if (!(account instanceof Reportable)) {
            throw new AccountNotReportableException("Account with iban " + iban + " does not support reporting!");
        }

        return account.toString();
    }


    @Override
    public final synchronized void closeAccount(String iban) throws AccountNotFoundException {
        try {
            Account account = accountDAO.findByIban(iban)
                    .orElseThrow(() -> new AccountNotFoundException("Account with iban " + iban + " not found!"));
            accountDAO.remove(iban);

        } catch (AccountNotFoundException e) {
            System.err.printf("The account with iban=%s was not found!\n", iban);
            throw e;
        }
    }
}
