package gr.aueb.cf.restbankapp.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.aueb.cf.restbankapp.authentication.AuthenticationService;
import gr.aueb.cf.restbankapp.authentication.JwtService;
import gr.aueb.cf.restbankapp.dto.AuthenticationRequestDTO;
import gr.aueb.cf.restbankapp.dto.AuthenticationResponseDTO;
import gr.aueb.cf.restbankapp.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Login is the one endpoint that has to work without a token, since it is where
 * tokens come from. If it ever stopped being public, nothing else would be reachable.
 */
// The filter chain is switched off here on purpose. @WebMvcTest applies Spring
// Security's default, which demands authentication for every path — including the
// one that hands out tokens. What is under test is the controller's contract and
// the error mapping, not the security configuration; that lives in its own tests.
@WebMvcTest(controllers = AuthRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthRestControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AuthenticationService authenticationService;
    @MockBean private JwtService jwtService;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            var request = invocation.getArgument(0, jakarta.servlet.http.HttpServletRequest.class);
            var response = invocation.getArgument(1, jakarta.servlet.http.HttpServletResponse.class);
            var chain = invocation.getArgument(2, jakarta.servlet.FilterChain.class);
            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    // G.20 — correct credentials come back with a token and the caller's role
    @Test
    void authenticate_shouldReturn200AndAToken() throws Exception {
        AuthenticationRequestDTO request = new AuthenticationRequestDTO("maria", "Test1234!");
        when(authenticationService.authenticate(any()))
                .thenReturn(new AuthenticationResponseDTO("a.jwt.token",
                        "11111111-1111-1111-1111-111111111111", "CUSTOMER",
                        "33333333-3333-3333-3333-333333333333"));

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("a.jwt.token"))
                // The client reads the role off this response to decide what to show
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    // Wrong credentials are 401, not 403: the caller is unknown, not forbidden
    @Test
    void authenticate_shouldReturn401_onWrongCredentials() throws Exception {
        AuthenticationRequestDTO request = new AuthenticationRequestDTO("maria", "wrong");
        when(authenticationService.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}
