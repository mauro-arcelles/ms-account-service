package com.project1.ms_account_service.business;

import com.project1.ms_account_service.exception.InvalidAccountTypeException;
import com.project1.ms_account_service.model.AccountRequest;
import com.project1.ms_account_service.model.AccountResponse;
import com.project1.ms_account_service.model.entity.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Component
public class AccountMapper {
    @Value("${account.config.checking.maintenanceFee}")
    private Double maintenanceFee;

    @Value("${account.config.fixedterm.maxMonthlyMovements}")
    private Integer fixedTermMaxMonthlyMovements;

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
                FixedTermAccount fixedTermAccount = new FixedTermAccount();
                fixedTermAccount.setMaxMonthlyMovements(fixedTermMaxMonthlyMovements);
                account = fixedTermAccount;
                account.setMaintenanceFee(0.0);
                break;
            default:
                throw new InvalidAccountTypeException("Invalid account type");
        }

        account.setAccountType(AccountType.valueOf(request.getAccountType()));
        account.setCustomerId(request.getCustomerId());
        account.setBalance(request.getInitialBalance());
        account.setCreationDate(LocalDateTime.now());
        account.setStatus(AccountStatus.ACTIVE);
        account.setAccountNumber(generateAccountNumber());

        return account;
    }

    public AccountResponse getAccountResponse(Account account) {
        AccountResponse accountResponse = new AccountResponse();
        accountResponse.setId(account.getId());
        accountResponse.setAccountNumber(account.getAccountNumber());
        accountResponse.setAccountType(account.getAccountType().toString());
        accountResponse.setBalance(account.getBalance());
        accountResponse.setCreationDate(account.getCreationDate().atOffset(ZoneOffset.UTC));
        accountResponse.setCustomerId(account.getCustomerId());
        accountResponse.setMaintenanceFee(account.getMaintenanceFee());
        accountResponse.setMonthlyMovements(account.getMonthlyMovements());
        accountResponse.setStatus(account.getStatus().toString());
        return accountResponse;
    }

    private String generateAccountNumber() {
        return "ACC-" + UUID.randomUUID().toString();
    }
}
