package gr.aueb.cf.restbankapp.core.exceptions;

public class NegativeAmountException extends AppGenericException {
    private static final String DEFAULT_CODE = "NegativeAmount";

    public NegativeAmountException(String code, String message) {
        super(code + DEFAULT_CODE, message);
    }
}
