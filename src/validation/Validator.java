package chatgpt.validation;

import chatgpt.dto.AccountDepositDTO;
import chatgpt.dto.AccountInsertDTO;
import chatgpt.dto.AccountWithdrawDTO;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class
 */
public class Validator {

    /**
     * No instances of this class should be available.
     */
    private Validator() {
        throw new AssertionError("No instances");
    }


    public static Map<String, String> validateInsertDTO(AccountInsertDTO insertDTO) {

        Map<String, String> errors = new HashMap<>();

        if (insertDTO.balance() == null || insertDTO.balance().compareTo(BigDecimal.ZERO) < 0) {
            errors.put("balance", "balance should not be negative");
        }

        return errors;
    }


    public static Map<String, String> validateDepositDTO(AccountDepositDTO depositDTO) {
        Map<String, String> errors = new HashMap<>();

        if (depositDTO.iban() == null || !depositDTO.iban().trim().matches("GR\\d{3,25}")) {
            errors.put("iban", "iban needs to start with GR and followed by 3-25 digits");
        }

        if (depositDTO.amount() == null || depositDTO.amount().compareTo(BigDecimal.ZERO) < 0) {
            errors.put("amount", "deposit amount should not be negative");
        }

        return errors;
    }


    public static Map<String, String> validateWithdrawDTO(AccountWithdrawDTO withdrawDTO) {
        Map<String, String> errors = new HashMap<>();

        if (withdrawDTO.iban() == null || !withdrawDTO.iban().trim().matches("GR\\d{3,25}")) {
            errors.put("iban", "iban needs to start with GR and followed by 3-25 digits");
        }

        if (withdrawDTO.amount() == null || withdrawDTO.amount().compareTo(BigDecimal.ZERO) < 0) {
            errors.put("amount", "withdraw amount should not be negative");
        }

        return errors;
    }


    public static Map<String, String> validateIban(String iban) {
        Map<String, String> errors = new HashMap<>();

        if (iban == null || !iban.trim().matches("GR\\d{3,25}")) {
            errors.put("iban", "iban needs to start with GR and followed by 3-25 digits");
        }
        return errors;
    }
}
