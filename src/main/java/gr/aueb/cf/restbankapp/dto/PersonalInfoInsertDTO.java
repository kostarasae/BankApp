package gr.aueb.cf.restbankapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record PersonalInfoInsertDTO(
        @NotNull
        @Pattern(regexp = "[Α-ΩA-Z]{1,2}\\d{6,7}", message = "ID number must be 1-2 uppercase letters followed by 6-7 digits")
        String idNumber,

        @NotBlank
        String placeOfBirth,

        @NotBlank
        String municipalityOfRegistration
) {}