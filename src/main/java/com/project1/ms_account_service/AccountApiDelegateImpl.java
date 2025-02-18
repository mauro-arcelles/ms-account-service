package com.project1.ms_account_service;

import com.project1.ms_account_service.api.AccountsApiDelegate;
import com.project1.ms_account_service.business.AccountService;
import com.project1.ms_account_service.model.AccountBalanceResponse;
import com.project1.ms_account_service.model.AccountPatchRequest;
import com.project1.ms_account_service.model.AccountRequest;
import com.project1.ms_account_service.model.AccountResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class AccountApiDelegateImpl implements AccountsApiDelegate {

    @Autowired
    private AccountService accountService;

    @Override
    public Mono<ResponseEntity<AccountResponse>> getAccountById(String id, ServerWebExchange exchange) {
        return accountService.getAccountById(id)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<AccountResponse>> getAccountByAccountNumber(String accountNumber, ServerWebExchange exchange) {
        return accountService.getAccountByAccountNumber(accountNumber)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Flux<AccountResponse>>> getAccountsByCustomer(String customerId, ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.ok(accountService.getAccountsByCustomerId(customerId)));
    }

    @Override
    public Mono<ResponseEntity<AccountResponse>> createAccount(Mono<AccountRequest> request, ServerWebExchange exchange) {
        return accountService.createAccount(request)
                .map(ResponseEntity.status(HttpStatus.CREATED)::body);
    }

    @Override
    public Mono<ResponseEntity<AccountBalanceResponse>> getAccountBalance(String accountNumber, ServerWebExchange exchange) {
        return accountService.getAccountBalanceByAccountNumber(accountNumber)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<AccountResponse>> updateAccountById(String id, Mono<AccountPatchRequest> accountPatchRequest, ServerWebExchange exchange) {
        return accountService.updateAccount(id, accountPatchRequest)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteAccountById(String id, ServerWebExchange exchange) {
        return accountService.deleteAccount(id)
                .map(ResponseEntity::ok);
    }
}
