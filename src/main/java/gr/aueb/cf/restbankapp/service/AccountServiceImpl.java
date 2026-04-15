package gr.aueb.cf.restbankapp.service;

import gr.aueb.cf.restbankapp.core.exceptions.*;
import gr.aueb.cf.restbankapp.dto.*;
import gr.aueb.cf.restbankapp.mapper.Mapper;
import gr.aueb.cf.restbankapp.model.Account;
import gr.aueb.cf.restbankapp.model.Customer;
import gr.aueb.cf.restbankapp.core.factory.AccountFactory;
import gr.aueb.cf.restbankapp.repository.AccountRepository;
import gr.aueb.cf.restbankapp.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements IAccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final Mapper mapper;

    @Override
    @PreAuthorize("hasAuthority('CREATE_ACCOUNT')")
    @Transactional(rollbackFor = { EntityAlreadyExistsException.class, EntityInvalidArgumentException.class, EntityNotFoundException.class })
    public AccountReadOnlyDTO createNewAccount(AccountInsertDTO accountInsertDTO)
            throws EntityNotFoundException {
        try {
            Account account = AccountFactory.create(accountInsertDTO.type(), accountInsertDTO.balance(), accountInsertDTO.customerUuid());
            // Save account to database
            accountRepository.save(account);            
            Customer customer = customerRepository.findByUuid(UUID.fromString(accountInsertDTO.customerUuid())) .orElseThrow(() -> 
                    new EntityNotFoundException("Customer", "Customer with uuid=" + accountInsertDTO.customerUuid() + " not found!"));
            customer.addAccount(account);
            customerRepository.save(customer);
            // Return read only DTO
            return mapper.mapToAccountReadOnlyDTO(account);
        } catch (EntityNotFoundException e) {
            log.error("Save failed. Customer with uuid={} not found", accountInsertDTO.customerUuid());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAccountExists(String iban) {
        return accountRepository.findByIban(iban).isPresent();
    }

    @Override
    @PreAuthorize("hasAuthority('DELETE_ACCOUNT')")
    @Transactional(rollbackFor = EntityNotFoundException.class)
    public synchronized AccountReadOnlyDTO closeAccount(String iban)
            throws EntityNotFoundException {
        try {
            Account account = accountRepository.findByIban(iban)
                    .orElseThrow(() -> new EntityNotFoundException("Account", "Account with iban " + iban + " not found!"));
            account.softDelete();
            account.getCustomers().forEach(Customer::softDelete);
            log.info("Account with iban={} deleted successfully", iban);
            return mapper.mapToAccountReadOnlyDTO(account);
        } catch (EntityNotFoundException e) {
            log.error("The account with iban={} was not found", iban);
            throw e;
        }
    }

    @PreAuthorize("hasAuthority('CAN_DEPOSIT')")
    @Override
    public synchronized AccountReadOnlyDTO deposit(AccountDepositDTO depositDTO)
            throws EntityNotFoundException, NegativeAmountException {
        try {
            Account account = accountRepository.findByIban(depositDTO.iban())
                    .orElseThrow(() -> new EntityNotFoundException("Account", "Account with iban " + depositDTO.iban() + " not found!"));
            if (depositDTO.amount().signum() < 0) {
                throw new NegativeAmountException("Account", "Negative deposit amount " + depositDTO.amount() + " was not accepted");
            }
            account.setBalance(account.getBalance().add(depositDTO.amount()));
            accountRepository.save(account);
            log.info("Amount of {} has been deposit to account with iban={} successfully", depositDTO.amount(), depositDTO.iban());
            return mapper.mapToAccountReadOnlyDTO(account);
        } catch (EntityNotFoundException e) {
            log.error("The account with iban={} was not found", depositDTO.iban());
            throw e;
        } catch (NegativeAmountException e) {
            log.error("Negative deposit amount={} was not accepted", depositDTO.amount());
            throw e;
        }
    }

    @PreAuthorize("hasAuthority('CAN_WITHDRAW')")
    @Override
    public synchronized AccountReadOnlyDTO withdraw(AccountWithdrawDTO withdrawDTO)
            throws EntityNotFoundException, NegativeAmountException, InsufficientBalanceException {
        try {
            Account account = accountRepository.findByIban(withdrawDTO.iban())
                    .orElseThrow(() -> new EntityNotFoundException("Account", "Account with iban={} " + withdrawDTO.iban() + " not found"));
            if (withdrawDTO.amount().signum() < 0) {
                throw new NegativeAmountException("Account", "Negative withdrawal amount " + withdrawDTO.amount() + " was not accepted");
            }
            BigDecimal finalAmount = account.getFeeStrategy().calculateFee(withdrawDTO.amount());
            BigDecimal predictedBalance = account.getBalance().subtract(finalAmount);
            if (account.violatesRules(predictedBalance))
                throw new InsufficientBalanceException("Account", "Invalid withdrawal for account with iban " + withdrawDTO.iban()
                        + ". Amount of " + withdrawDTO.amount() + " exceeds balance");
            account.setBalance(predictedBalance);
            accountRepository.save(account);
            log.info("Amount of {} has been withdrawn from account with iban={} successfully", withdrawDTO.amount(), withdrawDTO.iban());
            return mapper.mapToAccountReadOnlyDTO(account);
        } catch (EntityNotFoundException e) {
            log.error("The account with iban={} was not found", withdrawDTO.iban());
            throw e;
        } catch (NegativeAmountException e) {
            log.error("Negative withdrawal amount={} was not accepted", withdrawDTO.amount());
            throw e;
        } catch (InsufficientBalanceException e) {
            log.error("The amount={} is greater than the balance of the account with iban={}", withdrawDTO.amount(), withdrawDTO.iban());
            throw e;
        }
    }

    @PreAuthorize("hasAuthority('VIEW_ACCOUNT')")
    @Override
    public synchronized BigDecimal getBalance(String iban) throws EntityNotFoundException {
        try {
            Account account = accountRepository.findByIban(iban)
                    .orElseThrow(()-> new EntityNotFoundException("Account", "Account with iban " + iban + " not found"));
            return account.getBalance();
        } catch (EntityNotFoundException e) {
            log.error("The account with iban={} was not found", iban);
            throw e;
        }
    }

    @PreAuthorize("hasAnyAuthority('VIEW_ACCOUNT', 'VIEW_ONLY_ACCOUNT')")
    @Override
    public AccountReadOnlyDTO getAccountByIban(String iban) throws EntityNotFoundException {
        try {
            Account account = accountRepository.findByIban(iban)
                    .orElseThrow(() -> new EntityNotFoundException("Account","Account with iban=" + iban + " not found"));
            log.debug("Get account by iban={} returned successfully", iban);
            return mapper.mapToAccountReadOnlyDTO(account);
        } catch (EntityNotFoundException e) {
            log.error("Get account by iban={} failed", iban, e);
            throw e;
        }
    }

    @Override
    @PreAuthorize("hasAuthority('VIEW_ACCOUNTS')")
    @Transactional(readOnly = true)
    public List<AccountReadOnlyDTO> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(mapper::mapToAccountReadOnlyDTO)
                .toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public AccountReadOnlyDTO getAccountByIbanAndDeletedFalse(String iban) throws EntityNotFoundException {
        try {
            Account account = accountRepository.findByIbanAndDeletedFalse(iban)
                    .orElseThrow(() -> new EntityNotFoundException("Account","Account with iban=" + iban + " not found"));
            log.debug("Get non-deleted account by iban={} returned successfully", iban);
            return mapper.mapToAccountReadOnlyDTO(account);
        } catch (EntityNotFoundException e) {
            log.error("Get account by iban={} failed", iban, e);
            throw e;
        }
    }
}
