package gr.aueb.cf.restbankapp.validator;

import gr.aueb.cf.restbankapp.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.restbankapp.dto.CustomerInsertDTO;
import gr.aueb.cf.restbankapp.dto.CustomerReadOnlyDTO;
import gr.aueb.cf.restbankapp.dto.CustomerUpdateDTO;
import gr.aueb.cf.restbankapp.dto.PersonalInfoInsertDTO;
import gr.aueb.cf.restbankapp.dto.PersonalInfoUpdateDTO;
import gr.aueb.cf.restbankapp.dto.UserInsertDTO;
import gr.aueb.cf.restbankapp.dto.UserUpdateDTO;
import gr.aueb.cf.restbankapp.service.ICustomerService;
import gr.aueb.cf.restbankapp.validation.CustomerEditValidator;
import gr.aueb.cf.restbankapp.validation.CustomerInsertValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * The validators are what stop two customers ending up with the same VAT. Bean
 * validation cannot do this on its own — it has no way to ask the database.
 */
@ExtendWith(MockitoExtension.class)
class CustomerValidatorsTest {

    @Mock private ICustomerService customerService;

    @InjectMocks private CustomerInsertValidator insertValidator;
    @InjectMocks private CustomerEditValidator editValidator;

    private static final UUID UUID_MARIA = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private CustomerInsertDTO insertWithVat(String vat) {
        return new CustomerInsertDTO("Μαρία", "Παπαδοπούλου", vat, "maria@test.gr", "6900000001", 2L,
                new UserInsertDTO("maria", "Test1234!", 3L),
                new PersonalInfoInsertDTO("ΑΒ123456", "Αθήνα", "1988-03-15", "Αθήνα", "Πατησίων 1", "FEMALE"));
    }

    private CustomerUpdateDTO updateWithVat(String vat) {
        return new CustomerUpdateDTO(UUID_MARIA, "Μαρία", "Παπαδοπούλου", vat, "maria@test.gr",
                "6900000001", 2L,
                new UserUpdateDTO("maria", null),
                new PersonalInfoUpdateDTO("ΑΒ123456", "Αθήνα", "Αθήνα", "1988-03-15", "Πατησίων 1", "FEMALE"));
    }

    private CustomerReadOnlyDTO existing(String vat) {
        return new CustomerReadOnlyDTO(UUID_MARIA.toString(), "Μαρία", "Παπαδοπούλου", vat,
                "maria@test.gr", "6900000001", "ΑΤΤΙΚΗΣ", 2L, "maria", null);
    }

    // G.10 — registering with a VAT someone already has must be refused
    @Test
    void insert_shouldRejectAVatThatIsAlreadyTaken() {
        when(customerService.isCustomerExists("123456789")).thenReturn(true);

        CustomerInsertDTO dto = insertWithVat("123456789");
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(dto, "customerInsertDTO");

        insertValidator.validate(dto, errors);

        assertTrue(errors.hasErrors());
        assertNotNull(errors.getFieldError("vat"));
    }

    @Test
    void insert_shouldAcceptAFreeVat() {
        when(customerService.isCustomerExists(anyString())).thenReturn(false);

        CustomerInsertDTO dto = insertWithVat("999999999");
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(dto, "customerInsertDTO");

        insertValidator.validate(dto, errors);

        assertFalse(errors.hasErrors());
    }

    // G.12 — keeping your own VAT while editing is not a duplicate. Without this
    // check nobody could ever save the form without also changing their VAT.
    @Test
    void edit_shouldAllowKeepingYourOwnVat() throws Exception {
        when(customerService.getCustomerByUUIDDeletedFalse(UUID_MARIA)).thenReturn(existing("123456789"));

        CustomerUpdateDTO dto = updateWithVat("123456789");
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(dto, "customerUpdateDTO");

        editValidator.validate(dto, errors);

        assertFalse(errors.hasErrors());
    }

    // G.13 — but moving to someone else's VAT is still a duplicate
    @Test
    void edit_shouldRejectMovingToSomeoneElsesVat() throws Exception {
        when(customerService.getCustomerByUUIDDeletedFalse(UUID_MARIA)).thenReturn(existing("123456789"));
        when(customerService.isCustomerExists("987654321")).thenReturn(true);

        CustomerUpdateDTO dto = updateWithVat("987654321");
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(dto, "customerUpdateDTO");

        editValidator.validate(dto, errors);

        assertTrue(errors.hasErrors());
        assertNotNull(errors.getFieldError("vat"));
    }

    @Test
    void edit_shouldReportAnUnknownCustomer() throws Exception {
        when(customerService.getCustomerByUUIDDeletedFalse(UUID_MARIA))
                .thenThrow(new EntityNotFoundException("Customer", "not found"));

        CustomerUpdateDTO dto = updateWithVat("123456789");
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(dto, "customerUpdateDTO");

        editValidator.validate(dto, errors);

        assertTrue(errors.hasErrors());
        assertNotNull(errors.getFieldError("uuid"));
    }
}
