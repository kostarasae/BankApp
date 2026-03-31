package gr.aueb.cf.restbankapp.validator;

import gr.aueb.cf.restbankapp.dto.AccountDepositDTO;
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
        AccountDepositDTO dto = new AccountDepositDTO("GR123", BigDecimal.valueOf(-10));

        BeanPropertyBindingResult errors =
                new BeanPropertyBindingResult(dto, "accountDepositDTO");

        validator.validate(dto, errors);

        assertTrue(errors.hasErrors());
    }

    @Test
    void validateWithdraw_shouldPass_whenValidAmount() {
        AccountWithdrawDTO dto = new AccountWithdrawDTO("GR123", BigDecimal.valueOf(50));

        BeanPropertyBindingResult errors =
                new BeanPropertyBindingResult(dto, "accountWithdrawDTO");

        validator.validate(dto, errors);

        assertFalse(errors.hasErrors());
    }
}
