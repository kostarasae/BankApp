package gr.aueb.cf.restbankapp.dto;

import gr.aueb.cf.restbankapp.model.AccountType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AccountInsertDTO(

        @NotNull
        String iban,

        @NotNull
        AccountType type,

        BigDecimal balance)  {

    public static AccountInsertDTO empty() {

        return new AccountInsertDTO("GR0", AccountType.CHECKING, BigDecimal.ZERO);
    }
}
