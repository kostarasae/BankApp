package gr.aueb.cf.restbankapp.validation;

import gr.aueb.cf.restbankapp.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.restbankapp.dto.CustomerReadOnlyDTO;
import gr.aueb.cf.restbankapp.dto.CustomerUpdateDTO;
import gr.aueb.cf.restbankapp.service.ICustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerEditValidator implements Validator {

    private final ICustomerService customerService;

    @Override
    public boolean supports(Class<?> clazz) {
        return CustomerUpdateDTO.class == clazz;
    }

    @Override
    public void validate(Object target, Errors errors) {
        CustomerUpdateDTO customerUpdateDTO = (CustomerUpdateDTO) target;

        try {
            CustomerReadOnlyDTO readOnlyDTO = customerService.getCustomerByUUIDDeletedFalse(customerUpdateDTO.uuid());

            if (readOnlyDTO != null && !readOnlyDTO.vat().equals(customerUpdateDTO.vat())) {
                if (customerService.isCustomerExists(customerUpdateDTO.vat())) {
                    log.warn("Update failed. Customer with vat={} already exists", customerUpdateDTO.vat());
                    errors.rejectValue("vat", "vat.customer.exists");
                }
            }
        } catch (EntityNotFoundException e) {
            log.warn("Update failed. Customer with uuid={} not found", customerUpdateDTO.uuid());
            errors.rejectValue("uuid", "uuid.customer.notfound");
        }
    }
}
