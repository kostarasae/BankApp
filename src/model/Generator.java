package chatgpt.model;

import java.math.BigInteger;

/**
 * Utility class
 */

public final class Generator {

    private Generator() {} // no instances

    private static BigInteger counter = BigInteger.ZERO;

    public static synchronized String generateAccountNumber() {
        counter = counter.add(BigInteger.ONE);
        return String.format("%010d", counter); // 10 digits;
    }

    public static String generateIban(String accountNumber) {
        String bankCode = "0100";            // Bank
        String branchCode = "0001";          // Branch
        return "GR" + "00" + bankCode + branchCode + accountNumber;
    }
}
