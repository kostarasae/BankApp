package gr.aueb.cf.restbankapp.api;

import gr.aueb.cf.restbankapp.core.exceptions.EntityAlreadyExistsException;
import gr.aueb.cf.restbankapp.core.exceptions.EntityInvalidArgumentException;
import gr.aueb.cf.restbankapp.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.restbankapp.core.exceptions.ValidationException;
import gr.aueb.cf.restbankapp.dto.ErrorResponseDTO;
import gr.aueb.cf.restbankapp.dto.PasswordChangeDTO;
import gr.aueb.cf.restbankapp.dto.PasswordResetDTO;
import gr.aueb.cf.restbankapp.dto.UserInsertDTO;
import gr.aueb.cf.restbankapp.dto.UserReadOnlyDTO;
import gr.aueb.cf.restbankapp.dto.ValidationErrorResponseDTO;
import gr.aueb.cf.restbankapp.service.IUserService;
import gr.aueb.cf.restbankapp.validation.UserInsertValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User accounts, staff management and passwords.")
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
                                       BindingResult bindingResult)
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


    @Operation(
            summary = "Change your own password",
            description = """
                    Requires the current password, so it only works on your own account —
                    the uuid in the path has to be yours. An administrator setting someone
                    else's password uses the reset endpoints instead."""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Password changed"),
            @ApiResponse(responseCode = "400", description = "Current password is wrong, or the new one does not meet the strength rule",
                    content = @Content(schema = @Schema(implementation = ValidationErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/{uuid}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable String uuid,
            @Valid @RequestBody PasswordChangeDTO dto,
            BindingResult bindingResult)
            throws EntityNotFoundException, EntityInvalidArgumentException, ValidationException {

        if (bindingResult.hasErrors()) {
            throw new ValidationException("PasswordChange", "Invalid password", bindingResult);
        }
        userService.changePassword(uuid, dto.currentPassword(), dto.newPassword());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "List administrators and employees")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/staff")
    public ResponseEntity<List<UserReadOnlyDTO>> getStaff() {
        return ResponseEntity.ok(userService.getStaffUsers());
    }

    @Operation(summary = "Delete a staff user")
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID uuid)
            throws EntityNotFoundException, EntityInvalidArgumentException {

        userService.deleteUser(uuid);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reset a user's password without knowing the current one")
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/{uuid}/reset-password")
    public ResponseEntity<Void> resetPassword(
            @PathVariable UUID uuid,
            @Valid @RequestBody PasswordResetDTO dto,
            BindingResult bindingResult)
            throws EntityNotFoundException, ValidationException {

        if (bindingResult.hasErrors()) {
            throw new ValidationException("PasswordReset", "Invalid password", bindingResult);
        }
        userService.resetPassword(uuid, dto.newPassword());
        return ResponseEntity.noContent().build();
    }
}