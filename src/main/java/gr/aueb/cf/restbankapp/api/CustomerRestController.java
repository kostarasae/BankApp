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

        CustomerReadOnlyDTO customerReadOnlyDTO = customerService.updateCustomer(customerUpdateDTO);

        return ResponseEntity.ok(customerReadOnlyDTO);
    }

    @Operation(
            summary = "Upload AFM attachment file for a customer",
            description = "Uploads a customer's AFM document file. Replaces existing file if present."
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

    @PostMapping("/{uuid}/afm-file")
    public ResponseEntity<Void> uploadAfmFile(
            @PathVariable UUID uuid,
            @RequestParam("afmFile") MultipartFile afmFile
    ) throws EntityNotFoundException, FileUploadException {

        customerService.saveAfmFile(uuid, afmFile);
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
            throws EntityNotFoundException {
        CustomerReadOnlyDTO customerReadOnlyDTO = customerService.deleteCustomerByUUID(uuid);
        return ResponseEntity.ok(customerReadOnlyDTO);
    }
}
