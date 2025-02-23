package com.project1.ms_account_service.business.service;

import com.project1.ms_account_service.model.AccountBalanceResponse;
import com.project1.ms_account_service.model.AccountPatchRequest;
import com.project1.ms_account_service.model.AccountRequest;
import com.project1.ms_account_service.model.AccountResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountService {
    Mono<AccountResponse> createAccount(Mono<AccountRequest> request);

    Mono<AccountResponse> getAccountById(String id);

    Mono<AccountResponse> getAccountByAccountNumber(String accountNumber);

    Flux<AccountResponse> getAccountsByCustomerId(String customerId);

    Mono<AccountResponse> updateAccount(String id, Mono<AccountPatchRequest> request);

    Mono<AccountBalanceResponse> getAccountBalanceByAccountNumber(String accountNumber);

    Mono<Void> deleteAccount(String id);
}
