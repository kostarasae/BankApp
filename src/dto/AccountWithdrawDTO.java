package chatgpt.dto;

import java.math.BigDecimal;

public record AccountWithdrawDTO(String iban, BigDecimal amount)  {
}
