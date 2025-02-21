package com.project1.ms_account_service;

import com.project1.ms_account_service.business.AccountMapper;
import com.project1.ms_account_service.business.AccountService;
import com.project1.ms_account_service.model.AccountBalanceResponse;
import com.project1.ms_account_service.model.AccountPatchRequest;
import com.project1.ms_account_service.model.AccountRequest;
import com.project1.ms_account_service.model.AccountResponse;
import com.project1.ms_account_service.model.entity.Account;
import com.project1.ms_account_service.model.entity.AccountStatus;
import com.project1.ms_account_service.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

@SpringBootTest
public class AccountApiDelegateImplTest {
    @MockBean
    private AccountService accountService;

    @MockBean
    private AccountRepository accountRepository;

    @MockBean
    private AccountMapper accountMapper;

    @Autowired
    private AccountApiDelegateImpl accountApiDelegate;

    @Test
    void getAccountById_Success() {
        AccountResponse response = new AccountResponse();
        when(accountService.getAccountById("1")).thenReturn(Mono.just(response));

        StepVerifier.create(accountApiDelegate.getAccountById("1", null))
            .expectNext(ResponseEntity.ok(response))
            .verifyComplete();
    }

    @Test
    void createAccount_Success() {
        AccountRequest request = new AccountRequest();
        AccountResponse response = new AccountResponse();
        when(accountService.createAccount(any())).thenReturn(Mono.just(response));

        StepVerifier.create(accountApiDelegate.createAccount(Mono.just(request), null))
            .expectNext(ResponseEntity.status(HttpStatus.CREATED).body(response))
            .verifyComplete();
    }

    @Test
    void deleteAccount_Success() {
        when(accountService.deleteAccount("1")).thenReturn(Mono.empty());

        StepVerifier.create(accountApiDelegate.deleteAccountById("1", null))
            .expectComplete()
            .verify();
    }

    @Test
    void getAccountBalance_Success() {
        AccountBalanceResponse response = new AccountBalanceResponse();
        when(accountService.getAccountBalanceByAccountNumber("123")).thenReturn(Mono.just(response));

        StepVerifier.create(accountApiDelegate.getAccountBalance("123", null))
            .expectNext(ResponseEntity.ok(response))
            .verifyComplete();
    }

    @Test
    void getAccountByAccountNumber_Success() {
        AccountResponse response = new AccountResponse();
        when(accountService.getAccountByAccountNumber("123")).thenReturn(Mono.just(response));

        StepVerifier.create(accountApiDelegate.getAccountByAccountNumber("123", null))
            .expectNext(ResponseEntity.ok(response))
            .verifyComplete();
    }

    @Test
    void getAccountsByCustomer_Success() {
        AccountResponse response = new AccountResponse();
        when(accountService.getAccountsByCustomerId("customerId"))
            .thenReturn(Flux.just(response));

        StepVerifier.create(accountApiDelegate.getAccountsByCustomer("customerId", null))
            .expectNextMatches(responseEntity ->
                responseEntity.getStatusCode() == HttpStatus.OK &&
                    responseEntity.getBody() != null)
            .verifyComplete();
    }

    @Test
    void updateAccountById_Success() {
        String id = "1";

        AccountPatchRequest patchRequest = new AccountPatchRequest();
        patchRequest.setStatus(AccountStatus.ACTIVE.toString());

        AccountResponse accountResponse = new AccountResponse();
        accountResponse.setId(id);

        when(accountService.updateAccount(anyString(), any(Mono.class)))
            .thenReturn(Mono.just(accountResponse));

        StepVerifier.create(accountApiDelegate.updateAccountById("1", Mono.just(patchRequest), null))
            .expectNext(ResponseEntity.ok(accountResponse))
            .verifyComplete();
    }

}
