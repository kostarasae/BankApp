package gr.aueb.cf.restbankapp.security;

import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;

/**
 * Single source of truth for the error codes we send on a 401.
 * <p>
 * The same failure can surface from two places: {@code ErrorHandler} catches it when the
 * login endpoint itself rejects the credentials, and {@code CustomAuthenticationEntryPoint}
 * catches it when a request reaches a protected route unauthenticated. Both used to carry
 * their own copy of the mapping, and they had already drifted -- one said
 * {@code INVALID_CREDENTIALS}, the other {@code BAD_CREDENTIALS} for the very same event,
 * which forced the client to know both spellings.
 * <p>
 * The fallback stays per-caller, because the two sites genuinely differ in what "anything
 * else" means: a failed login attempt is {@link #AUTHENTICATION_ERROR}, whereas a request
 * with no usable token is {@link #UNAUTHORIZED} and should send the user back to sign in.
 */
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

    /**
     * Matches on the exception type rather than on {@code getClass().getSimpleName()}, so a
     * subclass of, say, {@link BadCredentialsException} still maps to the right code instead
     * of silently falling through to the fallback.
     *
     * @param e        the authentication failure
     * @param fallback code to use when the failure is none of the cases below
     */
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
