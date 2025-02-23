package com.project1.ms_account_service.business.factory;

import com.project1.ms_account_service.business.mapper.AccountMapper;
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
class FixedTermAccountCreatorTest {

    @Autowired
    private FixedTermAccountCreator fixedTermAccountCreator;

    @MockBean
    private AccountMapper accountMapper;

    @Test
    void shouldCreateFixedTermAccount() {
        com.project1.ms_account_service.model.FixedTermAccount request = new com.project1.ms_account_service.model.FixedTermAccount();
        request.setAccountType("FIXED_TERM");
        request.setCustomerId("123");
        request.setInitialBalance(new BigDecimal("1000"));
        request.setTermInMonths(12);

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

        Account account = fixedTermAccountCreator.createAccount(request, CustomerType.PERSONAL);

        assertInstanceOf(FixedTermAccount.class, account);
        FixedTermAccount fixedTermAccount = (FixedTermAccount) account;
        assertNotNull(fixedTermAccount.getMaxMonthlyMovements());
        assertEquals(12, fixedTermAccount.getTermInMonths());
        assertNotNull(fixedTermAccount.getAvailableDayForMovements());
        assertNotNull(fixedTermAccount.getEndDay());
        assertEquals(BigDecimal.ZERO, fixedTermAccount.getMaintenanceFee());
        assertNotNull(fixedTermAccount.getMaxMonthlyMovementsNoFee());
        assertNotNull(fixedTermAccount.getTransactionCommissionFeePercentage());
        assertEquals(0, fixedTermAccount.getMonthlyMovements());
        assertEquals(AccountType.FIXED_TERM, fixedTermAccount.getAccountType());
        assertEquals(CustomerType.PERSONAL, fixedTermAccount.getCustomerType());
        assertEquals("123", fixedTermAccount.getCustomerId());
        assertEquals(new BigDecimal("1000"), fixedTermAccount.getBalance());
        assertEquals(AccountStatus.ACTIVE, fixedTermAccount.getStatus());
        assertEquals(List.of(holder), fixedTermAccount.getHolders());
        assertEquals(List.of(signer), fixedTermAccount.getSigners());
    }
}
