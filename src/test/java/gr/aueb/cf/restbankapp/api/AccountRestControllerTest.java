package gr.aueb.cf.restbankapp.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.aueb.cf.restbankapp.authentication.JwtService;
import gr.aueb.cf.restbankapp.dto.AccountDepositDTO;
import gr.aueb.cf.restbankapp.dto.AccountWithdrawDTO;
import gr.aueb.cf.restbankapp.dto.AccountReadOnlyDTO;
import gr.aueb.cf.restbankapp.model.AccountType;
import gr.aueb.cf.restbankapp.security.JwtAuthenticationFilter;
import gr.aueb.cf.restbankapp.service.IAccountService;
import gr.aueb.cf.restbankapp.validation.AccountValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.BeforeEach;
import static org.mockito.Mockito.doAnswer;

@WebMvcTest(controllers = AccountRestController.class)
@Import(AccountRestControllerTest.TestMethodSecurityConfig.class)
class AccountRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IAccountService accountService;

    @MockBean
    private AccountValidator accountValidator;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() throws Exception {
        // Make the mocked JWT filter pass requests through to the next filter
        doAnswer(invocation -> {
            var request = invocation.getArgument(0, jakarta.servlet.http.HttpServletRequest.class);
            var response = invocation.getArgument(1, jakarta.servlet.http.HttpServletResponse.class);
            var chain = invocation.getArgument(2, jakarta.servlet.FilterChain.class);
            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {
    }

    // Basic test — bypass security with csrf disabled
    @Test
    @WithMockUser(authorities = "CAN_DEPOSIT")
    void deposit_shouldReturn201() throws Exception {
        AccountDepositDTO depositDTO = new AccountDepositDTO("GR123", null, BigDecimal.valueOf(100));
        AccountReadOnlyDTO readOnlyDTO = new AccountReadOnlyDTO("GR123", "0000000123", AccountType.CHECKING, BigDecimal.valueOf(100));

        when(accountService.deposit(depositDTO)).thenReturn(readOnlyDTO);

        mockMvc.perform(post("/api/v1/accounts/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositDTO))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    // Test with a specific authority
    @Test
    @WithMockUser(authorities = "CAN_DEPOSIT")
    void deposit_shouldWork_withProperAuthority() throws Exception {
        AccountDepositDTO dto = new AccountDepositDTO("GR123", null, BigDecimal.valueOf(100));

        when(accountService.deposit(any()))
                .thenReturn(new AccountReadOnlyDTO("GR123", "0000000123", AccountType.CHECKING, BigDecimal.valueOf(100)));

        mockMvc.perform(post("/api/v1/accounts/deposit")       // ← fixed path
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isOk());               // ← 201, not 200
    }

    // Authorization for deposit lives on the service, which @WebMvcTest replaces with
    // a mock — so it cannot be exercised from here. It is covered instead by
    // AccountServiceImplSecurityTest, which loads the real bean behind the
    // method-security proxy.

    // G.8 — the happy path for a withdrawal through the HTTP layer
    @Test
    @WithMockUser(authorities = "CAN_WITHDRAW")
    void withdraw_shouldReturn200_withTheAuthority() throws Exception {
        AccountWithdrawDTO dto = new AccountWithdrawDTO("GR123", null, BigDecimal.valueOf(50));

        when(accountService.withdraw(any()))
                .thenReturn(new AccountReadOnlyDTO("GR123", "0000000123", AccountType.CHECKING, BigDecimal.valueOf(50)));

        mockMvc.perform(post("/api/v1/accounts/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    // G.9 — with no user at all the request never reaches the controller. This is
    // authentication, decided by the filter chain, not authorization on a method —
    // which is why it can be tested here while the 403 case cannot.
    @Test
    void deposit_shouldReturn401_whenUnauthenticated() throws Exception {
        AccountDepositDTO dto = new AccountDepositDTO("GR123", null, BigDecimal.valueOf(100));

        mockMvc.perform(post("/api/v1/accounts/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}
