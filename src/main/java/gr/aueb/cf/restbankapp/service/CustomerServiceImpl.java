package gr.aueb.cf.restbankapp.service;

import gr.aueb.cf.restbankapp.core.exceptions.EntityAlreadyExistsException;
import gr.aueb.cf.restbankapp.core.exceptions.EntityInvalidArgumentException;
import gr.aueb.cf.restbankapp.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.restbankapp.core.exceptions.FileUploadException;
//import gr.aueb.cf.restbankapp.core.filters.CustomerFilters;
import gr.aueb.cf.restbankapp.dto.CustomerInsertDTO;
import gr.aueb.cf.restbankapp.dto.CustomerReadOnlyDTO;
//import gr.aueb.cf.restbankapp.dto.CustomerUpdateDTO;
import gr.aueb.cf.restbankapp.mapper.Mapper;
import gr.aueb.cf.restbankapp.model.*;
import gr.aueb.cf.restbankapp.model.static_data.Region;
import gr.aueb.cf.restbankapp.repository.*;
//import gr.aueb.cf.restbankapp.specification.CustomerSpecification;
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
            if (personalInfoRepository.findByAfm(dto.personalInfoInsertDTO().afm()).isPresent()) {
                throw new EntityAlreadyExistsException("AFM", "User with AFM " + dto.personalInfoInsertDTO().afm() + " already exists");
            }

            if (userRepository.findByUsername(dto.userInsertDTO().username()).isPresent()) {
                throw new EntityAlreadyExistsException("Username", "User with username " + dto.userInsertDTO().username() + " already exists");
            }

//            if (personalInfoRepository.findByIdentityNumber(dto.personalInfoInsertDTO().identityNumber()).isPresent()) {
//                throw new EntityAlreadyExistsException("IdentityNumber", "User with identity number " + dto.personalInfoInsertDTO().identityNumber() + " already exists");
//            }

            Region region = regionRepository.findById(dto.regionId())
                    .orElseThrow(() -> new EntityInvalidArgumentException("Region", "Region id=" + dto.regionId() + " invalid"));

            final Long customerRoleId = 3L;    // Πάντα ο ρόλος είναι customer - TODO να αλλάξει το DTO
//            Role role = roleRepository.findById(dto.userInsertDTO().roleId())
//                    .orElseThrow(() -> new EntityInvalidArgumentException("Role","Role id=" + dto.userInsertDTO().roleId() + " invalid"));
            Role role = roleRepository.findById(customerRoleId)
                    .orElseThrow(() -> new EntityInvalidArgumentException("Role","Role id=" + customerRoleId + " invalid"));

            Customer customer = mapper.mapToCustomerEntity(dto);
//            User user = mapper.mapToUserEntity(dto.userInsertDTO());
            User user = customer.getUser();
            user.setPassword(passwordEncoder.encode(dto.userInsertDTO().password()));
            region.addCustomer(customer);
            role.addUser(user);
//            customer.addUser(user); added to mapper TODO
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

//    @Override
//    @PreAuthorize("hasAuthority('EDIT_CUSTOMER')")
//    @Transactional(rollbackFor = { EntityNotFoundException.class, EntityAlreadyExistsException.class, EntityInvalidArgumentException.class} )
//    public CustomerReadOnlyDTO updateCustomer(CustomerUpdateDTO dto)
//            throws EntityNotFoundException, EntityAlreadyExistsException, EntityInvalidArgumentException {
//        try {
//            Customer customer = customerRepository.findByUuid(dto.uuid())
//                    .orElseThrow(() -> new EntityNotFoundException("Customer", "Customer with uuid=" + dto.uuid() + " not found"));
//
//            customer.setFirstname(dto.firstname());
//            customer.setLastname(dto.lastname());
//
//            if (!customer.getVat().equals(dto.vat())) {
//                if (customerRepository.findByVat(dto.vat()).isPresent()) {
//                    throw new EntityAlreadyExistsException("","Customer with vat=" + dto.vat() + " already exists");
//                }
//                customer.setVat(dto.vat());
//            }
//
//            if (!customer.getPersonalInfo().getIdentityNumber().equals(dto.personalInfoUpdateDTO().identityNumber()) &&
//                    personalInfoRepository.findByIdentityNumber(dto.personalInfoUpdateDTO().identityNumber()).isPresent()) {
//                throw new EntityAlreadyExistsException("Customer", "Customer with identity number " + dto.personalInfoUpdateDTO().identityNumber() + " already exists");
//            }
//
//            if (!Objects.equals(dto.regionId(), customer.getRegion().getId())) {
//                Region region = regionRepository.findById(dto.regionId())
//                        .orElseThrow(() -> new EntityInvalidArgumentException("Region","Region id=" + dto.regionId() + " invalid"));
//                Region oldRegion = customer.getRegion();
//                if (oldRegion != null) {
//                    oldRegion.removeCustomer(customer);
//                }
//                region.addCustomer(customer);
//            }
//            // user username and password updated TODO
//            // other features to be updated TODO
//
//            customerRepository.save(customer);    // προαιρετικό αν είναι managed
//            log.info("Customer with uuid={} updated successfully", dto.uuid());
//            return mapper.mapToCustomerReadOnlyDTO(customer);
//        } catch (EntityNotFoundException e) {
//            log.error("Update failed for customer with uuid={}. Customer not found", dto.uuid(), e);
//            throw e;
//        } catch (EntityAlreadyExistsException e) {
//            log.error("Update failed for customer with uuid={}. Customer with vat={} already exists", dto.uuid(), dto.vat(), e);
//            throw e;
//        } catch (EntityInvalidArgumentException e) {
//            log.error("Update failed for customer with uuid={}. Region id={} invalid", dto.uuid(), dto.regionId(), e);
//            throw e;
//        }
//    }

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
            // No save needed if Customer is managed
