package com.project1.ms_account_service.business;

import com.project1.ms_account_service.model.AccountRequest;
import com.project1.ms_account_service.model.AccountResponse;
import com.project1.ms_account_service.model.entity.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Component
public class AccountMapper {
    public Account getAccountCreationEntity(AccountRequest request) {
        Account account;
        AccountType accountType = AccountType.valueOf(request.getAccountType());
        switch (accountType) {
            case SAVINGS:
                account = new SavingsAccount();
                break;
            case CHECKING:
                account = new CheckingAccount();
                break;
            case FIXED_TERM:
                account = new FixedTermAccount();
                break;
            default:
                throw new IllegalArgumentException("Invalid account type");
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
