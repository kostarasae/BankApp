package gr.aueb.cf.restbankapp.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AccountWithdrawDTO(

        @NotNull
        String iban,

        @NotNull
        BigDecimal amount)  {
}
