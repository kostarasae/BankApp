package gr.aueb.cf.restbankapp.validation;

import gr.aueb.cf.restbankapp.dto.CustomerInsertDTO;
import gr.aueb.cf.restbankapp.service.ICustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerInsertValidator implements Validator {

    private final ICustomerService customerService;

    @Override
    public boolean supports(Class<?> clazz) {
        return CustomerInsertDTO.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        CustomerInsertDTO customerInsertDTO = (CustomerInsertDTO) target;

        if (customerInsertDTO.vat() != null && customerService.isCustomerExists(customerInsertDTO.vat())) {
            log.warn("Save failed. Customer with vat={} already exists", customerInsertDTO.vat());
            errors.rejectValue("vat", "customer.vat.exists", "Customer with vat=" + customerInsertDTO.vat() + " already exists");
        }
    }
}
