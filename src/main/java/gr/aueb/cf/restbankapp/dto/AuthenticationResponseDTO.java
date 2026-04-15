package gr.aueb.cf.restbankapp.dto;

public record AuthenticationResponseDTO(String token, String userUuid, String role, String customerUuid) {
}
