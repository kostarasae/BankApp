package chatgpt.dto;

import java.math.BigDecimal;

public record AccountDepositDTO (String iban, BigDecimal amount) {
}
