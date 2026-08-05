package gr.aueb.cf.restbankapp.security;

import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;

public final class AuthErrorCode {

    public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    public static final String ACCOUNT_DISABLED = "ACCOUNT_DISABLED";
    public static final String ACCOUNT_LOCKED = "ACCOUNT_LOCKED";
    public static final String ACCOUNT_EXPIRED = "ACCOUNT_EXPIRED";
    public static final String CREDENTIALS_EXPIRED = "CREDENTIALS_EXPIRED";
    public static final String AUTHENTICATION_ERROR = "AUTHENTICATION_ERROR";
    public static final String UNAUTHORIZED = "UNAUTHORIZED";

    private AuthErrorCode() {
    }

    public static String of(AuthenticationException e, String fallback) {
        return switch (e) {
            case BadCredentialsException ignored -> INVALID_CREDENTIALS;
            case DisabledException ignored -> ACCOUNT_DISABLED;
            case LockedException ignored -> ACCOUNT_LOCKED;
            case AccountExpiredException ignored -> ACCOUNT_EXPIRED;
            case CredentialsExpiredException ignored -> CREDENTIALS_EXPIRED;
            default -> fallback;
        };
    }
}
