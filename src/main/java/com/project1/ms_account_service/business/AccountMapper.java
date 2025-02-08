package com.project1.ms_account_service.business;

import com.project1.ms_account_service.exception.BadRequestException;
import com.project1.ms_account_service.exception.InvalidAccountTypeException;
import com.project1.ms_account_service.model.AccountBalanceResponse;
import com.project1.ms_account_service.model.AccountPatchRequest;
import com.project1.ms_account_service.model.AccountRequest;
import com.project1.ms_account_service.model.AccountResponse;
import com.project1.ms_account_service.model.entity.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

@Component
public class AccountMapper {
    @Value("${account.config.checking.maintenanceFee}")
    private Double maintenanceFee;

    @Value("${account.config.fixedterm.maxMonthlyMovements}")
    private Integer fixedTermMaxMonthlyMovements;

    @Value("${account.config.fixedterm.availableDayForMovements}")
    private Integer availableDayForMovements;

    @Value("${account.config.savings.maxMonthlyMovements}")
    private Integer savingsMaxMonthlyMovements;

    public Account getAccountCreationEntity(AccountRequest request) {
        Account account;
        AccountType accountType = AccountType.valueOf(request.getAccountType());
        switch (accountType) {
            case SAVINGS:
                SavingsAccount savingsAccount = new SavingsAccount();
                savingsAccount.setMaxMonthlyMovements(savingsMaxMonthlyMovements);
                account = savingsAccount;
                account.setMaintenanceFee(0.0);
                break;
            case CHECKING:
                CheckingAccount checkingAccount = new CheckingAccount();
                account = checkingAccount;
                account.setMaintenanceFee(maintenanceFee);
                break;
            case FIXED_TERM:
                if (request.getTermInMonths() == null) {
                    throw new BadRequestException("termInMonths is necessary for FIXED TERM accounts");
                }
                FixedTermAccount fixedTermAccount = new FixedTermAccount();
                fixedTermAccount.setMaxMonthlyMovements(fixedTermMaxMonthlyMovements);
                fixedTermAccount.setTermInMonths(request.getTermInMonths());
                fixedTermAccount.setAvailableDayForMovements(availableDayForMovements);
                fixedTermAccount.setEndDay(LocalDateTime.now().plusMonths(request.getTermInMonths()));
                account = fixedTermAccount;
                account.setMaintenanceFee(0.0);
                break;
            default:
                throw new InvalidAccountTypeException("Invalid account type. Should be one of: SAVINGS|CHECKING|FIXED_TERM");
        }

        account.setMonthlyMovements(0);
        account.setAccountType(AccountType.valueOf(request.getAccountType()));
        account.setCustomerId(request.getCustomerId());
        account.setBalance(request.getInitialBalance());
        account.setCreationDate(LocalDateTime.now());
        account.setStatus(AccountStatus.ACTIVE);
        account.setAccountNumber(generateAccountNumber());

        return account;
    }

    public Account getAccountUpdateEntity(AccountPatchRequest request, Account existingAccount) {
        if (request.getBalance() == null &&
                request.getMonthlyMovements() == null &&
                request.getStatus() == null) {
            throw new BadRequestException("At least one field must be provided");
        }
        Optional<Integer> optionalMonthlyMovements = Optional.ofNullable(request.getMonthlyMovements());
        if (optionalMonthlyMovements.isPresent()) {
            if (existingAccount.getAccountType().equals(AccountType.SAVINGS)) {
                SavingsAccount savingsAccount = (SavingsAccount) existingAccount;
                if (optionalMonthlyMovements.get() > savingsAccount.getMaxMonthlyMovements()) {
                    throw new BadRequestException("Max monthly movements limit reached. The monthly movements available: " + savingsAccount.getMaxMonthlyMovements());
                }
            }
            if (existingAccount.getAccountType().equals(AccountType.FIXED_TERM)) {
                FixedTermAccount fixedTermAccount = (FixedTermAccount) existingAccount;
                if (optionalMonthlyMovements.get() > fixedTermAccount.getMaxMonthlyMovements()) {
                    throw new BadRequestException("Max monthly movements limit reached. The monthly movements available: " + fixedTermAccount.getMaxMonthlyMovements());
                }
            }
        }
        Optional.ofNullable(request.getBalance()).ifPresent(existingAccount::setBalance);
        Optional.ofNullable(request.getMonthlyMovements()).ifPresent(existingAccount::setMonthlyMovements);
        Optional.ofNullable(request.getStatus())
                .ifPresent(status -> existingAccount.setStatus(AccountStatus.valueOf(status)));
        return existingAccount;
    }

    public AccountResponse getAccountResponse(Account account) {
        AccountResponse accountResponse = new AccountResponse();
        accountResponse.setId(account.getId());
        accountResponse.setAccountNumber(account.getAccountNumber());
        accountResponse.setAccountType(account.getAccountType().toString());
        accountResponse.setBalance(account.getBalance());
        accountResponse.setCreationDate(account.getCreationDate());
        accountResponse.setCustomerId(account.getCustomerId());
        accountResponse.setMaintenanceFee(account.getMaintenanceFee());
        accountResponse.setMonthlyMovements(account.getMonthlyMovements());
        accountResponse.setStatus(account.getStatus().toString());
        if (account.getAccountType().equals(AccountType.SAVINGS)) {
            accountResponse.setMaxMonthlyMovements(((SavingsAccount) account).getMaxMonthlyMovements());
        }
        if (account.getAccountType().equals(AccountType.FIXED_TERM)) {
            FixedTermAccount fixedTermAccount = (FixedTermAccount) account;
            accountResponse.setMaxMonthlyMovements(fixedTermAccount.getMaxMonthlyMovements());
            accountResponse.setEndDay(fixedTermAccount.getEndDay());
            accountResponse.setAvailableDayForMovements(fixedTermAccount.getAvailableDayForMovements());
        }
        return accountResponse;
    }

    public AccountBalanceResponse getAccountBalanceResponse(Account account) {
        AccountBalanceResponse accountBalanceResponse = new AccountBalanceResponse();
        accountBalanceResponse.setBalance(account.getBalance());
        return accountBalanceResponse;
    }

    private String generateAccountNumber() {
        return "ACC-" + UUID.randomUUID().toString();
    }
}
