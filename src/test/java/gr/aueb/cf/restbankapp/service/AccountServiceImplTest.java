package gr.aueb.cf.restbankapp.service;

import gr.aueb.cf.restbankapp.config.BankConfiguration;
import gr.aueb.cf.restbankapp.core.exceptions.InsufficientBalanceException;
import gr.aueb.cf.restbankapp.dto.AccountDepositDTO;
import gr.aueb.cf.restbankapp.dto.AccountReadOnlyDTO;
import gr.aueb.cf.restbankapp.dto.AccountWithdrawDTO;
import gr.aueb.cf.restbankapp.mapper.Mapper;
import gr.aueb.cf.restbankapp.model.Account;
import gr.aueb.cf.restbankapp.model.AccountChecking;
import gr.aueb.cf.restbankapp.repository.AccountRepository;
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

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository repository;

    @Mock
    private Mapper mapper;

    @InjectMocks
    private AccountServiceImpl service;

    private Account accountChecking;
    private final BankConfiguration config = BankConfiguration.getInstance();

    //private AccountInsertDTO accountInsertDTO = new AccountInsertDTO();
    private final AccountDepositDTO accountDepositDTO100 = new AccountDepositDTO("GR123", BigDecimal.valueOf(100));
    private final AccountDepositDTO accountDepositDTO200 = new AccountDepositDTO("GR123", BigDecimal.valueOf(200));
    private final AccountDepositDTO accountDepositDTO300 = new AccountDepositDTO("GR123", BigDecimal.valueOf(300));
    private final AccountWithdrawDTO accountWithdrawDTO50 = new AccountWithdrawDTO("GR123", BigDecimal.valueOf(50));
    private final AccountWithdrawDTO accountWithdrawDTO1000 = new AccountWithdrawDTO("GR123", BigDecimal.valueOf(1000));

    @BeforeEach
    void setup() {
        accountChecking = new AccountChecking.Builder("1", "GR123", BigDecimal.ZERO, Instant.now())
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
                .thenReturn(new AccountReadOnlyDTO("GR123", BigDecimal.valueOf(100)));

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
                .thenReturn(new AccountReadOnlyDTO("GR123", BigDecimal.valueOf(200)));

        service.deposit(accountDepositDTO200);

        when(mapper.mapToAccountReadOnlyDTO(any()))
                .thenReturn(new AccountReadOnlyDTO("GR123", BigDecimal.valueOf(50)));

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
                .thenReturn(new AccountReadOnlyDTO("GR123", BigDecimal.valueOf(300)));

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
}