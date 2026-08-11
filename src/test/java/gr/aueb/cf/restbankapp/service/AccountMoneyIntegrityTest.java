package gr.aueb.cf.restbankapp.service;

import gr.aueb.cf.restbankapp.config.BankConfiguration;
import gr.aueb.cf.restbankapp.dto.AccountTransferDTO;
import gr.aueb.cf.restbankapp.dto.AccountWithdrawDTO;
import gr.aueb.cf.restbankapp.mapper.Mapper;
import gr.aueb.cf.restbankapp.model.Account;
import gr.aueb.cf.restbankapp.model.AccountChecking;
import gr.aueb.cf.restbankapp.model.Transaction;
import gr.aueb.cf.restbankapp.model.TransactionType;
import gr.aueb.cf.restbankapp.repository.AccountRepository;
import gr.aueb.cf.restbankapp.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The statement has to explain the balance. Every euro that moves must leave a
 * row behind, including fees and the recipient's side of a transfer, otherwise
 * the sum of the transactions never matches what the account actually holds.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountMoneyIntegrityTest {

    private static final String SOURCE_IBAN = "GR123";
    private static final String TARGET_IBAN = "GR456";

    @Mock private AccountRepository accountRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private Mapper mapper;

    @InjectMocks private AccountServiceImpl service;

    private final BankConfiguration config = BankConfiguration.getInstance();
    private Account source;
    private Account target;

    @BeforeEach
    void setup() {
        source = new AccountChecking.Builder("1", SOURCE_IBAN, BigDecimal.valueOf(1000), "customer-a")
                .feeStrategy(config.getDefaultCheckingFeeStrategy())
                .build();
        target = new AccountChecking.Builder("2", TARGET_IBAN, BigDecimal.valueOf(500), "customer-b")
                .feeStrategy(config.getDefaultCheckingFeeStrategy())
                .build();
        when(accountRepository.findByIban(SOURCE_IBAN)).thenReturn(Optional.of(source));
        when(accountRepository.findByIban(TARGET_IBAN)).thenReturn(Optional.of(target));
    }

    /** Money in minus money out, using the same sign convention as the client. */
    private BigDecimal netOf(List<Transaction> transactions, String iban) {
        return transactions.stream()
                .filter(t -> iban.equals(t.getIban()))
                .map(t -> t.getType() == TransactionType.DEPOSIT ? t.getAmount() : t.getAmount().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<Transaction> savedTransactions() {
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, atLeastOnce()).save(captor.capture());
        return captor.getAllValues();
    }

    @Test
    void withdraw_shouldRecordTheFeeAsWellAsTheAmount() throws Exception {
        BigDecimal before = source.getBalance();

        service.withdraw(new AccountWithdrawDTO(SOURCE_IBAN, "atm", BigDecimal.valueOf(100)));

        BigDecimal actualChange = source.getBalance().subtract(before);
        assertEquals(0, netOf(savedTransactions(), SOURCE_IBAN).compareTo(actualChange),
                "the recorded movements must account for the full balance change, fee included");
    }

    @Test
    void transfer_shouldRecordBothSidesAndTheFee() throws Exception {
        BigDecimal sourceBefore = source.getBalance();
        BigDecimal targetBefore = target.getBalance();

        service.transfer(new AccountTransferDTO(SOURCE_IBAN, TARGET_IBAN, "rent", BigDecimal.valueOf(200)));

        List<Transaction> saved = savedTransactions();
        assertEquals(0, netOf(saved, SOURCE_IBAN).compareTo(source.getBalance().subtract(sourceBefore)),
                "the sender's rows must cover the amount and the fee");
        assertEquals(0, netOf(saved, TARGET_IBAN).compareTo(target.getBalance().subtract(targetBefore)),
                "the recipient must see the incoming money on their own statement");
    }
}
