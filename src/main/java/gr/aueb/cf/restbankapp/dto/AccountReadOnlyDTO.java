package gr.aueb.cf.restbankapp.dto;

import java.math.BigDecimal;

public record AccountReadOnlyDTO(String iban, BigDecimal balance) {
    @Override
    public String toString() {
        return "IBAN: " + iban + ", Balance: " + balance;
    }
}
