package gr.aueb.cf.restbankapp.service;

import gr.aueb.cf.restbankapp.core.exceptions.EntityAlreadyExistsException;
import gr.aueb.cf.restbankapp.core.exceptions.EntityInvalidArgumentException;
import gr.aueb.cf.restbankapp.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.restbankapp.core.exceptions.FileUploadException;
import gr.aueb.cf.restbankapp.core.filters.CustomerFilters;
import gr.aueb.cf.restbankapp.dto.CustomerInsertDTO;
import gr.aueb.cf.restbankapp.dto.CustomerReadOnlyDTO;
import gr.aueb.cf.restbankapp.dto.CustomerUpdateDTO;
import gr.aueb.cf.restbankapp.dto.AccountReadOnlyDTO;
import gr.aueb.cf.restbankapp.mapper.Mapper;
import gr.aueb.cf.restbankapp.model.*;
import gr.aueb.cf.restbankapp.model.static_data.Region;
import gr.aueb.cf.restbankapp.repository.*;
import gr.aueb.cf.restbankapp.specification.CustomerSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service                        // IoC Container
@RequiredArgsConstructor        // DI
@Slf4j                          // Logger
public class CustomerServiceImpl implements ICustomerService {

    private final CustomerRepository customerRepository;
    private final RegionRepository regionRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PersonalInfoRepository personalInfoRepository;
    private final Mapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${file.upload.dir}")
    private String uploadDir;

