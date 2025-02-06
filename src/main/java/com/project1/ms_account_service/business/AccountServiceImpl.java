package com.project1.ms_account_service.business;

import com.project1.ms_account_service.model.AccountRequest;
import com.project1.ms_account_service.model.AccountResponse;
import com.project1.ms_account_service.model.CustomerResponse;
import com.project1.ms_account_service.repository.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class AccountServiceImpl implements AccountService {
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private CustomerService customerService;

    @Override
    public Mono<AccountResponse> createAccount(Mono<AccountRequest> request) {
        return request
                .flatMap(req ->
                        customerService.getCustomerById(req.getCustomerId())
                                .then(Mono.just(req))
                ).map(accountMapper::getAccountCreationEntity)
                .flatMap(accountRepository::save)
                .map(accountMapper::getAccountResponse)
                .doOnSuccess(a -> log.info("Account created successfully with id: {}", a.getId()))
                .doOnError(e -> log.error("Error creating account", e));
    }

    @Override
    public Mono<AccountResponse> getAccountById(String id) {
        return accountRepository.findById(id)
                .map(accountMapper::getAccountResponse)
                .doOnSuccess(a -> log.info("Account found: {}", a.getId()))
                .doOnError(e -> log.error("Error fetching account", e));
    }

    @Override
    public Flux<AccountResponse> getAccountsByCustomerId(String customerId) {
        return accountRepository.findByCustomerId(customerId)
                .map(accountMapper::getAccountResponse)
                .doOnComplete(() -> log.info("Completed fetching accounts for customer: {}", customerId))
                .doOnError(e -> log.error("Error fetching customer accounts", e));
    }
}
