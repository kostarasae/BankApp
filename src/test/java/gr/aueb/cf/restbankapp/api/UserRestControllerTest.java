package gr.aueb.cf.restbankapp.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.aueb.cf.restbankapp.authentication.JwtService;
import gr.aueb.cf.restbankapp.core.exceptions.EntityAlreadyExistsException;
import gr.aueb.cf.restbankapp.dto.UserInsertDTO;
import gr.aueb.cf.restbankapp.dto.UserReadOnlyDTO;
import gr.aueb.cf.restbankapp.security.JwtAuthenticationFilter;
import gr.aueb.cf.restbankapp.service.IUserService;
import gr.aueb.cf.restbankapp.validation.UserInsertValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Filters off: what is under test is the controller's contract, not the security
// configuration, which has its own tests.
@WebMvcTest(controllers = UserRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserRestControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private IUserService userService;
    @MockBean private UserInsertValidator userInsertValidator;
    @MockBean private JwtService jwtService;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;

    // G.19 — creating a user answers 201 and points at where it now lives
    @Test
    void registerUser_shouldReturn201AndALocation() throws Exception {
        UserInsertDTO request = new UserInsertDTO("nikos", "Test1234!", 3L);
        when(userService.saveUser(any()))
                .thenReturn(new UserReadOnlyDTO("22222222-2222-2222-2222-222222222222", "nikos", "CUSTOMER"));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                // 201 without a Location header tells the caller nothing about what was made
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.username").value("nikos"))
                // Whatever else happens, the password must not travel back out
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    // A username already taken is a conflict, not a validation error on the shape
    @Test
    void registerUser_shouldReturn409_whenTheUsernameIsTaken() throws Exception {
        UserInsertDTO request = new UserInsertDTO("maria", "Test1234!", 3L);
        when(userService.saveUser(any()))
                .thenThrow(new EntityAlreadyExistsException("User", "User with username maria already exists"));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isConflict());
    }
}
