package gr.aueb.cf.restbankapp.service;

import gr.aueb.cf.restbankapp.mapper.Mapper;
import gr.aueb.cf.restbankapp.repository.CustomerRepository;
import gr.aueb.cf.restbankapp.repository.PersonalInfoRepository;
import gr.aueb.cf.restbankapp.repository.RegionRepository;
import gr.aueb.cf.restbankapp.repository.RoleRepository;
import gr.aueb.cf.restbankapp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Verifies the service-layer authorization on {@link CustomerServiceImpl#resetPassword}.
 * The rule ({@code @PreAuthorize("hasRole('ADMIN')")}) only takes effect when the real
 * bean is wrapped by the method-security AOP proxy, so this test loads the actual
 * service bean (not a mock) with its dependencies mocked. Same pattern as G.22.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        CustomerServiceImpl.class,
        CustomerServiceImplSecurityTest.TestMethodSecurityConfig.class
})
@TestPropertySource(properties = "file.upload.dir=build/tmp/test-uploads")
class CustomerServiceImplSecurityTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {
    }

    @MockBean private CustomerRepository customerRepository;
    @MockBean private RegionRepository regionRepository;
    @MockBean private UserRepository userRepository;
    @MockBean private RoleRepository roleRepository;
    @MockBean private PersonalInfoRepository personalInfoRepository;
    @MockBean private Mapper mapper;
    @MockBean private PasswordEncoder passwordEncoder;

    // Autowire the interface: the method-security proxy is a JDK dynamic proxy of
    // ICustomerService, not a CustomerServiceImpl subclass.
    @Autowired
    private ICustomerService customerService;

    private final UUID uuid = UUID.randomUUID();

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void resetPassword_shouldThrowAccessDenied_forNonAdmin() {
        assertThrows(AccessDeniedException.class,
                () -> customerService.resetPassword(uuid, "NewPass123!"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void resetPassword_shouldPassAuthorization_forAdmin() {
        // The mocked repository returns an empty Optional, so the body throws
        // EntityNotFoundException — anything other than AccessDeniedException proves
        // that @PreAuthorize let the ADMIN through.
        try {
            customerService.resetPassword(uuid, "NewPass123!");
        } catch (AccessDeniedException e) {
            throw e; // must NOT happen for an ADMIN
        } catch (Exception expectedNonSecurity) {
            // EntityNotFoundException from the empty mock — acceptable
        }
    }
}
