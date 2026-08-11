package gr.aueb.cf.restbankapp.service;

import gr.aueb.cf.restbankapp.mapper.Mapper;
import gr.aueb.cf.restbankapp.repository.AccountRepository;
import gr.aueb.cf.restbankapp.repository.CustomerRepository;
import gr.aueb.cf.restbankapp.repository.TransactionRepository;
import gr.aueb.cf.restbankapp.security.SecurityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Ownership rules on getTransactions(). IBANs are public knowledge, so a role
 * check alone would let any customer read anyone's statement. The real service
 * bean is loaded here because @PreAuthorize only applies through the
 * method-security proxy.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        AccountServiceImpl.class,
        AccountServiceImplSecurityTest.TestMethodSecurityConfig.class
})
class AccountServiceImplSecurityTest {

    private static final String OWN_IBAN = "GR1600000000000000000000001";
    private static final String OTHER_IBAN = "GR1600000000000000000000003";

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {

        // Registered under the name the @PreAuthorize expression resolves
        @Bean("securityService")
        SecurityService securityService() {
            SecurityService securityService = mock(SecurityService.class);
            when(securityService.isOwnAccount(anyString(), org.mockito.ArgumentMatchers.any()))
                    .thenAnswer(invocation -> OWN_IBAN.equals(invocation.getArgument(0)));
            return securityService;
        }
    }

    @MockBean private AccountRepository accountRepository;
    @MockBean private CustomerRepository customerRepository;
    @MockBean private TransactionRepository transactionRepository;
    @MockBean private Mapper mapper;

    @Autowired
    private IAccountService accountService;

    @Test
    @WithMockUser(authorities = "VIEW_ONLY_ACCOUNT")
    void getTransactions_shouldThrowAccessDenied_whenCustomerReadsAnotherAccount() {
        assertThrows(AccessDeniedException.class,
                () -> accountService.getTransactions(OTHER_IBAN));
    }

    @Test
    @WithMockUser(authorities = "VIEW_ONLY_ACCOUNT")
    void getTransactions_shouldBeAllowed_whenCustomerReadsOwnAccount() {
        when(transactionRepository.findByIbanOrderByCreatedAtDesc(OWN_IBAN)).thenReturn(List.of());

        assertDoesNotThrow(() -> accountService.getTransactions(OWN_IBAN));
    }

    @Test
    @WithMockUser(authorities = "VIEW_ACCOUNT")
    void getTransactions_shouldBeAllowed_forStaffOnAnyAccount() {
        when(transactionRepository.findByIbanOrderByCreatedAtDesc(OTHER_IBAN)).thenReturn(List.of());

        assertDoesNotThrow(() -> accountService.getTransactions(OTHER_IBAN));
    }
}
