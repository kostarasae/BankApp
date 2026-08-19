package gr.aueb.cf.restbankapp.validator;

import gr.aueb.cf.restbankapp.dto.UserInsertDTO;
import gr.aueb.cf.restbankapp.service.IUserService;
import gr.aueb.cf.restbankapp.validation.UserInsertValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserInsertValidatorTest {

    @Mock private IUserService userService;
    @InjectMocks private UserInsertValidator validator;

    // G.11 — two people cannot share a username, and only the database knows that
    @Test
    void shouldRejectAUsernameAlreadyTaken() {
        when(userService.isUserExists("maria")).thenReturn(true);

        UserInsertDTO dto = new UserInsertDTO("maria", "Test1234!", 3L);
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(dto, "userInsertDTO");

        validator.validate(dto, errors);

        assertTrue(errors.hasErrors());
        assertNotNull(errors.getFieldError("username"));
    }

    @Test
    void shouldAcceptAFreeUsername() {
        when(userService.isUserExists("giannis")).thenReturn(false);

        UserInsertDTO dto = new UserInsertDTO("giannis", "Test1234!", 3L);
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(dto, "userInsertDTO");

        validator.validate(dto, errors);

        assertFalse(errors.hasErrors());
    }
}
