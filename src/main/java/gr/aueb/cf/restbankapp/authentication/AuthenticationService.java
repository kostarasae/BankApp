package gr.aueb.cf.restbankapp.authentication;

import gr.aueb.cf.restbankapp.dto.AuthenticationRequestDTO;
import gr.aueb.cf.restbankapp.dto.AuthenticationResponseDTO;
import gr.aueb.cf.restbankapp.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponseDTO authenticate(AuthenticationRequestDTO dto) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.username(), dto.password()));

        User user = (User) authentication.getPrincipal();

        String token = jwtService.generateToken(authentication.getName(), user.getRole().getName());

        String userUuid = user.getUuid().toString();

        String role = user.getRole().getName();

        String customerUuid = user.getCustomer() != null ? user.getCustomer().getUuid().toString() : null;

        return new AuthenticationResponseDTO(token, userUuid, role, customerUuid);
    }
}
