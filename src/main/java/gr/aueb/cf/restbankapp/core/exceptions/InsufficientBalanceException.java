package gr.aueb.cf.restbankapp.core.exceptions;

public class InsufficientBalanceException extends AppGenericException {
    private static final String DEFAULT_CODE = "InsufficientBalance";

    public InsufficientBalanceException(String code, String message) {
        super(code + DEFAULT_CODE, message);
    }
}
