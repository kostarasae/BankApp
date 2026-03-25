package gr.aueb.cf.restbankapp.security;

import gr.aueb.cf.restbankapp.model.User;
import gr.aueb.cf.restbankapp.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.nio.file.attribute.UserPrincipal;
import java.util.UUID;

@Service("securityService")
public class SecurityService {

    @Autowired
    private CustomerRepository customerRepository;

    public boolean isOwnCustomerProfile(UUID customerUuid, Authentication authentication) {
        User principal = (User) authentication.getPrincipal();
        // Find the customer record and check if its user uuid matches the logged-in user
        return customerRepository.existsByUuidAndUser_Uuid(customerUuid, principal.getUuid());
    }
}
