package gr.aueb.cf.restbankapp.api;

import gr.aueb.cf.restbankapp.core.exceptions.*;
import gr.aueb.cf.restbankapp.core.filters.CustomerFilters;
import gr.aueb.cf.restbankapp.dto.*;
import gr.aueb.cf.restbankapp.service.ICustomerService;
import gr.aueb.cf.restbankapp.validation.CustomerInsertValidator;
import gr.aueb.cf.restbankapp.validation.CustomerEditValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import org.springframework.security.access.prepost.PreAuthorize;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Customer records: registration, details, accounts and identity documents.")
public class CustomerRestController {

    private final ICustomerService customerService;
    private final CustomerInsertValidator customerInsertValidator;
    private final CustomerEditValidator customerEditValidator;

    @Operation(
            summary = "Save a customer",
            description = "Registers a new customer in the system"
    )
            @ApiResponses({
                    @ApiResponse(
                            responseCode = "201", description = "Customer created",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomerReadOnlyDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "409", description = "Customer already exists",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "500", description = "Internal Server Error",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "400", description = "Validation error",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponseDTO.class))
                    )
            }
    )

    @PostMapping
    public ResponseEntity<CustomerReadOnlyDTO> saveCustomer(
            @Valid @RequestBody CustomerInsertDTO customerInsertDTO,
            BindingResult bindingResult)
            throws EntityAlreadyExistsException, EntityInvalidArgumentException, ValidationException {

        if (bindingResult.hasErrors()) {
            throw new ValidationException("Customer", "Invalid customer data", bindingResult);
        }

        customerInsertValidator.validate(customerInsertDTO, bindingResult);

        if (bindingResult.hasErrors()) {
            throw new ValidationException("Customer", "Invalid customer data", bindingResult);
        }

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

    @Operation(summary = "Update a customer")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "Customer updated",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomerReadOnlyDTO.class))
            ),
            @ApiResponse(
                    responseCode = "409", description = "Customer already exists",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "Customer not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidationErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401", description = "Not Authenticated",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "403", description = "Access Denied",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))
            )
    })

    @PutMapping("/{uuid}")
    public ResponseEntity<CustomerReadOnlyDTO> updateCustomer(
            @PathVariable UUID uuid,
            @Valid @RequestBody CustomerUpdateDTO customerUpdateDTO,
            BindingResult bindingResult)
            throws EntityAlreadyExistsException, EntityInvalidArgumentException, ValidationException, EntityNotFoundException {

        if (bindingResult.hasErrors()) {
            throw new ValidationException("Customer", "Invalid customer data", bindingResult);
        }

        customerEditValidator.validate(customerUpdateDTO, bindingResult);

        if (bindingResult.hasErrors()) {
            throw new ValidationException("Customer", "Invalid customer data", bindingResult);
        }

        CustomerReadOnlyDTO customerReadOnlyDTO = customerService.updateCustomer(customerUpdateDTO);

        return ResponseEntity.ok(customerReadOnlyDTO);
    }

    @Operation(
            summary = "Upload identity card file for a customer",
            description = "Uploads a customer's identity card document. Replaces existing file if present."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "File uploaded successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Customer not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "File upload failed",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })

    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/{uuid}/id-file")
    public ResponseEntity<Void> uploadIdFile(
            @PathVariable UUID uuid,
            @RequestParam("idFile") MultipartFile idFile
    ) throws EntityNotFoundException, FileUploadException {

        customerService.saveIdFile(uuid, idFile);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all customers paginated and filtered")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "Customers returned",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                    responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "403", description = "Access Denied",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    })

    @GetMapping
    public ResponseEntity<Page<CustomerReadOnlyDTO>> getFilteredAndPaginatedCustomers(
            @PageableDefault(page = 0, size = 5) Pageable pageable,
            @ModelAttribute CustomerFilters filters // instantiates filters with NoArgsConstructor
            ) throws EntityNotFoundException {
        Page<CustomerReadOnlyDTO> paginatedDTO = customerService.getCustomersPaginatedFiltered(pageable, filters);
        return ResponseEntity.ok(paginatedDTO);
    }

    @Operation(summary = "Get one customer by uuid")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "Customer returned",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomerReadOnlyDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404", description = "Customer not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401", description = "Not Authenticated",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "403", description = "Access Denied",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))
            )
    })

    @GetMapping("/{uuid}")
    public ResponseEntity<CustomerReadOnlyDTO> getCustomerByUUID(@PathVariable UUID uuid)
        throws EntityNotFoundException {
        CustomerReadOnlyDTO customerReadOnlyDTO = customerService.getCustomerByUUIDDeletedFalse(uuid);
        return ResponseEntity.ok(customerReadOnlyDTO);
    }

    @Operation(summary = "Deletes a customer. It is a soft-delete design pattern.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "Customer deleted",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                    responseCode = "404", description = "Customer not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "403", description = "Access Denied",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    })

    @DeleteMapping("/{uuid}")
    public ResponseEntity<CustomerReadOnlyDTO> deleteCustomerByUUID(@PathVariable UUID uuid)
            throws EntityNotFoundException, EntityInvalidArgumentException {
        CustomerReadOnlyDTO customerReadOnlyDTO = customerService.deleteCustomerByUUID(uuid);
        return ResponseEntity.ok(customerReadOnlyDTO);
    }

    @Operation(
            summary = "List a customer's accounts",
            description = """
                    Returns the customer's open accounts. Staff may ask for any customer;
                    a customer may only ask for their own."""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The customer's open accounts"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{uuid}/accounts")
    @PreAuthorize("hasAuthority('VIEW_ACCOUNT') or authentication.principal.customer?.uuid?.toString() == #uuid")
    public ResponseEntity<List<AccountReadOnlyDTO>> getCustomerAccounts(@PathVariable String uuid)
    throws EntityNotFoundException {
        List<AccountReadOnlyDTO> accounts = customerService.getCustomerAccountsNotDeleted(uuid);
        return ResponseEntity.ok(accounts);
    }

    @Operation(
            summary = "Set a customer's password (administrator)",
            description = """
                    Replaces the password without asking for the current one, for when a
                    customer has lost access. Administrators only — an employee cannot reset
                    anyone's password. A customer changing their own password uses
                    PUT /users/{uuid}/password instead, which does require the current one."""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Password changed"),
            @ApiResponse(responseCode = "400", description = "New password does not meet the strength rule",
                    content = @Content(schema = @Schema(implementation = ValidationErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/{uuid}/password")
    public ResponseEntity<Void> resetCustomerPassword(
            @PathVariable UUID uuid,
            @Valid @RequestBody PasswordResetDTO dto,
            BindingResult bindingResult)
            throws EntityNotFoundException, ValidationException {

        if (bindingResult.hasErrors()) {
            throw new ValidationException("PasswordReset", "Invalid password", bindingResult);
        }
        customerService.resetPassword(uuid, dto.newPassword());
        return ResponseEntity.noContent().build();
    }
}
