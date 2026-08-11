package gr.aueb.cf.restbankapp.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder
public record UserUpdateDTO(

        @NotNull(message = "To username δεν μπορεί να είναι null.")
        @Size(min = 2, max = 20, message = "Το username πρέπει να είναι μεταξύ 2-20 χαρακτήρες.")
        String username,

        // Optional: editing a customer's details never touches the password, and a
        // staff member filling the edit form does not know it. When present it still
        // has to satisfy the strength rule.
        @Pattern(regexp = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=])^.{8,}$",
                message = "Το password πρέπει να περιέχει τουλάχιστον 1 πεζό, 1 κεφαλαίο, 1 ψηφίο, και 1 ειδικό χαρακτήρα χωρίς κενά")
        String password
) {}