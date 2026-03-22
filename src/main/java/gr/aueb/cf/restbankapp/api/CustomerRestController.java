package gr.aueb.cf.restbankapp.api;

import gr.aueb.cf.restbankapp.core.exceptions.*;
import gr.aueb.cf.restbankapp.core.filters.CustomerFilters;
import gr.aueb.cf.restbankapp.dto.CustomerInsertDTO;
import gr.aueb.cf.restbankapp.dto.CustomerReadOnlyDTO;
import gr.aueb.cf.restbankapp.dto.CustomerUpdateDTO;
import gr.aueb.cf.restbankapp.service.ICustomerService;
import gr.aueb.cf.restbankapp.validation.CustomerInsertValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/customers")
@RequiredArgsConstructor
public class CustomerRestController {

    private final ICustomerService customerService;
    private final CustomerInsertValidator customerInsertValidator;

    @PostMapping
    public ResponseEntity<CustomerReadOnlyDTO> saveCustomer(
            @Valid @RequestBody CustomerInsertDTO customerInsertDTO,
            BindingResult bindingResult)
    throws EntityAlreadyExistsException, EntityInvalidArgumentException, ValidationException {

        if (bindingResult.hasErrors()) {
            throw new ValidationException("Customer", "Invalid customer data", bindingResult);
        }

        customerInsertValidator.validate(customerInsertDTO, bindingResult);

        CustomerReadOnlyDTO customerReadOnlyDTO = customerService.saveCustomer(customerInsertDTO);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{uuid}")
                .buildAndExpand(customerReadOnlyDTO.uuid())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(customerReadOnlyDTO);
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<CustomerReadOnlyDTO> updateCustomer(
            @PathVariable UUID uuid,
            @Valid @RequestBody CustomerUpdateDTO customerUpdateDTO,
            BindingResult bindingResult)
            throws EntityAlreadyExistsException, EntityInvalidArgumentException, ValidationException, EntityNotFoundException {

        if (bindingResult.hasErrors()) {
            throw new ValidationException("Customer", "Invalid customer data", bindingResult);
        }

        //customerUpdateValidator.validate(customerUpdateDTO, bindingResult);

        CustomerReadOnlyDTO customerReadOnlyDTO = customerService.updateCustomer(customerUpdateDTO);

        return ResponseEntity.ok(customerReadOnlyDTO);
    }

    @PostMapping("/{uuid}/afm-file")
    public ResponseEntity<Void> uploadAfmFile(
            @PathVariable UUID uuid,
            @RequestParam("afmFile") MultipartFile afmFile
    ) throws EntityNotFoundException, FileUploadException {

        customerService.saveAfmFile(uuid, afmFile);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<CustomerReadOnlyDTO>> getFilteredAndPaginatedTeachers(
            @PageableDefault(page = 0, size = 5) Pageable pageable,
            @ModelAttribute CustomerFilters filters // instantiates filters with NoArgsConstructor
            ) throws EntityNotFoundException {
        Page<CustomerReadOnlyDTO> paginatedDTO = customerService.getCustomersPaginatedFiltered(pageable, filters);
        return ResponseEntity.ok(paginatedDTO);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<CustomerReadOnlyDTO> getCustomerByUUID(@PathVariable UUID uuid)
        throws EntityNotFoundException {
        CustomerReadOnlyDTO customerReadOnlyDTO = customerService.getCustomerByUUIDDeletedFalse(uuid);
        return ResponseEntity.ok(customerReadOnlyDTO);
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<CustomerReadOnlyDTO> deleteCustomerByUUID(@PathVariable UUID uuid)
            throws EntityNotFoundException {
        CustomerReadOnlyDTO customerReadOnlyDTO = customerService.deleteCustomerByUUID(uuid);
        return ResponseEntity.ok(customerReadOnlyDTO);
    }
}
