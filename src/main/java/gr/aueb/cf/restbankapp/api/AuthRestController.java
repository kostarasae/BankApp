package gr.aueb.cf.restbankapp.api;

import gr.aueb.cf.restbankapp.authentication.AuthenticationService;
import gr.aueb.cf.restbankapp.dto.AuthenticationRequestDTO;
import gr.aueb.cf.restbankapp.dto.AuthenticationResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Getting a token. Every other endpoint needs one.")
public class AuthRestController {

    private final AuthenticationService authenticationService;

    // Deliberately no @SecurityRequirement: this is how a caller obtains a token in
    // the first place, so it has to stay reachable without one.
    @Operation(
            summary = "Authenticate and receive a JWT",
            description = """
                    Exchanges a username and password for a signed token. Send it on every
                    other request as 'Authorization: Bearer <token>'. The token carries the
                    user's role and expires after 3 hours."""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentication succeeded",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthenticationResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Wrong username or password, or the account is disabled")
    })
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponseDTO> authenticate(@RequestBody AuthenticationRequestDTO dto) {
        AuthenticationResponseDTO responseDTO = authenticationService.authenticate(dto);
        return ResponseEntity.ok(responseDTO);
    }
}
