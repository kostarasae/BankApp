package gr.aueb.cf.restbankapp.core.factory;

import gr.aueb.cf.restbankapp.model.Account;
import gr.aueb.cf.restbankapp.model.AccountChecking;
import gr.aueb.cf.restbankapp.model.AccountSavings;
import gr.aueb.cf.restbankapp.model.AccountType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class AccountFactoryTest {

    @Test
    void createCheckingAccount_shouldReturnCheckingAccount() {
        Account account = AccountFactory.create(
                AccountType.CHECKING,
                BigDecimal.ZERO
        );

        assertNotNull(account);
        assertTrue(account instanceof AccountChecking);
    }

    @Test
    void createSavingsAccount_shouldReturnSavingsAccount() {
        Account account = AccountFactory.create(
                AccountType.SAVINGS,
                BigDecimal.valueOf(200)
        );

        assertNotNull(account);
        assertTrue(account instanceof AccountSavings);
    }
}