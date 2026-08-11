package gr.aueb.cf.restbankapp.security;

import gr.aueb.cf.restbankapp.model.User;
import gr.aueb.cf.restbankapp.repository.AccountRepository;
import gr.aueb.cf.restbankapp.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service("securityService")
public class SecurityService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;

    public boolean isOwnCustomerProfile(UUID customerUuid, Authentication authentication) {
        User principal = (User) authentication.getPrincipal();
        // Find the customer record and check if its user uuid matches the logged-in user
        return customerRepository.existsByUuidAndUser_Uuid(customerUuid, principal.getUuid());
    }

    /**
     * Whether the account belongs to the logged-in user. IBANs are not secret —
     * they travel with every transfer — so a role check alone is not enough to
     * protect per-account data.
     */
    public boolean isOwnAccount(String iban, Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof User principal)) {
            return false;
        }
        return accountRepository.existsByIbanAndCustomers_User_Uuid(iban, principal.getUuid());
    }

    /**
     * Whether the current caller is an administrator. Used where the rule is
     * conditional and cannot be expressed as a @PreAuthorize on the whole method,
     * such as allowing only an admin to assign a staff role during registration.
     * An anonymous caller holds ROLE_ANONYMOUS, so this correctly returns false.
     */
    public boolean isCurrentUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
