package gr.aueb.cf.restbankapp.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.aueb.cf.restbankapp.authentication.JwtService;
import gr.aueb.cf.restbankapp.dto.AccountDepositDTO;
import gr.aueb.cf.restbankapp.dto.AccountReadOnlyDTO;
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

@WebMvcTest(controllers = AccountController.class)
@Import(AccountControllerTest.TestMethodSecurityConfig.class)
class AccountControllerTest {

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
    @WithMockUser(authorities = "DEPOSIT")
    void deposit_shouldReturn201() throws Exception {
        AccountDepositDTO depositDTO = new AccountDepositDTO("GR123", BigDecimal.valueOf(100));
        AccountReadOnlyDTO readOnlyDTO = new AccountReadOnlyDTO("GR123", BigDecimal.valueOf(100));

        when(accountService.deposit(depositDTO)).thenReturn(readOnlyDTO);

        mockMvc.perform(post("/api/v1/accounts/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositDTO))
                        .with(csrf()))
                .andExpect(status().isCreated());
    }

    // Test with a specific authority
    @Test
    @WithMockUser(authorities = "DEPOSIT")
    void deposit_shouldWork_withProperAuthority() throws Exception {
        AccountDepositDTO dto = new AccountDepositDTO("GR123", BigDecimal.valueOf(100));

        when(accountService.deposit(any()))
                .thenReturn(new AccountReadOnlyDTO("GR123", BigDecimal.valueOf(100)));

        mockMvc.perform(post("/api/v1/accounts/deposit")       // ← fixed path
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isCreated());               // ← 201, not 200
    }

    // Test without required authority
    @Test
    @WithMockUser
    void deposit_shouldReturn403_whenNoAuthority() throws Exception {
        AccountDepositDTO dto = new AccountDepositDTO("GR123", BigDecimal.valueOf(100));

        mockMvc.perform(post("/api/v1/accounts/deposit")       // ← fixed path
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}