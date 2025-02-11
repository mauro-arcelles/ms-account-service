package com.project1.ms_account_service.business;

import com.project1.ms_account_service.business.adapter.CustomerService;
import com.project1.ms_account_service.exception.AccountNotFoundException;
import com.project1.ms_account_service.exception.BadRequestException;
import com.project1.ms_account_service.exception.InvalidAccountTypeException;
import com.project1.ms_account_service.model.*;
import com.project1.ms_account_service.model.entity.AccountStatus;
import com.project1.ms_account_service.model.entity.AccountType;
import com.project1.ms_account_service.model.entity.CustomerType;
import com.project1.ms_account_service.repository.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
                .flatMap(this::validateAccountType)
                .flatMap(this::validateCustomerAccountLimits)
                .flatMap(r -> customerService.getCustomerById(r.getCustomerId())
                        .flatMap(cr -> validateBusinessAccountMembers(r, cr)))
                .map(accountMapper::getAccountCreationEntity)
                .flatMap(accountRepository::save)
                .map(accountMapper::getAccountResponse);
    }

    private Mono<AccountRequest> validateBusinessAccountMembers(AccountRequest request, CustomerResponse customerResponse) {
        CustomerType customerType = CustomerType.valueOf(customerResponse.getType());
        if (customerType == CustomerType.BUSINESS) {
            if (request.getHolders().isEmpty()) {
                return Mono.error(new BadRequestException("At least one HOLDER is necessary for BUSINESS accounts"));
            }
        } else {
            if (!request.getHolders().isEmpty()) {
                return Mono.error(new BadRequestException("HOLDERS are not valid for PERSONAL accounts"));
            }
            if (!request.getSigners().isEmpty()) {
                return Mono.error(new BadRequestException("AUTHORIZED SIGNERS are not valid for PERSONAL accounts"));
            }
        }
        return Mono.just(request);
    }

    @Override
    public Mono<AccountResponse> getAccountById(String id) {
        return accountRepository.findById(id)
                .map(accountMapper::getAccountResponse);
    }

    @Override
    public Mono<AccountResponse> getAccountByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .map(accountMapper::getAccountResponse)
                .switchIfEmpty(Mono.error(new AccountNotFoundException("Account not found with account number: " + accountNumber)));
    }

    @Override
    public Flux<AccountResponse> getAccountsByCustomerId(String customerId) {
        return accountRepository.findByCustomerId(customerId)
                .map(accountMapper::getAccountResponse);
    }

    @Override
    public Mono<AccountResponse> updateAccount(String id, Mono<AccountPatchRequest> request) {
        return accountRepository.findById(id)
                .switchIfEmpty(Mono.error(new AccountNotFoundException("Account not found with id: " + id)))
                .flatMap(existingAccount -> request
                        .map(req -> accountMapper.getAccountUpdateEntity(req, existingAccount))
                        .flatMap(accountRepository::save)
                        .map(accountMapper::getAccountResponse)
                );
    }

    @Override
    public Mono<AccountBalanceResponse> getAccountBalanceByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .switchIfEmpty(Mono.error(new AccountNotFoundException("Account no found with account number: " + accountNumber)))
                .map(accountMapper::getAccountBalanceResponse);
    }

    @Override
    public Mono<Void> deleteAccount(String id) {
        return accountRepository.findById(id)
                .switchIfEmpty(Mono.error(new AccountNotFoundException("Account not found with id: " + id)))
                .flatMap(acc -> {
                    acc.setStatus(AccountStatus.INACTIVE);
                    return accountRepository.save(acc);
                })
                .then()
                .doOnSuccess(v -> log.info("Deleted account: {}", id));
    }

    /**
     * Validates that the account type is valid
     *
     * @param request Account request to validate
     * @return Valid account request or error
     */
    private Mono<AccountRequest> validateAccountType(AccountRequest request) {
        if (!isValidAccountType(request.getAccountType())) {
            return Mono.error(new InvalidAccountTypeException());
        }
        return Mono.just(request);
    }

    private Mono<CustomerResponse> validateCustomerExists(AccountRequest request) {
        return customerService.getCustomerById(request.getCustomerId());
    }

    /**
     * Validates customer account limits based on customer type
     *
     * @param request Account request to validate
     * @return Valid account request or error
     */
    private Mono<AccountRequest> validateCustomerAccountLimits(AccountRequest request) {
        return customerService.getCustomerById(request.getCustomerId())
                .flatMap(customer -> {
                    if (CustomerType.PERSONAL.toString().equals(customer.getType())) {
                        return validatePersonalCustomerAccounts(request);
                    }
                    if (CustomerType.BUSINESS.toString().equals(customer.getType())) {
                        return validateBusinessCustomerAccounts(request);
                    }
                    return Mono.just(request);
                });
    }

    /**
     * Validates business customer account creation rules
     *
     * @param request Account request to validate
     * @return Valid account request or error
     */
    private Mono<AccountRequest> validateBusinessCustomerAccounts(AccountRequest request) {
        AccountType accountType = AccountType.valueOf(request.getAccountType());
        if (accountType.equals(AccountType.SAVINGS) || accountType.equals(AccountType.FIXED_TERM)) {
            return Mono.error(new BadRequestException("BUSINESS customers cannot have " + accountType + " account"));
        }
        return Mono.just(request);
    }

    /**
     * Validates personal customer account creation rules
     *
     * @param request Account request to validate
     * @return Valid account request or error
     */
    private Mono<AccountRequest> validatePersonalCustomerAccounts(AccountRequest request) {
        return accountRepository.findByCustomerId(request.getCustomerId())
                .collectList()
                .flatMap(accounts -> {
                    AccountType accountType = AccountType.valueOf(request.getAccountType());
                    long accountTypeCount = accounts.stream()
                            .filter(acc -> acc.getAccountType().equals(accountType))
                            .count();
                    if (accountType.equals(AccountType.SAVINGS) && accountTypeCount > 0) {
                        return Mono.error(new BadRequestException("PERSONAL customers can only have one SAVINGS account"));
                    }
                    if (accountType.equals(AccountType.CHECKING) && accountTypeCount > 0) {
                        return Mono.error(new BadRequestException("PERSONAL customers can only have one CHECKING account"));
                    }
                    return Mono.just(request);
                });
    }

    /**
     * Checks if provided account type is valid
     *
     * @param accountType Account type to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidAccountType(String accountType) {
        try {
            AccountType.valueOf(accountType);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
