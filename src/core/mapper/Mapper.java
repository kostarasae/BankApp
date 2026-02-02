package chatgpt.core.mapper;

import chatgpt.core.factory.AccountFactory;
import chatgpt.dto.AccountInsertDTO;
import chatgpt.dto.AccountReadOnlyDTO;
import chatgpt.model.Account;

/**
 * Utility class
 */
public class Mapper {

    // no available instances
    private Mapper(){}

    public static Account mapToModelEntity(AccountInsertDTO dto) {
        return AccountFactory.create(dto.type(), dto.balance());
    }

    public static AccountReadOnlyDTO mapToReadOnlyDTO(Account account) {
        return new AccountReadOnlyDTO(account.getIban(), account.getBalance());
    }
}
