package gr.aueb.cf.restbankapp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerInsertDTO (

        @NotNull
        @Size(min = 2)
        String firstname,

        @NotNull
        @Size(min = 2)
        String lastname,

        @NotNull
        @Pattern(regexp = "\\d{9,}") // at least 9 digits
        String vat,

        @NotNull
        @Pattern(regexp = "^[\\w.-]+@[\\w.-]+\\.\\w{2,}$")
        String email,

        @NotNull
        @Pattern(regexp = "69\\d{7}") // starts with 69 and has 10 digits
        String phone,

        @NotNull
        Long regionId,

        @Valid
        @NotNull
        UserInsertDTO userInsertDTO,

        @Valid
        @NotNull
        PersonalInfoInsertDTO personalInfoInsertDTO
) {}