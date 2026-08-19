package gr.aueb.cf.restbankapp.service;

import gr.aueb.cf.restbankapp.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.restbankapp.dto.CustomerReadOnlyDTO;
import gr.aueb.cf.restbankapp.mapper.Mapper;
import gr.aueb.cf.restbankapp.model.Customer;
import gr.aueb.cf.restbankapp.repository.CustomerRepository;
import gr.aueb.cf.restbankapp.repository.PersonalInfoRepository;
import gr.aueb.cf.restbankapp.repository.RegionRepository;
import gr.aueb.cf.restbankapp.repository.RoleRepository;
import gr.aueb.cf.restbankapp.repository.UserRepository;
import gr.aueb.cf.restbankapp.security.SecurityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock private CustomerRepository customerRepository;
    @Mock private RegionRepository regionRepository;
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PersonalInfoRepository personalInfoRepository;
    @Mock private Mapper mapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private SecurityService securityService;

    @InjectMocks private CustomerServiceImpl service;

    private static final UUID UUID_MARIA = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private CustomerReadOnlyDTO dto() {
        return new CustomerReadOnlyDTO(UUID_MARIA.toString(), "Μαρία", "Παπαδοπούλου", "123456789",
                "maria@test.gr", "6900000001", "ΑΤΤΙΚΗΣ", 2L, "maria", null);
    }

    // G.15 — the happy path: found, mapped, returned
    @Test
    void getCustomerByUUID_shouldReturnTheCustomer() throws Exception {
        Customer customer = new Customer();
        when(customerRepository.findByUuidAndDeletedFalse(UUID_MARIA)).thenReturn(Optional.of(customer));
        when(mapper.mapToCustomerReadOnlyDTO(customer)).thenReturn(dto());

        CustomerReadOnlyDTO result = service.getCustomerByUUIDDeletedFalse(UUID_MARIA);

        assertEquals("Μαρία", result.firstname());
        assertEquals("123456789", result.vat());
    }

    // G.16 — a deleted or unknown customer is not silently returned as null
    @Test
    void getCustomerByUUID_shouldThrowWhenMissing() {
        when(customerRepository.findByUuidAndDeletedFalse(UUID_MARIA)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.getCustomerByUUIDDeletedFalse(UUID_MARIA));
    }
}