    @Override
    @Transactional(rollbackFor = { EntityAlreadyExistsException.class, EntityInvalidArgumentException.class } )
    public CustomerReadOnlyDTO saveCustomer(CustomerInsertDTO dto)
            throws EntityAlreadyExistsException, EntityInvalidArgumentException {

        try {
            if (dto.vat() != null && customerRepository.findByVat(dto.vat()).isPresent()) {
                throw new EntityAlreadyExistsException("Customer", "Customer with vat=" + dto.vat() + " already exists");
            }
            if (userRepository.findByUsername(dto.userInsertDTO().username()).isPresent()) {
                throw new EntityAlreadyExistsException("Username", "User with username " + dto.userInsertDTO().username() + " already exists");
            }
            if (personalInfoRepository.findByIdNumber(dto.personalInfoInsertDTO().idNumber()).isPresent()) {
                throw new EntityAlreadyExistsException("IdNumber", "User with ID number " + dto.personalInfoInsertDTO().idNumber() + " already exists");
            }

            Region region = regionRepository.findById(dto.regionId())
                    .orElseThrow(() -> new EntityInvalidArgumentException("Region", "Region id=" + dto.regionId() + " invalid"));

            Role role = roleRepository.findById(dto.userInsertDTO().roleId())
                    .orElseThrow(() -> new EntityInvalidArgumentException("Role","Role id=" + dto.userInsertDTO().roleId() + " invalid"));

            Customer customer = mapper.mapToCustomerEntity(dto);
            User user = mapper.mapToUserEntity(dto.userInsertDTO());
            user.setPassword(passwordEncoder.encode(dto.userInsertDTO().password()));
            region.addCustomer(customer);
            role.addUser(user);
            customer.addUser(user); // added to mapper TODO
            customerRepository.save(customer);        // saved customer
            log.info("Customer with vat={} saved successfully.", dto.vat());
            return mapper.mapToCustomerReadOnlyDTO(customer);
        } catch (EntityAlreadyExistsException e) {
            log.error("Save failed for customer with vat={}. Customer already exists", dto.vat(), e);     // Structured Logging
            throw e;
        } catch (EntityInvalidArgumentException e) {
            log.error("Save failed for customer with vat={} and region id={} invalid", dto.vat(), dto.regionId());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCustomerExists(String vat) {
        return customerRepository.findByVat(vat).isPresent();
    }

    @Override
    @PreAuthorize("hasAuthority('VIEW_CUSTOMERS')")
    @Transactional(readOnly = true)
    public Page<CustomerReadOnlyDTO> getPaginatedCustomers(Pageable pageable) {
        Page<Customer> customersPage = customerRepository.findAll(pageable);
        log.debug("Get paginated returned successfully page={} and size={}", customersPage.getNumber(), customersPage.getSize());
        return customersPage.map(mapper::mapToCustomerReadOnlyDTO);
    }

    @Override
    @PreAuthorize("hasAuthority('VIEW_CUSTOMERS')")
    @Transactional(readOnly = true)
    public Page<CustomerReadOnlyDTO> getPaginatedCustomersDeletedFalse(Pageable pageable) {
        Page<Customer> customersPage = customerRepository.findAllByDeletedFalse(pageable);
        log.debug("Get paginated not deleted returned successfully page={} and size={}", customersPage.getNumber(), customersPage.getSize());
        return customersPage.map(mapper::mapToCustomerReadOnlyDTO);
    }

    @Override
    @PreAuthorize("hasAuthority('EDIT_CUSTOMER')")
    @Transactional(rollbackFor = { EntityNotFoundException.class, EntityAlreadyExistsException.class, EntityInvalidArgumentException.class} )
    public CustomerReadOnlyDTO updateCustomer(CustomerUpdateDTO dto)
            throws EntityNotFoundException, EntityAlreadyExistsException, EntityInvalidArgumentException {
        try {
            Customer customer = customerRepository.findByUuid(dto.uuid())
                    .orElseThrow(() -> new EntityNotFoundException("Customer", "Customer with uuid=" + dto.uuid() + " not found"));

            customer.setFirstname(dto.firstname());
            customer.setLastname(dto.lastname());

            if (!customer.getVat().equals(dto.vat())) {
                if (customerRepository.findByVat(dto.vat()).isPresent()) {
                    throw new EntityAlreadyExistsException("","Customer with vat=" + dto.vat() + " already exists");
                }
                customer.setVat(dto.vat());
            }

            if (!customer.getPersonalInfo().getIdNumber().equals(dto.personalInfoUpdateDTO().idNumber()) &&
                    personalInfoRepository.findByIdNumber(dto.personalInfoUpdateDTO().idNumber()).isPresent()) {
                throw new EntityAlreadyExistsException("Customer", "Customer with ID number " + dto.personalInfoUpdateDTO().idNumber() + " already exists");
            }

            if (!Objects.equals(dto.regionId(), customer.getRegion().getId())) {
                Region region = regionRepository.findById(dto.regionId())
                        .orElseThrow(() -> new EntityInvalidArgumentException("Region","Region id=" + dto.regionId() + " invalid"));
                Region oldRegion = customer.getRegion();
                if (oldRegion != null) {
                    oldRegion.removeCustomer(customer);
                }
                region.addCustomer(customer);
            }
            // user username and password updated TODO
            // other features to be updated TODO

            customerRepository.save(customer);    // προαιρετικό αν είναι managed
            log.info("Customer with uuid={} updated successfully", dto.uuid());
            return mapper.mapToCustomerReadOnlyDTO(customer);
        } catch (EntityNotFoundException e) {
            log.error("Update failed for customer with uuid={}. Customer not found", dto.uuid(), e);
            throw e;
        } catch (EntityAlreadyExistsException e) {
            log.error("Update failed for customer with uuid={}. Customer with vat={} already exists", dto.uuid(), dto.vat(), e);
            throw e;
        } catch (EntityInvalidArgumentException e) {
            log.error("Update failed for customer with uuid={}. Region id={} invalid", dto.uuid(), dto.regionId(), e);
            throw e;
        }
    }

    @Override
    @PreAuthorize("hasAuthority('DELETE_CUSTOMER')")
    @Transactional(rollbackFor = EntityNotFoundException.class)
    public CustomerReadOnlyDTO deleteCustomerByUUID(UUID uuid) throws EntityNotFoundException {
        try {
            Customer customer = customerRepository.findByUuidAndDeletedFalse(uuid)
                    .orElseThrow(() -> new EntityNotFoundException("Customer","Customer with uuid=" + uuid + " not found"));

            customer.softDelete();
            customer.getPersonalInfo().softDelete();
            customer.getUser().softDelete();
//            customerRepository.save(customer); // No save needed if Customer is managed
            log.info("Customer with uuid={} deleted successfully", uuid);
            return mapper.mapToCustomerReadOnlyDTO(customer);
        } catch (EntityNotFoundException e) {
            log.error("Delete failed for customer with uuid={}. Customer not found", uuid, e);
            // Automatic rollback due to @Transactional annotation
            throw e;
        }
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public CustomerReadOnlyDTO getCustomerByUUID(UUID uuid) throws EntityNotFoundException {

        try {
            Customer customer = customerRepository.findByUuid(uuid)
                    .orElseThrow(() -> new EntityNotFoundException("Customer","Customer with uuid=" + uuid + " not found"));
            log.debug("Get customer by uuid={} returned successfully", uuid);
            return mapper.mapToCustomerReadOnlyDTO(customer);
        } catch (EntityNotFoundException e) {
            log.error("Get customer by uuid={} failed", uuid, e);
            throw e;
        }
    }

    @Override
    @PreAuthorize("hasAuthority('VIEW_CUSTOMER') or (hasAuthority('VIEW_ONLY_CUSTOMER') and @securityService.isOwnCustomerProfile(#uuid, authentication))")
    @Transactional(readOnly = true)
    public CustomerReadOnlyDTO getCustomerByUUIDDeletedFalse(UUID uuid) throws EntityNotFoundException {
        try {
            Customer customer = customerRepository.findByUuidAndDeletedFalse(uuid)
                    .orElseThrow(() -> new EntityNotFoundException("Customer","Customer with uuid=" + uuid + " not found"));
            log.debug("Get non-deleted customer by uuid={} returned successfully", uuid);
            return mapper.mapToCustomerReadOnlyDTO(customer);
        } catch (EntityNotFoundException e) {
            log.error("Get customer by uuid={} failed", uuid, e);
            throw e;
        }
    }

    @Override
    @PreAuthorize("hasAuthority('VIEW_CUSTOMERS')")
    @Transactional(readOnly = true)
    public Page<CustomerReadOnlyDTO> getCustomersPaginatedFiltered(Pageable pageable, CustomerFilters filters)
            throws EntityNotFoundException {
        try {
            if (filters.getUuid() != null) {
                Customer customer = customerRepository.findByUuid(filters.getUuid())
                        .orElseThrow(() -> new EntityNotFoundException("Customer", "uuid=" + filters.getUuid() + " not found"));
                return singleResultPage(customer, pageable);
            }

            if (filters.getVat() != null) {
                Customer customer = customerRepository.findByVat(filters.getVat())
                        .orElseThrow(() -> new EntityNotFoundException("Customer", "vat=" + filters.getVat() + " not found"));
                return singleResultPage(customer, pageable);
            }

            if (filters.getIdNumber() != null) {
                Customer customer = customerRepository.findByPersonalInfo_IdNumber(filters.getIdNumber())
                        .orElseThrow(() -> new EntityNotFoundException("Customer", "idNumber=" + filters.getIdNumber() + " not found"));
                return singleResultPage(customer, pageable);
            }

            var filtered = customerRepository.findAll(CustomerSpecification.build(filters), pageable);

            log.debug("Filtered and paginated customers were returned successfully with page={} and size={}", pageable.getPageNumber(),
                    pageable.getPageSize());
            return filtered.map(mapper::mapToCustomerReadOnlyDTO);
        } catch (EntityNotFoundException e) {
            log.error("Filtered and paginated customers were not found", e);
            throw e;
        }
    }

    private Page<CustomerReadOnlyDTO> singleResultPage(Customer customer, Pageable pageable) {
        return new PageImpl<>(
                List.of(mapper.mapToCustomerReadOnlyDTO(customer)),
                pageable,
                1
        );
    }

    @Override
    @Retryable(
            retryFor = { IOException.class, HttpServerErrorException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2, maxDelay = 10000)
    )
    @Transactional(rollbackFor = EntityNotFoundException.class)
    public void saveIdFile(UUID uuid, MultipartFile idFile)
            throws FileUploadException, EntityNotFoundException {
        try {
            String originalFilename = idFile.getOriginalFilename();
            String savedName = UUID.randomUUID() + getFileExtension(originalFilename);

            String uploadDirectory = uploadDir;
            Path filePath = Paths.get(uploadDirectory + savedName);

            Files.createDirectories(filePath.getParent());
            idFile.transferTo(filePath);  // safe for large files, more efficient

            Attachment attachment = new Attachment();
            attachment.setFilename(originalFilename);
            attachment.setSavedName(savedName);
            attachment.setFilePath(filePath.toString());

            Tika tika = new Tika();
            String contentType = tika.detect(idFile.getBytes());
            attachment.setContentType(contentType);
            attachment.setExtension(getFileExtension(originalFilename));

            Customer customer = customerRepository.findByUuid(uuid).orElseThrow(()
                    -> new EntityNotFoundException("Customer", "Customer with uuid=" + uuid + " not found."));

            PersonalInfo personalInfo = customer.getPersonalInfo();

            if (personalInfo.getIdFile() != null) {
                Files.deleteIfExists(Path.of(personalInfo.getIdFile().getFilePath()));
                personalInfo.removeIdFile();  // orphanRemoval handles DB deletion
            }

            personalInfo.addIdFile(attachment);
            customerRepository.save(customer);
            log.info("ID file for customer uuid={} saved", uuid);
        } catch (EntityNotFoundException e) {
            log.error("Customer with uuid={} not found when saving ID file", uuid, e);
            throw e;
        } catch (IOException | HttpServerErrorException e) {
            log.error("Error saving ID file for customer uuid={}", uuid, e);
            throw new FileUploadException("CustomerIdFile", "Error saving ID file for customer with uuid=" + uuid);
        }
    }

    private String getFileExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf("."));
        }
        return "";
    }

    @Override
    public List<AccountReadOnlyDTO> getCustomerAccountsNotDeleted(String uuid) throws EntityNotFoundException {
        try {
            Customer customer = customerRepository.findByUuidAndDeletedFalse(UUID.fromString(uuid))
                    .orElseThrow(() -> new EntityNotFoundException("Customer","Customer with uuid=" + uuid + " not found"));
            log.debug("Get accounts for customer uuid={} returned successfully", uuid);
            return customer.getAccounts().stream()
                    .filter(account -> !account.isDeleted())
                    .map(mapper::mapToAccountReadOnlyDTO)
                    .toList();
        } catch (EntityNotFoundException e) {
            log.error("Get accounts for customer uuid={} failed. Customer not found", uuid, e);
            throw e;
        }
    }
}
