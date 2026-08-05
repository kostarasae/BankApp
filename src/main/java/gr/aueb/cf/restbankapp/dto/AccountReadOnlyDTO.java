package gr.aueb.cf.restbankapp.dto;

import gr.aueb.cf.restbankapp.model.AccountType;

import java.math.BigDecimal;

public record AccountReadOnlyDTO(String iban, String accountNumber, AccountType accountType, BigDecimal balance) {
    @Override
    public String toString() {
        return "IBAN: " + iban + ", Number: " + accountNumber + ", Type: " + accountType + ", Balance: " + balance;
    }
}
