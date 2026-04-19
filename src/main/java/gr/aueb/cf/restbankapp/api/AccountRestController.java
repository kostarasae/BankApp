package gr.aueb.cf.restbankapp.api;

import gr.aueb.cf.restbankapp.core.exceptions.*;
import gr.aueb.cf.restbankapp.dto.*;
import gr.aueb.cf.restbankapp.service.IAccountService;
import gr.aueb.cf.restbankapp.validation.AccountValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Facade Design Pattern (simplifies access to subsystems)
 */
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountRestController {

    private final IAccountService accountService;
    private final AccountValidator accountValidator;

    @PostMapping
    public ResponseEntity<AccountReadOnlyDTO> createNewAccount(
            @Valid @RequestBody AccountInsertDTO accountInsertDTO,
            BindingResult bindingResult)
            throws EntityAlreadyExistsException, ValidationException, EntityNotFoundException, EntityInvalidArgumentException {

        accountValidator.validate(accountInsertDTO, bindingResult);

        if (bindingResult.hasErrors()) {
            throw new ValidationException("Account", "Invalid account data", bindingResult);
        }

        // Proceed to service and return read DTO
        AccountReadOnlyDTO accountReadOnlyDTO = accountService.createNewAccount(accountInsertDTO);

        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{iban}")
            .buildAndExpand(accountReadOnlyDTO.iban())
            .toUri();

        return ResponseEntity
                .created(location)
                .body(accountReadOnlyDTO);
    }

    @DeleteMapping("/{iban}")
    public ResponseEntity<AccountReadOnlyDTO> closeAccount(@PathVariable String iban)
            throws EntityNotFoundException {
        AccountReadOnlyDTO accountReadOnlyDTO = accountService.closeAccount(iban);
        return ResponseEntity.ok(accountReadOnlyDTO);
    }

    @GetMapping("/{iban}")
    public ResponseEntity<AccountReadOnlyDTO> getAccountByIban(@PathVariable String iban)
            throws EntityNotFoundException {
        AccountReadOnlyDTO accountReadOnlyDTO = accountService.getAccountByIban(iban);
        return ResponseEntity.ok(accountReadOnlyDTO);
    }

    @PostMapping("/deposit")
    public ResponseEntity<AccountReadOnlyDTO> deposit(
            @Valid @RequestBody AccountDepositDTO depositDTO, BindingResult bindingResult)
            throws EntityNotFoundException, NegativeAmountException, ValidationException
    {
        accountValidator.validate(depositDTO, bindingResult);

        if (bindingResult.hasErrors()) {
            throw new ValidationException("Account", "Invalid account data", bindingResult);
        }

        AccountReadOnlyDTO accountReadOnlyDTO = accountService.deposit(depositDTO);

        return ResponseEntity.ok(accountReadOnlyDTO);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<AccountReadOnlyDTO> withdraw(
            @Valid @RequestBody AccountWithdrawDTO withdrawDTO, BindingResult bindingResult)
            throws EntityNotFoundException, NegativeAmountException, InsufficientBalanceException, ValidationException
    {
        accountValidator.validate(withdrawDTO, bindingResult);

        if (bindingResult.hasErrors()) {
            throw new ValidationException("Account", "Invalid account data", bindingResult);
        }

        AccountReadOnlyDTO accountReadOnlyDTO = accountService.withdraw(withdrawDTO);

        return ResponseEntity.ok(accountReadOnlyDTO);
    }

    @PostMapping("/transfer")
    public ResponseEntity<AccountReadOnlyDTO> transfer(
            @Valid @RequestBody AccountTransferDTO transferDTO, BindingResult bindingResult)
            throws EntityNotFoundException, NegativeAmountException, InsufficientBalanceException, ValidationException
    {
        accountValidator.validate(transferDTO, bindingResult);

        if (bindingResult.hasErrors()) {
            throw new ValidationException("Account", "Invalid account data", bindingResult);
        }

        AccountReadOnlyDTO accountReadOnlyDTO = accountService.transfer(transferDTO);

        return ResponseEntity.ok(accountReadOnlyDTO);
    }

    @GetMapping
    public ResponseEntity<List<AccountReadOnlyDTO>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @GetMapping("/{iban}/transactions")
    @PreAuthorize("hasAuthority('VIEW_ONLY_ACCOUNT')")
    public ResponseEntity<List<TransactionReadOnlyDTO>> getTransactions(@PathVariable String iban) {
        return ResponseEntity.ok(accountService.getTransactions(iban));
    }
}
