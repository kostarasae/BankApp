package gr.aueb.cf.restbankapp.service;

import gr.aueb.cf.restbankapp.core.exceptions.*;
import gr.aueb.cf.restbankapp.dto.*;
import gr.aueb.cf.restbankapp.model.Transaction;

import java.math.BigDecimal;
import java.util.List;

/**
 * Contract
 */
public interface IAccountService {
    AccountReadOnlyDTO createNewAccount(AccountInsertDTO insertDTO)
            throws EntityAlreadyExistsException, EntityNotFoundException, EntityInvalidArgumentException;

    AccountReadOnlyDTO closeAccount(String iban) throws EntityNotFoundException;

    AccountReadOnlyDTO deposit(AccountDepositDTO depositDTO) throws EntityNotFoundException, NegativeAmountException;
    AccountReadOnlyDTO withdraw(AccountWithdrawDTO withdrawDTO) throws EntityNotFoundException, NegativeAmountException, InsufficientBalanceException;
    AccountReadOnlyDTO transfer(AccountTransferDTO transferDTO) throws EntityNotFoundException, NegativeAmountException, InsufficientBalanceException;

    List<TransactionReadOnlyDTO> getTransactions(String iban);

    BigDecimal getBalance(String iban) throws EntityNotFoundException;

    AccountReadOnlyDTO getAccountByIban(String iban) throws EntityNotFoundException;
    AccountReadOnlyDTO getAccountByIbanAndDeletedFalse(String iban) throws EntityNotFoundException;

    public boolean isAccountExists(String iban);

    List<AccountReadOnlyDTO> getAllAccounts();
}
