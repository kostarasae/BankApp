package gr.aueb.cf.restbankapp.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordChangeDTO(

        @NotBlank
        String currentPassword,

        @NotBlank
        String newPassword) {
}
