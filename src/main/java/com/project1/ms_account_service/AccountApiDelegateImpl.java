package com.project1.ms_account_service;

import com.project1.ms_account_service.api.AccountsApiDelegate;
import com.project1.ms_account_service.business.AccountService;
import com.project1.ms_account_service.model.AccountRequest;
import com.project1.ms_account_service.model.AccountResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@Component
public class AccountApiDelegateImpl implements AccountsApiDelegate {

    @Autowired
    private AccountService accountService;

    @Override
    public Mono<ResponseEntity<AccountResponse>> createAccount(@Valid Mono<AccountRequest> request, ServerWebExchange exchange) {
        return accountService.createAccount(request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @Override
    public Mono<ResponseEntity<AccountResponse>> getAccount(String id, ServerWebExchange exchange) {
        return accountService.getAccountById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Override
    public Mono<ResponseEntity<Flux<AccountResponse>>> getAccountsByCustomer(String customerId, ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.ok(accountService.getAccountsByCustomerId(customerId)));
    }
}
