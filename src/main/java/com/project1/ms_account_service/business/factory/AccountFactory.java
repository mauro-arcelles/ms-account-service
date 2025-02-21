package com.project1.ms_account_service.business.factory;

import com.project1.ms_account_service.exception.InvalidAccountTypeException;
import com.project1.ms_account_service.model.AccountRequest;
import com.project1.ms_account_service.model.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AccountFactory {

    @Autowired
    private CheckingAccountCreator checkingAccountCreator;

    @Autowired
    private SavingsAccountCreator savingsAccountCreator;

    @Autowired
    private FixedTermAccountCreator fixedTermAccountCreator;

    public Account getAccount(AccountRequest request, CustomerType customerType) {
        if (request.getAccountType() == null) {
            throw new InvalidAccountTypeException();
        }

        switch (AccountType.valueOf(request.getAccountType())) {
            case CHECKING:
                return checkingAccountCreator.createAccount(request, customerType);
            case SAVINGS:
                return savingsAccountCreator.createAccount(request, customerType);
            case FIXED_TERM:
                return fixedTermAccountCreator.createAccount(request, customerType);
            default:
                throw new InvalidAccountTypeException();
        }

    }
}
