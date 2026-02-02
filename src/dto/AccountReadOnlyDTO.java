package chatgpt.dto;

import java.math.BigDecimal;

public record AccountReadOnlyDTO(String iban, BigDecimal balance) {
    @Override
    public String toString() {
        return "IBAN: " + iban + ", Balance: " + balance;
    }
}
