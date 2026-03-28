package gr.aueb.cf.restbankapp.service;

import gr.aueb.cf.restbankapp.core.exceptions.*;
import gr.aueb.cf.restbankapp.dto.*;

import java.math.BigDecimal;

/**
 * Contract
 */
public interface IAccountService {
    AccountReadOnlyDTO createNewAccount(AccountInsertDTO insertDTO)
            throws EntityAlreadyExistsException;

    AccountReadOnlyDTO closeAccount(String iban) throws EntityNotFoundException;

    AccountReadOnlyDTO deposit(AccountDepositDTO depositDTO) throws EntityNotFoundException, NegativeAmountException;
    AccountReadOnlyDTO withdraw(AccountWithdrawDTO withdrawDTO) throws EntityNotFoundException, NegativeAmountException, InsufficientBalanceException;

    BigDecimal getBalance(String iban) throws EntityNotFoundException;

    AccountReadOnlyDTO getAccountByIban(String iban) throws EntityNotFoundException;
    AccountReadOnlyDTO getAccountByIbanAndDeletedFalse(String iban) throws EntityNotFoundException;

    public boolean isAccountExists(String iban);
}
