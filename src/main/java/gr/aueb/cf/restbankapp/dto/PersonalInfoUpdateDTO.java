package gr.aueb.cf.restbankapp.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record PersonalInfoUpdateDTO(

        @NotEmpty(message = "ID number is required")
        @Pattern(regexp = "[Α-ΩA-Z]{1,2}\\d{6,7}", message = "ID number must be 1-2 uppercase letters followed by 6-7 digits")
        String idNumber,

        @NotEmpty(message = "Place of birth is required")
        String placeOfBirth,

        @NotEmpty(message = "Municipality of registration is required")
        String municipalityOfRegistration
) {}