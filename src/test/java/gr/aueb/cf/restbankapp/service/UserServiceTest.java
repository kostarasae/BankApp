package gr.aueb.cf.restbankapp.service;

import gr.aueb.cf.restbankapp.core.exceptions.EntityAlreadyExistsException;
import gr.aueb.cf.restbankapp.dto.UserInsertDTO;
import gr.aueb.cf.restbankapp.mapper.Mapper;
import gr.aueb.cf.restbankapp.model.Role;
import gr.aueb.cf.restbankapp.model.User;
import gr.aueb.cf.restbankapp.repository.RoleRepository;
import gr.aueb.cf.restbankapp.repository.UserRepository;
import gr.aueb.cf.restbankapp.security.SecurityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private Mapper mapper;
    @Mock private SecurityService securityService;

    @InjectMocks private UserService service;

    private Role customerRole() {
        Role role = new Role();
        role.setName("CUSTOMER");
        return role;
    }

    // G.17 — a password must never reach the database as typed
    @Test
    void saveUser_shouldStoreThePasswordEncoded() throws Exception {
        when(userRepository.findByUsernameAndDeletedFalse(anyString())).thenReturn(Optional.empty());
        // The mapper is mocked, so without this it hands back null and the service
        // trips over it before ever reaching the password.
        when(mapper.mapToUserEntity(any())).thenReturn(new User());
        when(roleRepository.findById(3L)).thenReturn(Optional.of(customerRole()));
        when(passwordEncoder.encode("Test1234!")).thenReturn("$2a$12$hashed");

        service.saveUser(new UserInsertDTO("nikos", "Test1234!", 3L));

        // Capture what was actually handed to the repository and look at it.
        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());

        assertEquals("$2a$12$hashed", saved.getValue().getPassword());
        assertNotEquals("Test1234!", saved.getValue().getPassword());
    }

    @Test
    void saveUser_shouldRefuseAUsernameAlreadyTaken() {
        when(userRepository.findByUsernameAndDeletedFalse("nikos"))
                .thenReturn(Optional.of(new User()));

        assertThrows(EntityAlreadyExistsException.class,
                () -> service.saveUser(new UserInsertDTO("nikos", "Test1234!", 3L)));

        verify(userRepository, never()).save(any());
    }
}
