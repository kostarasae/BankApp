package gr.aueb.cf.restbankapp.service;

import gr.aueb.cf.restbankapp.core.exceptions.*;
import gr.aueb.cf.restbankapp.dto.*;
import gr.aueb.cf.restbankapp.mapper.Mapper;
import gr.aueb.cf.restbankapp.model.Account;
import gr.aueb.cf.restbankapp.model.Customer;
import gr.aueb.cf.restbankapp.core.factory.AccountFactory;
import gr.aueb.cf.restbankapp.model.Transaction;
import gr.aueb.cf.restbankapp.model.TransactionType;
import gr.aueb.cf.restbankapp.repository.AccountRepository;
import gr.aueb.cf.restbankapp.repository.CustomerRepository;
import gr.aueb.cf.restbankapp.repository.TransactionRepository;
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
    private final TransactionRepository transactionRepository;
    private final Mapper mapper;

    @Override
    @PreAuthorize("hasAuthority('CREATE_ACCOUNT')")
    @Transactional(rollbackFor = { EntityAlreadyExistsException.class, EntityInvalidArgumentException.class, EntityNotFoundException.class })
    public AccountReadOnlyDTO createNewAccount(AccountInsertDTO accountInsertDTO)
            throws EntityNotFoundException {
        try {
            Account account = AccountFactory.create(accountInsertDTO.accountType(), accountInsertDTO.initialDeposit(), accountInsertDTO.customerUuid());
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
    @Transactional(rollbackFor = { EntityNotFoundException.class, NegativeAmountException.class })
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
            Transaction transaction = new Transaction();
            transaction.setIban(depositDTO.iban());
            transaction.setAmount(depositDTO.amount());
            transaction.setType(TransactionType.DEPOSIT);
            transaction.setDescription(depositDTO.description());
            transactionRepository.save(transaction);
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
    @Transactional(rollbackFor = { EntityNotFoundException.class, NegativeAmountException.class, InsufficientBalanceException.class })
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
            Transaction transaction = new Transaction();
            transaction.setIban(withdrawDTO.iban());
            transaction.setAmount(withdrawDTO.amount());
            transaction.setType(TransactionType.WITHDRAWAL);
            transaction.setDescription(withdrawDTO.description());
            transactionRepository.save(transaction);
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

    @PreAuthorize("hasAuthority('CAN_TRANSFER')")
    @Transactional(rollbackFor = { EntityNotFoundException.class, NegativeAmountException.class, InsufficientBalanceException.class })
    @Override
    public synchronized AccountReadOnlyDTO transfer(AccountTransferDTO transferDTO)
            throws EntityNotFoundException, NegativeAmountException, InsufficientBalanceException {
        try {
            Account sourceAccount = accountRepository.findByIban(transferDTO.myIban())
                    .orElseThrow(() -> new EntityNotFoundException("Account", "Source account with iban " + transferDTO.myIban() + " not found"));
            Account destinationAccount = accountRepository.findByIban(transferDTO.toIban())
                    .orElseThrow(() -> new EntityNotFoundException("Account", "Destination account with iban " + transferDTO.toIban() + " not found"));
            if (transferDTO.amount().signum() < 0) {
                throw new NegativeAmountException("Account", "Negative transfer amount " + transferDTO.amount() + " was not accepted");
            }
            BigDecimal finalAmount = sourceAccount.getFeeStrategy().calculateFee(transferDTO.amount());
            BigDecimal predictedBalance = sourceAccount.getBalance().subtract(finalAmount);
            if (sourceAccount.violatesRules(predictedBalance))
                throw new InsufficientBalanceException("Account", "Invalid transfer from account with iban " + transferDTO.myIban()
                        + ". Amount of " + transferDTO.amount() + " exceeds balance");
            sourceAccount.setBalance(predictedBalance);
            destinationAccount.setBalance(destinationAccount.getBalance().add(transferDTO.amount()));
            accountRepository.save(sourceAccount);
            accountRepository.save(destinationAccount);
            log.info("Amount of {} has been transferred from account with iban={} to account with iban={} successfully",
                    transferDTO.amount(), transferDTO.myIban(), transferDTO.toIban());
            Transaction transaction = new Transaction();
            transaction.setIban(transferDTO.myIban());
            transaction.setAmount(transferDTO.amount());
            transaction.setType(TransactionType.TRANSFER);
            transaction.setDescription(transferDTO.description());
            transactionRepository.save(transaction);
            return mapper.mapToAccountReadOnlyDTO(sourceAccount);
        } catch (EntityNotFoundException e) {
            log.error("The account with iban={} was not found", e.getMessage().contains("Source") ? transferDTO.myIban() : transferDTO.toIban());
            throw e;
        } catch (NegativeAmountException e) {
            log.error("Negative transfer amount={} was not accepted", transferDTO.amount());
            throw e;
        } catch (InsufficientBalanceException e) {
            log.error("The amount={} is greater than the balance of the account with iban={}", transferDTO.amount(), transferDTO.myIban());
            throw e;
        }
    }

    @PreAuthorize("hasAnyAuthority('VIEW_ACCOUNT', 'VIEW_ONLY_ACCOUNT')")
    @Override
    public synchronized List<TransactionReadOnlyDTO> getTransactions(String iban) {
        return transactionRepository.findByIbanOrderByCreatedAtDesc(iban)
                .stream()
                .map(mapper::mapToTransactionReadOnlyDTO)
                .toList();
    }

    @PreAuthorize("hasAuthority('VIEW_ONLY_ACCOUNT')")
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