//            customerRepository.save(customer);
            log.info("Customer with uuid={} deleted successfully", uuid);
            return mapper.mapToCustomerReadOnlyDTO(customer);
        } catch (EntityNotFoundException e) {
            log.error("Update failed for customer with uuid={}. Customer not found", uuid, e);

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
    @Retryable(
            retryFor = { IOException.class, HttpServerErrorException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2, maxDelay = 10000)
    )
    @Transactional(rollbackFor = EntityNotFoundException.class)
    public void saveAfmFile(UUID uuid, MultipartFile afmFile)
            throws FileUploadException, EntityNotFoundException {
        try {
            String originalFilename = afmFile.getOriginalFilename();
            String savedName = UUID.randomUUID() + getFileExtension(originalFilename);

            String uploadDirectory = uploadDir;
            Path filePath = Paths.get(uploadDirectory + savedName);

            Files.createDirectories(filePath.getParent());
            //        Files.write(filePath, afmFile.getBytes());
            afmFile.transferTo(filePath);  // safe for large files, more efficient

            Attachment attachment = new Attachment();
            attachment.setFilename(originalFilename);
            attachment.setSavedName(savedName);
            attachment.setFilePath(filePath.toString());

            Tika tika = new Tika();
            String contentType = tika.detect(afmFile.getBytes());
//            attachment.setContentType(afmFile.getContentType());
            attachment.setContentType(contentType);
            attachment.setExtension(getFileExtension(originalFilename));

            Customer customer = customerRepository.findByUuid(uuid).orElseThrow(()
                    -> new EntityNotFoundException("Customer", "Customer with uuid=" + uuid + " not found."));

            PersonalInfo personalInfo = customer.getPersonalInfo();

            if (personalInfo.getAfmFile() != null) {
                Files.deleteIfExists(Path.of(personalInfo.getAfmFile().getFilePath()));
                personalInfo.removeAfmFile();  // orphanRemoval handles DB deletion
            }

            personalInfo.addAfmFile(attachment);
            customerRepository.save(customer);
            log.info("Attachment for customer with afm={} saved", personalInfo.getAfm());
        } catch (EntityNotFoundException e) {
            log.error("Attachment for customer with afm={} not found", uuid, e);
            throw e;
        } catch (IOException | HttpServerErrorException e) {
            log.error("Error saving attachment for customer with afm={}", uuid, e);
            throw new FileUploadException("CustomerAfm", "Error saving attachment for customer with afm=" + uuid);
        }
    }



//    @Override
//    @PreAuthorize("hasAuthority('VIEW_CUSTOMERS')")
//    @Transactional(readOnly = true)
//    public Page<CustomerReadOnlyDTO> getCustomersPaginatedFiltered(Pageable pageable, CustomerFilters filters)
//            throws EntityNotFoundException {
//        try {
//            if (filters.getUuid() != null) {
//                Customer customer = customerRepository.findByUuid(filters.getUuid())
//                        .orElseThrow(() -> new EntityNotFoundException("Customer", "uuid=" + filters.getUuid() + " not found"));
//                return singleResultPage(customer, pageable);
//            }
//
//            if (filters.getVat() != null) {
//                Customer customer = customerRepository.findByVat(filters.getVat())
//                        .orElseThrow(() -> new EntityNotFoundException("Customer", "vat=" + filters.getVat() + " not found"));
//                return singleResultPage(customer, pageable);
//            }
//
//            if (filters.getAfm() != null) {
//                Customer customer = customerRepository.findByPersonalInfo_Afm(filters.getAfm())
//                        .orElseThrow(() -> new EntityNotFoundException("Customer", "afm=" + filters.getAfm() + " not found"));
//                return singleResultPage(customer, pageable);
//            }
//
//            var filtered = customerRepository.findAll(CustomerSpecification.build(filters), pageable);
//
//            log.debug("Filtered and paginated customers were returned successfully with page={} and size={}", pageable.getPageNumber(),
//                    pageable.getPageSize());
//            return filtered.map(mapper::mapToCustomerReadOnlyDTO);
//        } catch (EntityNotFoundException e) {
//            log.error("Filtered and paginated customers were not found", e);
//            throw e;
//        }
//    }

    private String getFileExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf("."));
        }
        return "";
    }

    private Page<CustomerReadOnlyDTO> singleResultPage(Customer customer, Pageable pageable) {
        return new PageImpl<>(
                List.of(mapper.mapToCustomerReadOnlyDTO(customer)),
                pageable,
                1
        );
    }
}
