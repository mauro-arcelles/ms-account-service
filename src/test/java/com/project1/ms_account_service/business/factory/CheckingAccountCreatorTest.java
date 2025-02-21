package com.project1.ms_account_service.business.factory;

import com.project1.ms_account_service.business.AccountMapper;
import com.project1.ms_account_service.model.AccountRequest;
import com.project1.ms_account_service.model.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;

@SpringBootTest
class CheckingAccountCreatorTest {

    @Autowired
    private CheckingAccountCreator checkingAccountCreator;

    @MockBean
    private AccountMapper accountMapper;

    @Test
    void shouldCreateCheckingAccount() {
        AccountRequest request = new AccountRequest();
        request.setAccountType("CHECKING");
        request.setCustomerId("123");
        request.setInitialBalance(new BigDecimal("1000"));

        com.project1.ms_account_service.model.AccountMember holderRequest = new com.project1.ms_account_service.model.AccountMember();
        holderRequest.setDni("12345678");
        holderRequest.setName("John");
        holderRequest.setLastName("Doe");
        holderRequest.setEmail("john@email.com");

        com.project1.ms_account_service.model.AccountMember signerRequest = new com.project1.ms_account_service.model.AccountMember();
        signerRequest.setDni("87654321");
        signerRequest.setName("Jane");
        signerRequest.setLastName("Doe");
        signerRequest.setEmail("jane@email.com");

        request.setHolders(List.of(holderRequest));
        request.setSigners(List.of(signerRequest));

        com.project1.ms_account_service.model.entity.AccountMember holder = com.project1.ms_account_service.model.entity.AccountMember.builder()
            .dni("12345678")
            .name("John")
            .lastName("Doe")
            .email("john@email.com")
            .build();

        com.project1.ms_account_service.model.entity.AccountMember signer = com.project1.ms_account_service.model.entity.AccountMember.builder()
            .dni("87654321")
            .name("Jane")
            .lastName("Doe")
            .email("jane@email.com")
            .build();

        when(accountMapper.getAccountMember(holderRequest)).thenReturn(holder);
        when(accountMapper.getAccountMember(signerRequest)).thenReturn(signer);

        Account account = checkingAccountCreator.createAccount(request, CustomerType.PERSONAL);

        assertInstanceOf(CheckingAccount.class, account);
        CheckingAccount checkingAccount = (CheckingAccount) account;
        assertNotNull(checkingAccount.getMaintenanceFee());
        assertNotNull(checkingAccount.getMaxMonthlyMovementsNoFee());
        assertNotNull(checkingAccount.getTransactionCommissionFeePercentage());
        assertEquals(0, checkingAccount.getMonthlyMovements());
        assertEquals(AccountType.CHECKING, checkingAccount.getAccountType());
        assertEquals(CustomerType.PERSONAL, checkingAccount.getCustomerType());
        assertEquals("123", checkingAccount.getCustomerId());
        assertEquals(new BigDecimal("1000"), checkingAccount.getBalance());
        assertEquals(AccountStatus.ACTIVE, checkingAccount.getStatus());
        assertEquals(List.of(holder), checkingAccount.getHolders());
        assertEquals(List.of(signer), checkingAccount.getSigners());
    }
}
