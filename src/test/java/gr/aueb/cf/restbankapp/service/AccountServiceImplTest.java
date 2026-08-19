package gr.aueb.cf.restbankapp.service;

import gr.aueb.cf.restbankapp.config.BankConfiguration;
import gr.aueb.cf.restbankapp.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.restbankapp.core.exceptions.InsufficientBalanceException;
import gr.aueb.cf.restbankapp.core.exceptions.NegativeAmountException;
import gr.aueb.cf.restbankapp.dto.AccountDepositDTO;
import gr.aueb.cf.restbankapp.dto.AccountReadOnlyDTO;
import gr.aueb.cf.restbankapp.dto.AccountTransferDTO;
import gr.aueb.cf.restbankapp.model.AccountType;
import gr.aueb.cf.restbankapp.dto.AccountWithdrawDTO;
import gr.aueb.cf.restbankapp.mapper.Mapper;
import gr.aueb.cf.restbankapp.model.Account;
import gr.aueb.cf.restbankapp.model.AccountChecking;
import gr.aueb.cf.restbankapp.repository.AccountRepository;
import gr.aueb.cf.restbankapp.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository repository;

    @Mock
    private Mapper mapper;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AccountServiceImpl service;

    private Account accountChecking;
    private final BankConfiguration config = BankConfiguration.getInstance();

    //private AccountInsertDTO accountInsertDTO = new AccountInsertDTO();
    private final AccountDepositDTO accountDepositDTO100 = new AccountDepositDTO("GR123", null, BigDecimal.valueOf(100));
    private final AccountDepositDTO accountDepositDTO200 = new AccountDepositDTO("GR123", null, BigDecimal.valueOf(200));
    private final AccountDepositDTO accountDepositDTO300 = new AccountDepositDTO("GR123", null, BigDecimal.valueOf(300));
    private final AccountWithdrawDTO accountWithdrawDTO50 = new AccountWithdrawDTO("GR123", null, BigDecimal.valueOf(50));
    private final AccountWithdrawDTO accountWithdrawDTO1000 = new AccountWithdrawDTO("GR123", null, BigDecimal.valueOf(1000));

    @BeforeEach
    void setup() {
        accountChecking = new AccountChecking.Builder("1", "GR123", BigDecimal.ZERO, "customer-uuid")
                .feeStrategy(config.getDefaultCheckingFeeStrategy())
                .build();
    }

    @Test
    void deposit_shouldIncreaseBalance() throws Exception {
        when(repository.findByIban(anyString()))
                .thenReturn(Optional.of(accountChecking));

        when(repository.save(any(Account.class)))
                .thenReturn(accountChecking);

        when(mapper.mapToAccountReadOnlyDTO(any()))
                .thenReturn(new AccountReadOnlyDTO("GR123", "0000000123", AccountType.CHECKING, BigDecimal.valueOf(100)));

        service.deposit(accountDepositDTO100);

        assertEquals(BigDecimal.valueOf(100), accountChecking.getBalance());
    }

    @Test
    void withdraw_shouldDecreaseBalance() throws Exception {
        when(repository.findByIban(anyString()))
                .thenReturn(Optional.of(accountChecking));

        when(repository.save(any(Account.class)))
                .thenReturn(accountChecking);

        when(mapper.mapToAccountReadOnlyDTO(any()))
                .thenReturn(new AccountReadOnlyDTO("GR123", "0000000123", AccountType.CHECKING, BigDecimal.valueOf(200)));

        service.deposit(accountDepositDTO200);

        when(mapper.mapToAccountReadOnlyDTO(any()))
                .thenReturn(new AccountReadOnlyDTO("GR123", "0000000123", AccountType.CHECKING, BigDecimal.valueOf(50)));

        service.withdraw(accountWithdrawDTO50);

        assertEquals(BigDecimal.valueOf(149.5), accountChecking.getBalance());
    }

    @Test
    void getBalance_shouldReturnCorrectAmount() throws Exception {
        when(repository.findByIban(anyString()))
                .thenReturn(Optional.of(accountChecking));

        when(repository.save(any(Account.class)))
                .thenReturn(accountChecking);

        when(mapper.mapToAccountReadOnlyDTO(any()))
                .thenReturn(new AccountReadOnlyDTO("GR123", "0000000123", AccountType.CHECKING, BigDecimal.valueOf(300)));

        service.deposit(accountDepositDTO300);

        BigDecimal balance = service.getBalance("GR123");

        assertEquals(BigDecimal.valueOf(300), balance);
    }

    @Test
    void withdraw_shouldThrowException_whenInsufficientBalance() throws InsufficientBalanceException {
        when(repository.findByIban("GR123")).thenReturn(Optional.of(accountChecking));

        assertThrows(InsufficientBalanceException.class, () -> {
            service.withdraw(accountWithdrawDTO1000);
        });
    }

    @Test
    void deposit_negativeAmount_throwsNegativeAmountException() {
        when(repository.findByIban(anyString()))
            .thenReturn(Optional.of(accountChecking));

        AccountDepositDTO dto = new AccountDepositDTO("GR123", null, BigDecimal.valueOf(-50));

        assertThrows(NegativeAmountException.class, () -> service.deposit(dto));
        verify(repository, never()).save(any());
    }

    @Test
    void deposit_accountNotFound_throwsEntityNotFoundException() {
       when(repository.findByIban(anyString())).thenReturn(Optional.empty());

       AccountDepositDTO dto = new AccountDepositDTO("GR123", null, BigDecimal.valueOf(100));

       assertThrows(EntityNotFoundException.class, () -> service.deposit(dto));
    }

    // G.4 — the account has to exist before anything else is considered
    @Test
    void deposit_shouldThrowEntityNotFound_whenIbanIsUnknown() {
        when(repository.findByIban(anyString())).thenReturn(Optional.empty());

        AccountDepositDTO dto = new AccountDepositDTO("GR-does-not-exist", null, BigDecimal.valueOf(100));

        assertThrows(EntityNotFoundException.class, () -> service.deposit(dto));
        verify(repository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    // G.5 — a transfer moves money out of one account and into the other, and the
    // sender also pays the fee. Both sides must end up right, not just the sender.
    @Test
    void transfer_shouldMoveMoneyAndChargeTheSender() throws Exception {
        Account source = new AccountChecking.Builder("1", "GR123", BigDecimal.valueOf(1000), "customer-a")
                .feeStrategy(config.getDefaultCheckingFeeStrategy())
                .build();
        Account target = new AccountChecking.Builder("2", "GR456", BigDecimal.valueOf(500), "customer-b")
                .feeStrategy(config.getDefaultCheckingFeeStrategy())
                .build();
        when(repository.findByIban("GR123")).thenReturn(Optional.of(source));
        when(repository.findByIban("GR456")).thenReturn(Optional.of(target));

        BigDecimal fee = source.getFeeStrategy().calculateFee(BigDecimal.valueOf(200));
        service.transfer(new AccountTransferDTO("GR123", "GR456", "rent", BigDecimal.valueOf(200)));

        // Sender pays the amount plus the fee; recipient receives the amount only.
        assertEquals(0, BigDecimal.valueOf(1000).subtract(BigDecimal.valueOf(200)).subtract(fee)
                .compareTo(source.getBalance()));
        assertEquals(0, BigDecimal.valueOf(700).compareTo(target.getBalance()));
    }
}
