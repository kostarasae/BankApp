package gr.aueb.cf.restbankapp.service;

import gr.aueb.cf.restbankapp.core.exceptions.EntityAlreadyExistsException;
import gr.aueb.cf.restbankapp.core.exceptions.EntityInvalidArgumentException;
import gr.aueb.cf.restbankapp.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.restbankapp.core.exceptions.FileUploadException;
import gr.aueb.cf.restbankapp.core.filters.CustomerFilters;
import gr.aueb.cf.restbankapp.dto.CustomerInsertDTO;
import gr.aueb.cf.restbankapp.dto.CustomerReadOnlyDTO;
import gr.aueb.cf.restbankapp.dto.CustomerUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface ICustomerService {

    CustomerReadOnlyDTO saveCustomer(CustomerInsertDTO customerInsertDTO)
            throws EntityAlreadyExistsException, EntityInvalidArgumentException;

    CustomerReadOnlyDTO updateCustomer(CustomerUpdateDTO customerUpdateDTO)
            throws EntityNotFoundException, EntityAlreadyExistsException, EntityInvalidArgumentException;

    CustomerReadOnlyDTO deleteCustomerByUUID(UUID uuid) throws EntityNotFoundException;

    CustomerReadOnlyDTO getCustomerByUUID(UUID uuid) throws EntityNotFoundException;
    CustomerReadOnlyDTO getCustomerByUUIDDeletedFalse(UUID uuid) throws EntityNotFoundException;

    Page<CustomerReadOnlyDTO> getPaginatedCustomers(Pageable pageable);
    Page<CustomerReadOnlyDTO> getPaginatedCustomersDeletedFalse(Pageable pageable);
    Page<CustomerReadOnlyDTO> getCustomersPaginatedFiltered(Pageable pageable, CustomerFilters filters)
            throws EntityNotFoundException;
    boolean isCustomerExists(String vat);

    void saveAfmFile(UUID uuid, MultipartFile afmFile)
            throws FileUploadException, EntityNotFoundException;
}