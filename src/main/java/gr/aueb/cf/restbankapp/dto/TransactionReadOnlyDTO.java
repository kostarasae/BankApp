package gr.aueb.cf.restbankapp.dto;

import gr.aueb.cf.restbankapp.model.TransactionType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionReadOnlyDTO(

        @NotNull
        Instant timestamp,

        @NotNull
        TransactionType type,

        @NotNull
        BigDecimal amount,

        String description) {
}
