package gr.aueb.cf.restbankapp.api;

import gr.aueb.cf.restbankapp.core.exceptions.*;
import gr.aueb.cf.restbankapp.dto.*;
import gr.aueb.cf.restbankapp.service.IAccountService;
import gr.aueb.cf.restbankapp.validation.AccountValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
@Tag(name = "Accounts", description = "Bank accounts and the money that moves through them.")
// Declared once for the whole controller: every endpoint here needs a token, and it
// also switches on the customizer in OpenApiConfig that documents 401 and 403.
@SecurityRequirement(name = "Bearer Authentication")
public class AccountRestController {

    private final IAccountService accountService;
    private final AccountValidator accountValidator;

    @Operation(
            summary = "Open an account for a customer",
            description = """
                    Creates a checking or savings account, generates its account number and
                    IBAN, and links it to the customer. An opening deposit greater than zero
                    is recorded as the account's first transaction."""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created; the Location header points at it",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AccountReadOnlyDTO.class))),
            @ApiResponse(responseCode = "400", description = "Missing customer, unknown account type, or negative opening deposit",
                    content = @Content(schema = @Schema(implementation = ValidationErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Customer not found"),
            @ApiResponse(responseCode = "409", description = "Generated account number collided with an existing one")
    })
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

    @Operation(
            summary = "Close an account",
            description = """
                    Marks the account as closed. It stops appearing in listings but its
                    history is kept. The customers who owned it are left untouched —
                    deleting a customer is a separate action."""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account closed",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AccountReadOnlyDTO.class))),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @DeleteMapping("/{iban}")
    public ResponseEntity<AccountReadOnlyDTO> closeAccount(
            @Parameter(description = "IBAN of the account to close", example = "GR1600000000000000000000001")
            @PathVariable String iban)
            throws EntityNotFoundException {
        AccountReadOnlyDTO accountReadOnlyDTO = accountService.closeAccount(iban);
        return ResponseEntity.ok(accountReadOnlyDTO);
    }

    @Operation(
            summary = "Get one account",
            description = "Returns the account's number, IBAN, type and current balance."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The account",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AccountReadOnlyDTO.class))),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping("/{iban}")
    public ResponseEntity<AccountReadOnlyDTO> getAccountByIban(
            @Parameter(description = "IBAN of the account", example = "GR1600000000000000000000001")
            @PathVariable String iban)
            throws EntityNotFoundException {
        AccountReadOnlyDTO accountReadOnlyDTO = accountService.getAccountByIban(iban);
        return ResponseEntity.ok(accountReadOnlyDTO);
    }

    @Operation(
            summary = "Deposit money",
            description = """
                    Adds the amount to the balance and records it on the account's statement.
                    Deposits carry no fee."""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deposit accepted; returns the updated account",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AccountReadOnlyDTO.class))),
            @ApiResponse(responseCode = "400", description = "Amount is zero or negative, or the IBAN is malformed",
                    content = @Content(schema = @Schema(implementation = ValidationErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
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

    @Operation(
            summary = "Withdraw money",
            description = """
                    Takes the amount plus the account's fee out of the balance. Both are
                    written to the statement as separate entries, so the statement always
                    adds up to the balance."""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Withdrawal accepted; returns the updated account",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AccountReadOnlyDTO.class))),
            @ApiResponse(responseCode = "400", description = "Amount is zero or negative, or the IBAN is malformed",
                    content = @Content(schema = @Schema(implementation = ValidationErrorResponseDTO.class))),
            @ApiResponse(responseCode = "422", description = "Amount plus fee exceeds what the account allows to be taken out",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
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

    @Operation(
            summary = "Transfer money to another account",
            description = """
                    Moves the amount from the sender to the recipient and charges the sender
                    a fee. Three entries are written: the transfer and the fee on the sender's
                    statement, and the incoming amount on the recipient's. Either the whole
                    transfer succeeds or nothing changes."""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transfer completed; returns the sender's updated account",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AccountReadOnlyDTO.class))),
            @ApiResponse(responseCode = "400", description = "Amount is zero or negative, or sender and recipient are the same account",
                    content = @Content(schema = @Schema(implementation = ValidationErrorResponseDTO.class))),
            @ApiResponse(responseCode = "422", description = "Amount plus fee exceeds the sender's available balance",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Sender or recipient account not found")
    })
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

    @Operation(
            summary = "List every account",
            description = "Staff-facing listing of all open accounts in the bank."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "All open accounts",
                    content = @Content(mediaType = "application/json",
                            array = @io.swagger.v3.oas.annotations.media.ArraySchema(
                                    schema = @Schema(implementation = AccountReadOnlyDTO.class))))
    })
    @GetMapping
    public ResponseEntity<List<AccountReadOnlyDTO>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @Operation(
            summary = "Get an account's statement",
            description = """
                    Returns the account's movements, newest first, one page at a time.
                    A customer may only read their own statement; staff may read any.
                    Every movement is here, fees included, so the entries add up to the balance."""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "One page of movements",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping("/{iban}/transactions")
    public ResponseEntity<Page<TransactionReadOnlyDTO>> getTransactions(
            @Parameter(description = "IBAN of the account", example = "GR1600000000000000000000001")
            @PathVariable String iban,
            @Parameter(description = "Page number (0-based) and page size; defaults to 20 newest first")
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(accountService.getTransactions(iban, pageable));
    }

    @Operation(
            summary = "Get the fee charged on this account",
            description = """
                    Returns what a withdrawal or transfer from this account costs. The amount
                    depends on the account type. Useful for showing the cost before confirming."""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The fee",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = FeeDTO.class))),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping("/{iban}/fee")
    public ResponseEntity<FeeDTO> getAccountFee(
            @Parameter(description = "IBAN of the account", example = "GR1600000000000000000000001")
            @PathVariable String iban)
            throws EntityNotFoundException {
        return ResponseEntity.ok(accountService.getAccountFee(iban));
    }

    @Operation(
            summary = "Look up who owns an account, by IBAN",
            description = """
                    Returns only the owner's first and last name, so the sender can confirm
                    the recipient before transferring. Deliberately nothing else — this is a
                    confirmation step, not a way to read someone's details."""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The recipient's name",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = IrisRecipientDTO.class))),
            @ApiResponse(responseCode = "404", description = "No account with this IBAN")
    })
    @GetMapping("/{iban}/owner")
    @PreAuthorize("hasAuthority('CAN_TRANSFER')")
    public ResponseEntity<IrisRecipientDTO> getAccountOwner(
            @Parameter(description = "IBAN of the recipient's account", example = "GR1600000000000000000000003")
            @PathVariable String iban)
            throws EntityNotFoundException {
        return ResponseEntity.ok(accountService.getAccountOwner(iban));
    }

    @Operation(
            summary = "Look up who owns an account, by phone number (IRIS)",
            description = """
                    The IRIS flow: you know the recipient's phone number, not their IBAN.
                    Returns their name and IBAN so the transfer can be confirmed and sent.
                    The number is exactly 10 digits, with no country code."""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The recipient's name and IBAN",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = IrisRecipientDTO.class))),
            @ApiResponse(responseCode = "404", description = "No account registered to this phone number")
    })
    @GetMapping("/phone/{phone}")
    @PreAuthorize("hasAuthority('CAN_TRANSFER')")
    public ResponseEntity<IrisRecipientDTO> getAccountByPhone(
            @Parameter(description = "Recipient's phone number, 10 digits", example = "6900000002")
            @PathVariable String phone)
            throws EntityNotFoundException {
        return ResponseEntity.ok(accountService.getAccountByPhone(phone));
    }
}
