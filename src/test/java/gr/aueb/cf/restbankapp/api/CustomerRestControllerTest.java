package gr.aueb.cf.restbankapp.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.aueb.cf.restbankapp.authentication.JwtService;
import gr.aueb.cf.restbankapp.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.restbankapp.dto.CustomerReadOnlyDTO;
import gr.aueb.cf.restbankapp.dto.PersonalInfoReadOnlyDTO;
import gr.aueb.cf.restbankapp.security.JwtAuthenticationFilter;
import gr.aueb.cf.restbankapp.service.ICustomerService;
import gr.aueb.cf.restbankapp.validation.CustomerEditValidator;
import gr.aueb.cf.restbankapp.validation.CustomerInsertValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Filters off: what is under test is the controller's contract, not the security
// configuration, which has its own tests.
@WebMvcTest(controllers = CustomerRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class CustomerRestControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private ICustomerService customerService;
    @MockBean private CustomerInsertValidator customerInsertValidator;
    @MockBean private CustomerEditValidator customerEditValidator;
    @MockBean private JwtService jwtService;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final UUID UUID_MARIA = UUID.fromString("33333333-3333-3333-3333-333333333333");

    // G.18 — reading one customer returns the whole shape the interface relies on
    @Test
    void getCustomerByUUID_shouldReturn200AndTheCustomer() throws Exception {
        CustomerReadOnlyDTO dto = new CustomerReadOnlyDTO(UUID_MARIA.toString(), "Μαρία",
                "Παπαδοπούλου", "123456789", "maria@test.gr", "6900000001", "ΑΤΤΙΚΗΣ", 2L, "maria",
                new PersonalInfoReadOnlyDTO("ΑΒ123456", "Αθήνα", "Αθήνα", "1988-03-15", "Πατησίων 1", "FEMALE"));
        when(customerService.getCustomerByUUIDDeletedFalse(any())).thenReturn(dto);

        mockMvc.perform(get("/api/v1/customers/{uuid}", UUID_MARIA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname").value("Μαρία"))
                .andExpect(jsonPath("$.vat").value("123456789"))
                // The edit form preselects the region from its id, so both must be present
                .andExpect(jsonPath("$.regionId").value(2))
                .andExpect(jsonPath("$.personalInfo.homeAddress").value("Πατησίων 1"));
    }

    // An unknown or deleted customer is a 404, not an empty 200
    @Test
    void getCustomerByUUID_shouldReturn404_whenMissing() throws Exception {
        when(customerService.getCustomerByUUIDDeletedFalse(any()))
                .thenThrow(new EntityNotFoundException("Customer", "not found"));

        mockMvc.perform(get("/api/v1/customers/{uuid}", UUID_MARIA))
                .andExpect(status().isNotFound());
    }
}
