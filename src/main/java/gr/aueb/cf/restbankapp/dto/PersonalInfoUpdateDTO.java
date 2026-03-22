package gr.aueb.cf.restbankapp.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record PersonalInfoUpdateDTO(

        @NotEmpty(message = "AFM is required")
        @Pattern(regexp = "\\d{11}", message = "AFM must be an 11-digit number")
        String afm,

        @NotEmpty(message = "Identity number is required")
        String identityNumber,

        @NotEmpty(message = "Place of birth is required")
        String placeOfBirth,

        @NotEmpty(message = "Municipality of registration is required")
        String municipalityOfRegistration
) {}
