package chatgpt.dao;

import chatgpt.model.Account;

import java.util.List;
import java.util.Optional;

/**
 * CRUD Contract API
 */
public interface IAccountDAO {

    //CRUD
    Account saveOrUpdate(Account account);
    Optional<Account> findByIban(String iban);
    List<Account> getAllAccounts();
    void remove(String iban);

    long count();

    // Queries
    boolean isAccountExists(String iban);
}
