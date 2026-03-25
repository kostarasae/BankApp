package gr.aueb.cf.restbankapp.api;

import gr.aueb.cf.restbankapp.core.exceptions.EntityAlreadyExistsException;
import gr.aueb.cf.restbankapp.core.exceptions.EntityInvalidArgumentException;
import gr.aueb.cf.restbankapp.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.restbankapp.core.exceptions.ValidationException;
//import gr.aueb.cf.restbankapp.dto.RoleReadOnlyDTO;
import gr.aueb.cf.restbankapp.dto.ErrorResponseDTO;
import gr.aueb.cf.restbankapp.dto.UserInsertDTO;
import gr.aueb.cf.restbankapp.dto.UserReadOnlyDTO;
//import gr.aueb.cf.restbankapp.service.IRoleService;
import gr.aueb.cf.restbankapp.dto.ValidationErrorResponseDTO;
import gr.aueb.cf.restbankapp.service.IUserService;
import gr.aueb.cf.restbankapp.validation.UserInsertValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserRestController {

    private final IUserService userService;
    private final UserInsertValidator userInsertValidator;

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account in the system."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "User created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserReadOnlyDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ValidationErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "User already exists",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })

    @PostMapping
    public ResponseEntity<UserReadOnlyDTO> registerUser(@Valid @RequestBody UserInsertDTO userInsertDTO,
                                       BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model)
            throws ValidationException, EntityAlreadyExistsException, EntityInvalidArgumentException {

        userInsertValidator.validate(userInsertDTO, bindingResult);

        if (bindingResult.hasErrors()) {
            throw new ValidationException("User", "Invalid user data", bindingResult);
        }

        UserReadOnlyDTO userReadOnlyDTO = userService.saveUser(userInsertDTO);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{uuid}")
                .buildAndExpand(userReadOnlyDTO.uuid())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(userReadOnlyDTO);
    }

    @Operation(
            summary = "Get user by UUID",
            description = "Retrieves a non-deleted user by their UUID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserReadOnlyDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDTO.class)
                    )
            )
    })

    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{uuid}")
    public ResponseEntity<UserReadOnlyDTO> getUserByUUID(@PathVariable UUID uuid)
            throws EntityNotFoundException {

        return ResponseEntity.ok(userService.getUserByUUIDDeletedFalse(uuid));
    }

}