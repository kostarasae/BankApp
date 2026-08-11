package gr.aueb.cf.restbankapp.repository;

import gr.aueb.cf.restbankapp.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, Long>,
        JpaSpecificationExecutor<Account> {

    Optional<Account> findByAccountNumber(String accountNumber);
    Optional<Account> findByIban(String iban);
    Optional<Account> findByIbanAndDeletedFalse(String iban);

    boolean existsByIbanAndCustomers_User_Uuid(String iban, UUID userUuid);

    @Query(value = "SELECT nextval('account_number_seq')", nativeQuery = true)
    Long nextAccountNumberValue();
}
