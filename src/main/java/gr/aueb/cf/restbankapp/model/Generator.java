package gr.aueb.cf.restbankapp.model;

/**
 * Utility class
 */

public final class Generator {

    private Generator() {} // no instances

    public static String generateIban(String accountNumber) {
        String bankCode = "0100";            // Bank
        String branchCode = "0001";          // Branch
        return "GR" + "00" + bankCode + branchCode + accountNumber;
    }
}
