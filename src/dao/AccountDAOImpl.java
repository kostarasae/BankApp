package chatgpt.dao;

import chatgpt.model.Account;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CRUD API implementation
 */
public class AccountDAOImpl implements IAccountDAO {

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    // Create / Update
    @Override
    public Account saveOrUpdate(Account account) {
        // Insert account
        accounts.put(account.getIban(), account);
        return account;
    }


    // Read
    @Override
    public Optional<Account> findByIban(String iban) {
        return Optional.ofNullable(accounts.get(iban));
    }
    @Override
    public List<Account> getAllAccounts() {
        return Collections.unmodifiableList(new ArrayList<>(accounts.values()));
    }

    // Delete
    @Override
    public void remove(String iban) {
        accounts.remove(iban);
    }

    @Override
    public long count() {
        return accounts.size();
    }

    // Queries
    @Override
    public boolean isAccountExists(String iban) {
        return accounts.containsKey(iban);
    }
}
