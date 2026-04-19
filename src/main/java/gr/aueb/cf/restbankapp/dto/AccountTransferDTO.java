package gr.aueb.cf.restbankapp.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AccountTransferDTO(

        @NotNull
        String myIban,

        @NotNull
        String toIban,

        String description,

        @NotNull
        BigDecimal amount)  {
}