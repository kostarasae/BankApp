package gr.aueb.cf.restbankapp.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AccountDepositDTO (

        @NotNull
        String iban,

        String description,

        @NotNull
        BigDecimal amount) {
}
