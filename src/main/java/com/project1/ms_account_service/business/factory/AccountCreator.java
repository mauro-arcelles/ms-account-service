package com.project1.ms_account_service.business.factory;

import com.project1.ms_account_service.model.AccountRequest;
import com.project1.ms_account_service.model.entity.Account;
import com.project1.ms_account_service.model.entity.CustomerType;

public interface AccountCreator {
    Account createAccount(AccountRequest request, CustomerType customerType);
}
