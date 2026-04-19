package gr.aueb.cf.restbankapp.validation;

import gr.aueb.cf.restbankapp.dto.AccountDepositDTO;
import gr.aueb.cf.restbankapp.dto.AccountInsertDTO;
import gr.aueb.cf.restbankapp.dto.AccountTransferDTO;
import gr.aueb.cf.restbankapp.dto.AccountWithdrawDTO;
import gr.aueb.cf.restbankapp.service.IAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountValidator implements Validator {

    private final IAccountService accountService;

    @Override
    public boolean supports(Class<?> clazz) {
        return AccountInsertDTO.class == clazz
                || AccountDepositDTO.class == clazz
                || AccountWithdrawDTO.class == clazz
                || AccountTransferDTO.class == clazz;
    }

    @Override
    public void validate(Object target, Errors errors) {

        if (target instanceof AccountInsertDTO dto) {
            validateInsert(dto, errors);
        }

        if (target instanceof AccountDepositDTO dto) {
            validateDeposit(dto, errors);
        }

        if (target instanceof AccountWithdrawDTO dto) {
            validateWithdraw(dto, errors);
        }

        if (target instanceof AccountTransferDTO dto) {
            validateTransfer(dto, errors);
        }
    }

    private void validateInsert(AccountInsertDTO dto, Errors errors) {

        if (dto.customerUuid() == null) {
            log.warn("Validation failed. Invalid customer Uuid: {}", dto.customerUuid());
            errors.rejectValue(
                    "customerUuid",
                    "customerUuid.account.invalid",
                    "Missing customer Uuid"
            );
        }

        if (dto.accountType() == null) {
            log.warn("Validation failed. Account type is null");
            errors.rejectValue(
                    "type",
                    "type.account.invalid",
                    "Account type must not be null"
            );
        }

        if (dto.initialDeposit() == null || dto.initialDeposit().compareTo(BigDecimal.ZERO) < 0) {
            log.warn("Validation failed. Negative balance: {}", dto.initialDeposit());
            errors.rejectValue(
                    "balance",
                    "balance.account.negative",
                    "Balance should not be negative when opening an account"
            );
        }
    }

    private void validateDeposit(AccountDepositDTO dto, Errors errors) {

        if (dto.iban() == null || !dto.iban().trim().matches("GR\\d{3,25}")) {
            log.warn("Deposit failed. Invalid IBAN: {}", dto.iban());
            errors.rejectValue(
                    "iban",
                    "iban.account.invalid",
                    "Invalid IBAN"
            );
        }

        if (dto.amount() == null || dto.amount().compareTo(BigDecimal.ZERO) < 0) {
            log.warn("Deposit failed. Invalid amount: {}", dto.amount());
            errors.rejectValue(
                    "amount",
                    "amount.account.negative",
                    "Deposit amount should not be negative"
            );
        }
    }

    private void validateWithdraw(AccountWithdrawDTO dto, Errors errors) {

        if (dto.iban() == null || !dto.iban().trim().matches("GR\\d{3,25}")) {
            log.warn("Withdraw failed. Invalid IBAN: {}", dto.iban());
            errors.rejectValue(
                    "iban",
                    "iban.account.invalid",
                    "Invalid IBAN"
            );
        }

        if (dto.amount() == null || dto.amount().compareTo(BigDecimal.ZERO) < 0) {
            log.warn("Withdraw failed. Invalid amount: {}", dto.amount());
            errors.rejectValue(
                    "amount",
                    "amount.account.negative",
                    "Withdraw amount should not be negative"
            );
        }
    }

    private void validateTransfer(AccountTransferDTO dto, Errors errors) {

        if (dto.myIban() == null || !dto.myIban().trim().matches("GR\\d{3,25}")) {
            log.warn("Transfer failed. Invalid source IBAN: {}", dto.myIban());
            errors.rejectValue("myIban", "myIban.account.invalid", "Invalid source IBAN");
        }

        if (dto.toIban() == null || !dto.toIban().trim().matches("GR\\d{3,25}")) {
            log.warn("Transfer failed. Invalid destination IBAN: {}", dto.toIban());
            errors.rejectValue("toIban", "toIban.account.invalid", "Invalid destination IBAN");
        }

        if (dto.myIban() != null && dto.myIban().equals(dto.toIban())) {
            log.warn("Transfer failed. Source and destination IBAN are the same: {}", dto.myIban());
            errors.rejectValue("toIban", "toIban.account.self", "Cannot transfer to the same account");
        }

        if (dto.amount() == null || dto.amount().compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Transfer failed. Invalid amount: {}", dto.amount());
            errors.rejectValue("amount", "amount.account.invalid", "Transfer amount must be greater than zero");
        }
    }
}
