package gr.aueb.cf.restbankapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordChangeDTO(

        @NotBlank
        String currentPassword,

        @NotBlank
        @Pattern(regexp = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=])^.{8,}$",
                message = "Το password πρέπει να περιέχει τουλάχιστον 1 πεζό, 1 κεφαλαίο, 1 ψηφίο, και 1 ειδικό χαρακτήρα χωρίς κενά")
        String newPassword) {
}
