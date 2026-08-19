package gr.aueb.cf.restbankapp.validator;

import gr.aueb.cf.restbankapp.dto.AccountDepositDTO;
import gr.aueb.cf.restbankapp.dto.AccountTransferDTO;
import gr.aueb.cf.restbankapp.dto.AccountWithdrawDTO;
import gr.aueb.cf.restbankapp.service.IAccountService;
import gr.aueb.cf.restbankapp.validation.AccountValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.validation.BeanPropertyBindingResult;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class AccountValidatorTest {

    @InjectMocks
    private AccountValidator validator;

    @Mock
    private IAccountService accountService;


    @BeforeEach
    void setup() {
        validator = new AccountValidator(accountService);
    }

    @Test
    void validateDeposit_shouldHaveErrors_whenNegativeAmount() {
        AccountDepositDTO dto = new AccountDepositDTO("GR123", null, BigDecimal.valueOf(-10));

        BeanPropertyBindingResult errors =
                new BeanPropertyBindingResult(dto, "accountDepositDTO");

        validator.validate(dto, errors);

        assertTrue(errors.hasErrors());
    }

    @Test
    void validateWithdraw_shouldPass_whenValidAmount() {
        AccountWithdrawDTO dto = new AccountWithdrawDTO("GR123", null, BigDecimal.valueOf(50));

        BeanPropertyBindingResult errors =
                new BeanPropertyBindingResult(dto, "accountWithdrawDTO");

        validator.validate(dto, errors);

        assertFalse(errors.hasErrors());
    }

    // G.6 — sending money to yourself is not a transfer; it would just burn the fee
    @Test
    void validateTransfer_shouldHaveError_whenSenderAndRecipientAreTheSame() {
        AccountTransferDTO dto = new AccountTransferDTO("GR123", "GR123", "to myself", BigDecimal.valueOf(50));

        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(dto, "accountTransferDTO");
        validator.validate(dto, errors);

        assertTrue(errors.hasErrors());
        // Named so the client can point at the offending field rather than the whole form
        assertNotNull(errors.getFieldError("toIban"));
    }

    // G.7 — zero is not a transfer either, and it used to slip through (H.7)
    @Test
    void validateTransfer_shouldHaveError_whenAmountIsZero() {
        AccountTransferDTO dto = new AccountTransferDTO("GR123", "GR456", "nothing", BigDecimal.ZERO);

        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(dto, "accountTransferDTO");
        validator.validate(dto, errors);

        assertTrue(errors.hasErrors());
        assertNotNull(errors.getFieldError("amount"));
    }

    // The same rule now holds for deposits and withdrawals, which is what H.7 fixed
    @Test
    void validateDeposit_shouldHaveError_whenAmountIsZero() {
        AccountDepositDTO dto = new AccountDepositDTO("GR123", null, BigDecimal.ZERO);

        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(dto, "accountDepositDTO");
        validator.validate(dto, errors);

        assertTrue(errors.hasErrors());
    }
}
