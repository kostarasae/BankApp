package gr.aueb.cf.restbankapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record PersonalInfoUpdateDTO(

        @NotBlank(message = "ID number is required")
        @Pattern(regexp = "[Α-ΩA-Z]{1,2}\\d{6,7}", message = "ID number must be 1-2 uppercase letters followed by 6-7 digits")
        String idNumber,

        @NotBlank(message = "Place of birth is required")
        String placeOfBirth,

        @NotBlank(message = "Municipality of registration is required")
        String municipalityOfRegistration,

        @NotBlank(message = "Date of birth is required")
        String dateOfBirth,

        @NotBlank(message = "Home address is required")
        String homeAddress,

        @NotBlank(message = "Gender is required")
        String gender
) {}