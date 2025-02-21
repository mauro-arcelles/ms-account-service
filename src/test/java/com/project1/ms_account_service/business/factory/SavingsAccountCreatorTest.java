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
class SavingsAccountCreatorTest {

    @Autowired
    private SavingsAccountCreator savingsAccountCreator;

    @MockBean
    private AccountMapper accountMapper;

    @Test
    void shouldCreateSavingsAccount() {
        AccountRequest request = new AccountRequest();
        request.setAccountType("SAVINGS");
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

        AccountMember holder = AccountMember.builder()
            .dni("12345678")
            .name("John")
            .lastName("Doe")
            .email("john@email.com")
            .build();

        AccountMember signer = AccountMember.builder()
            .dni("87654321")
            .name("Jane")
            .lastName("Doe")
            .email("jane@email.com")
            .build();

        when(accountMapper.getAccountMember(holderRequest)).thenReturn(holder);
        when(accountMapper.getAccountMember(signerRequest)).thenReturn(signer);

        Account account = savingsAccountCreator.createAccount(request, CustomerType.PERSONAL);

        assertInstanceOf(SavingsAccount.class, account);
        SavingsAccount savingsAccount = (SavingsAccount) account;
        assertEquals(0.0, savingsAccount.getMaintenanceFee());
        assertNotNull(savingsAccount.getMaxMonthlyMovements());
        assertNotNull(savingsAccount.getMaxMonthlyMovementsNoFee());
        assertNotNull(savingsAccount.getTransactionCommissionFeePercentage());
        assertEquals(0, savingsAccount.getMonthlyMovements());
        assertEquals(AccountType.SAVINGS, savingsAccount.getAccountType());
        assertEquals(CustomerType.PERSONAL, savingsAccount.getCustomerType());
        assertEquals("123", savingsAccount.getCustomerId());
        assertEquals(new BigDecimal("1000"), savingsAccount.getBalance());
        assertEquals(AccountStatus.ACTIVE, savingsAccount.getStatus());
        assertEquals(List.of(holder), savingsAccount.getHolders());
        assertEquals(List.of(signer), savingsAccount.getSigners());
    }
}
