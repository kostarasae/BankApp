package gr.aueb.cf.restbankapp.api;

import gr.aueb.cf.restbankapp.authentication.AuthenticationService;
import gr.aueb.cf.restbankapp.dto.AuthenticationRequestDTO;
import gr.aueb.cf.restbankapp.dto.AuthenticationResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthRestController {

    private final AuthenticationService authenticationService;

    public ResponseEntity<AuthenticationResponseDTO> authenticate(AuthenticationRequestDTO dto) {
        AuthenticationResponseDTO responseDTO = authenticationService.authenticate(dto);
        return ResponseEntity.ok(responseDTO);
    }
}
