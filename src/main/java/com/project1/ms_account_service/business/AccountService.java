package com.project1.ms_account_service.business;

import com.project1.ms_account_service.model.AccountRequest;
import com.project1.ms_account_service.model.AccountResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountService {
    Mono<AccountResponse> createAccount(Mono<AccountRequest> request);
    Mono<AccountResponse> getAccountById(String id);
    Flux<AccountResponse> getAccountsByCustomerId(String customerId);
}
