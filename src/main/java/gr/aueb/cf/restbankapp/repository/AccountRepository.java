package gr.aueb.cf.restbankapp.repository;

import gr.aueb.cf.restbankapp.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long>,
        JpaSpecificationExecutor<Account> {

    Optional<Account> findByAccountNumber(String accountNumber);
    Optional<Account> findByIban(String iban);
    Optional<Account> findByIbanAndDeletedFalse(String iban);
}
