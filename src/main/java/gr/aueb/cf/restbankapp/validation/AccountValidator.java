package gr.aueb.cf.restbankapp.validation;

import gr.aueb.cf.restbankapp.dto.AccountDepositDTO;
import gr.aueb.cf.restbankapp.dto.AccountInsertDTO;
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
                || AccountWithdrawDTO.class == clazz;
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

        if (dto.type() == null) {
            log.warn("Validation failed. Account type is null");
            errors.rejectValue(
                    "type",
                    "type.account.invalid",
                    "Account type must not be null"
            );
        }

        if (dto.balance() == null || dto.balance().compareTo(BigDecimal.ZERO) < 0) {
            log.warn("Validation failed. Negative balance: {}", dto.balance());
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
}
