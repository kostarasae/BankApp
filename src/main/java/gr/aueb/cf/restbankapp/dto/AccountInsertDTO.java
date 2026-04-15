package gr.aueb.cf.restbankapp.dto;

import gr.aueb.cf.restbankapp.model.AccountType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AccountInsertDTO(

        @NotNull
        AccountType type,

        BigDecimal balance,
        
        @NotNull
        String customerUuid)  {

    public static AccountInsertDTO empty() {

        return new AccountInsertDTO(AccountType.CHECKING, BigDecimal.ZERO, "");
    }
}
