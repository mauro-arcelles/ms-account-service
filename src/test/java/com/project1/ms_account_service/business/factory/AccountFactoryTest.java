package com.project1.ms_account_service.business.factory;

import com.project1.ms_account_service.exception.InvalidAccountTypeException;
import com.project1.ms_account_service.model.AccountRequest;
import com.project1.ms_account_service.model.entity.AccountType;
import com.project1.ms_account_service.model.entity.CustomerType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class AccountFactoryTest {
    @MockBean
    private CheckingAccountCreator checkingAccountCreator;

    @MockBean
    private SavingsAccountCreator savingsAccountCreator;

    @MockBean
    private FixedTermAccountCreator fixedTermAccountCreator;

    @Autowired
    private AccountFactory accountFactory;

    @Test
    void whenCheckingAccount_thenCallCheckingCreator() {
        AccountRequest request = new AccountRequest();
        request.setAccountType(AccountType.CHECKING.toString());
        CustomerType customerType = CustomerType.PERSONAL;

        accountFactory.getAccount(request, customerType);

        verify(checkingAccountCreator).createAccount(request, customerType);
    }

    @Test
    void whenSavingsAccount_thenCallSavingsCreator() {
        AccountRequest request = new AccountRequest();
        request.setAccountType("SAVINGS");
        CustomerType customerType = CustomerType.PERSONAL;

        accountFactory.getAccount(request, customerType);

        verify(savingsAccountCreator).createAccount(request, customerType);
    }

    @Test
    void whenFixedTermAccount_thenCallFixedTermCreator() {
        AccountRequest request = new AccountRequest();
        request.setAccountType("FIXED_TERM");
        CustomerType customerType = CustomerType.PERSONAL;

        accountFactory.getAccount(request, customerType);

        verify(fixedTermAccountCreator).createAccount(request, customerType);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenInvalidEnumValue() {
        AccountRequest request = new AccountRequest();
        request.setAccountType("INVALID_TYPE");

        assertThrows(IllegalArgumentException.class, () ->
            accountFactory.getAccount(request, CustomerType.PERSONAL));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenNull() {
        AccountRequest request = new AccountRequest();
        request.setAccountType(null);

        assertThrows(InvalidAccountTypeException.class, () ->
            accountFactory.getAccount(request, CustomerType.PERSONAL));
    }
}
