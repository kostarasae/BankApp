package gr.aueb.cf.restbankapp.api;

import gr.aueb.cf.restbankapp.core.exceptions.*;
import gr.aueb.cf.restbankapp.dto.AccountDepositDTO;
import gr.aueb.cf.restbankapp.dto.AccountInsertDTO;
import gr.aueb.cf.restbankapp.dto.AccountReadOnlyDTO;
import gr.aueb.cf.restbankapp.dto.AccountWithdrawDTO;
import gr.aueb.cf.restbankapp.service.IAccountService;
import gr.aueb.cf.restbankapp.validation.AccountValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;

/**
 * Facade Design Pattern (simplifies access to subsystems)
 */
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final IAccountService accountService;
    private final AccountValidator accountValidator;

    @PostMapping
    public ResponseEntity<AccountReadOnlyDTO> createNewAccount(
            @Valid @RequestBody AccountInsertDTO accountInsertDTO,
            BindingResult bindingResult)
            throws EntityAlreadyExistsException, ValidationException {

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
    @PreAuthorize("hasAuthority('DEPOSIT')")
    public ResponseEntity<AccountReadOnlyDTO> deposit(
            @Valid @RequestBody AccountDepositDTO depositDTO, BindingResult bindingResult)
            throws EntityNotFoundException, NegativeAmountException, ValidationException
    {
        accountValidator.validate(depositDTO, bindingResult);

        if (bindingResult.hasErrors()) {
            throw new ValidationException("Account", "Invalid account data", bindingResult);
        }

        AccountReadOnlyDTO accountReadOnlyDTO = accountService.deposit(depositDTO);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{deposit}")
                .buildAndExpand(accountReadOnlyDTO.iban())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(accountReadOnlyDTO);
    }

    @PostMapping("/withdraw")
    @PreAuthorize("hasAuthority('WITHDRAW')")
    public ResponseEntity<AccountReadOnlyDTO> withdraw(
            @Valid @RequestBody AccountWithdrawDTO withdrawDTO, BindingResult bindingResult)
            throws EntityNotFoundException, NegativeAmountException, InsufficientBalanceException, ValidationException
    {
        accountValidator.validate(withdrawDTO, bindingResult);

        if (bindingResult.hasErrors()) {
            throw new ValidationException("Account", "Invalid account data", bindingResult);
        }

        AccountReadOnlyDTO accountReadOnlyDTO = accountService.withdraw(withdrawDTO);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{withdraw}")
                .buildAndExpand(accountReadOnlyDTO.iban())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(accountReadOnlyDTO);
    }

    public void validateIban(String iban, BindingResult bindingResult) {
        if (iban == null || iban.isBlank()) {
            bindingResult.rejectValue("iban", "invalid", "IBAN is invalid");
        }
    }

    @GetMapping
    public BigDecimal getBalance(
            @Valid @RequestBody String iban, BindingResult bindingResult)
            throws EntityNotFoundException, ValidationException
    {
        validateIban(iban, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new ValidationException("Account", "Invalid account data", bindingResult);
        }
        return accountService.getBalance(iban);
    }
}
