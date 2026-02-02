package chatgpt.dto;

import chatgpt.model.AccountType;

import java.math.BigDecimal;

public record AccountInsertDTO(AccountType type, BigDecimal balance)  {

    public static AccountInsertDTO empty() {

        return new AccountInsertDTO(AccountType.CHECKING, BigDecimal.ZERO);
    }
}
