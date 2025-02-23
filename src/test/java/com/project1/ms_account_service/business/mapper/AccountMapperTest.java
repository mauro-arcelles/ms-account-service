package com.project1.ms_account_service.business.mapper;

import com.project1.ms_account_service.exception.BadRequestException;
import com.project1.ms_account_service.model.AccountBalanceResponse;
import com.project1.ms_account_service.model.AccountPatchRequest;
import com.project1.ms_account_service.model.AccountResponse;
import com.project1.ms_account_service.model.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class AccountMapperTest {
    @Autowired
    private AccountMapper accountMapper;

    @Test
    void getAccountMemberResponse_ShouldMapCorrectly() {
        AccountMember accountMember = AccountMember.builder()
            .dni("12345678")
            .email("test@test.com")
            .name("John")
            .lastName("Doe")
            .build();

        var response = accountMapper.getAccountMemberResponse(accountMember);

        assertEquals(accountMember.getDni(), response.getDni());
        assertEquals(accountMember.getEmail(), response.getEmail());
        assertEquals(accountMember.getName(), response.getName());
        assertEquals(accountMember.getLastName(), response.getLastName());
    }

    @Test
    void getAccountUpdateEntity_ShouldThrowException_WhenNoFieldsProvided() {
        AccountPatchRequest request = new AccountPatchRequest();
        Account account = mock(Account.class);

        assertThrows(BadRequestException.class, () ->
            accountMapper.getAccountUpdateEntity(request, account));
    }

    @Test
    void getAccountUpdateEntity_ShouldUpdateFields_WhenValidRequest() {
        AccountPatchRequest request = new AccountPatchRequest();
        request.setBalance(new BigDecimal("1000.0"));
        request.setStatus("ACTIVE");

        Account account = new SavingsAccount();

        Account updated = accountMapper.getAccountUpdateEntity(request, account);

        assertEquals(request.getBalance(), updated.getBalance());
        assertEquals(AccountStatus.valueOf(request.getStatus()), updated.getStatus());
    }

    @Test
    void getAccountUpdateEntity_ShouldThrowExceptionWhenLimitReachedForSavingAccount() {
        AccountPatchRequest request = new AccountPatchRequest();
        request.setMonthlyMovements(10);

        SavingsAccount existingAccount = new SavingsAccount();
        existingAccount.setAccountType(AccountType.SAVINGS);
        existingAccount.setMaxMonthlyMovements(9);

        assertThrows(BadRequestException.class, () ->
            accountMapper.getAccountUpdateEntity(request, existingAccount));
    }

    @Test
    void getAccountUpdateEntity_ShouldThrowExceptionWhenLimitReachedForFixedTermAccount() {
        AccountPatchRequest request = new AccountPatchRequest();
        request.setMonthlyMovements(10);

        FixedTermAccount existingAccount = new FixedTermAccount();
        existingAccount.setAccountType(AccountType.FIXED_TERM);
        existingAccount.setMaxMonthlyMovements(9);

        assertThrows(BadRequestException.class, () ->
            accountMapper.getAccountUpdateEntity(request, existingAccount));
    }

    @Test
    void getAccountResponse_ShouldMapSavingsAccount() {
        SavingsAccount account = new SavingsAccount();
        account.setId("123");
        account.setAccountNumber("ACC123");
        account.setBalance(new BigDecimal("1000.0"));
        account.setAccountType(AccountType.SAVINGS);
        account.setMaxMonthlyMovements(5);
        account.setHolders(new ArrayList<>());
        account.setSigners(new ArrayList<>());

        AccountResponse response = accountMapper.getAccountResponse(account);

        assertEquals(account.getId(), response.getId());
        assertEquals(account.getAccountNumber(), response.getAccountNumber());
        assertEquals(account.getBalance(), response.getBalance());
        assertEquals(account.getAccountType().toString(), response.getAccountType());
        assertEquals(account.getMaxMonthlyMovements(), response.getMaxMonthlyMovements());
        assertEquals(0, response.getHolders().size());
        assertEquals(0, response.getSigners().size());
    }

    @Test
    void getAccountResponse_ShouldMapFixedTermAccount() {
        FixedTermAccount account = new FixedTermAccount();
        account.setId("123");
        account.setAccountNumber("ACC123");
        account.setBalance(new BigDecimal("1000.0"));
        account.setAccountType(AccountType.FIXED_TERM);
        account.setMaxMonthlyMovements(5);
        account.setEndDay(LocalDateTime.of(2024, 1, 1, 10, 30));
        account.setAvailableDayForMovements(10);

        AccountMember accountMember = new AccountMember();
        accountMember.setDni("12312312");
        accountMember.setEmail("test@test.com");
        accountMember.setName("testname");
        accountMember.setLastName("testlastname");

        List<AccountMember> holders = new ArrayList<>();
        holders.add(accountMember);
        account.setHolders(holders);

        List<AccountMember> signers = new ArrayList<>();
        signers.add(accountMember);
        account.setSigners(signers);

        AccountResponse response = accountMapper.getAccountResponse(account);

        assertEquals(account.getId(), response.getId());
        assertEquals(account.getAccountNumber(), response.getAccountNumber());
        assertEquals(account.getBalance(), response.getBalance());
        assertEquals(account.getAccountType().toString(), response.getAccountType());
        assertEquals(account.getMaxMonthlyMovements(), response.getMaxMonthlyMovements());
        assertEquals(account.getEndDay(), response.getEndDay());
        assertEquals(account.getAvailableDayForMovements(), response.getAvailableDayForMovements());
        assertEquals(holders.size(), response.getHolders().size());
        assertEquals(signers.size(), response.getSigners().size());
    }

    @Test
    void getAccountBalanceResponse_ShouldMapCorrectly() {
        Account account = mock(Account.class);
        when(account.getBalance()).thenReturn(new BigDecimal("1000.0"));

        AccountBalanceResponse response = accountMapper.getAccountBalanceResponse(account);

        assertEquals(account.getBalance(), response.getBalance());
    }

    @Test
    void getAccountMember_ShouldMapCorrectly() {
        com.project1.ms_account_service.model.AccountMember request =
            new com.project1.ms_account_service.model.AccountMember();
        request.setDni("12345678");
        request.setEmail("test@test.com");
        request.setName("John");
        request.setLastName("Doe");

        AccountMember result = accountMapper.getAccountMember(request);

        assertEquals(request.getDni(), result.getDni());
        assertEquals(request.getEmail(), result.getEmail());
        assertEquals(request.getName(), result.getName());
        assertEquals(request.getLastName(), result.getLastName());
    }
}
