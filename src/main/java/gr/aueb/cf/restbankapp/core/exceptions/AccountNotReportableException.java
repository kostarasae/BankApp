package gr.aueb.cf.restbankapp.core.exceptions;

public class AccountNotReportableException extends AppGenericException {
    private static final String DEFAULT_CODE = "AccountNotReportable";

    public AccountNotReportableException(String code, String message) {
        super(code + DEFAULT_CODE, message);
    }
}
