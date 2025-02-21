package com.project1.ms_account_service.business;

import com.project1.ms_account_service.business.adapter.CreditCardService;
import com.project1.ms_account_service.business.adapter.CustomerService;
import com.project1.ms_account_service.business.factory.AccountFactory;
import com.project1.ms_account_service.exception.AccountNotFoundException;
import com.project1.ms_account_service.exception.BadRequestException;
import com.project1.ms_account_service.exception.InvalidAccountTypeException;
import com.project1.ms_account_service.model.*;
import com.project1.ms_account_service.model.AccountMember;
import com.project1.ms_account_service.model.entity.*;
import com.project1.ms_account_service.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class AccountServiceImplTest {
    @MockBean
    private AccountRepository accountRepository;

    @MockBean
    private AccountMapper accountMapper;

    @MockBean
    private AccountFactory accountFactory;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private CreditCardService creditCardService;

    @Autowired
    private AccountServiceImpl accountService;

    @Test
    void createAccount_Success() {
        AccountRequest request = new AccountRequest();
        request.setAccountType(AccountType.SAVINGS.toString());
        request.setCustomerId("123");
        request.setHolders(new ArrayList<>());
        request.setSigners(new ArrayList<>());

        CustomerResponse customer = new CustomerResponse();
        customer.setId("123");
        customer.setType(CustomerType.PERSONAL.toString());
        customer.setStatus(CustomerStatus.ACTIVE.toString());
        customer.setSubType(null);

        Account account = new Account();
        account.setId("1");
        account.setAccountNumber("ACC-123");
        account.setAccountType(AccountType.SAVINGS);
        account.setCustomerId("123");
        account.setStatus(AccountStatus.ACTIVE);

        AccountResponse response = new AccountResponse();
        response.setId("1");
        response.setAccountNumber("ACC-123");
        response.setAccountType(AccountType.SAVINGS.toString());
        response.setCustomerId("123");
        response.setStatus(AccountStatus.ACTIVE.toString());

        when(customerService.getCustomerById("123")).thenReturn(Mono.just(customer));
        when(accountFactory.getAccount(any(), any())).thenReturn(account);
        when(accountRepository.findByCustomerId("123")).thenReturn(Flux.empty());
        when(accountRepository.save(any())).thenReturn(Mono.just(account));
        when(accountMapper.getAccountResponse(account)).thenReturn(response);

        StepVerifier.create(accountService.createAccount(Mono.just(request)))
            .expectNext(response)
            .verifyComplete();
    }

    @Test
    void getAccountById_Success() {
        String id = "123";
        Account account = new Account();
        AccountResponse response = new AccountResponse();

        when(accountRepository.findById(id)).thenReturn(Mono.just(account));
        when(accountMapper.getAccountResponse(account)).thenReturn(response);

        StepVerifier.create(accountService.getAccountById(id))
            .expectNext(response)
            .verifyComplete();
    }

    @Test
    void getAccountByAccountNumber_NotFound() {
        String accountNumber = "123";
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Mono.empty());

        StepVerifier.create(accountService.getAccountByAccountNumber(accountNumber))
            .expectError(AccountNotFoundException.class)
            .verify();
    }

    @Test
    void deleteAccount_Success() {
        String id = "123";
        Account account = new Account();

        when(accountRepository.findById(id)).thenReturn(Mono.just(account));
        when(accountRepository.save(any())).thenReturn(Mono.just(account));

        StepVerifier.create(accountService.deleteAccount(id))
            .verifyComplete();

        assertEquals(AccountStatus.INACTIVE, account.getStatus());
    }

    @Test
    void validateAccountType_Invalid() {
        AccountRequest request = new AccountRequest();
        request.setAccountType("INVALID");

        StepVerifier.create(accountService.createAccount(Mono.just(request)))
            .expectError(InvalidAccountTypeException.class)
            .verify();
    }

    @Test
    void createAccount_WhenBusinessAccountWithoutHolders_ShouldFail() {
        AccountRequest request = new AccountRequest();
        request.setAccountType(AccountType.CHECKING.toString());
        request.setCustomerId("123");
        request.setHolders(new ArrayList<>());
        request.setSigners(new ArrayList<>());

        CustomerResponse customer = new CustomerResponse();
        customer.setId("123");
        customer.setType(CustomerType.BUSINESS.toString());
        customer.setStatus(CustomerStatus.ACTIVE.toString());

        when(customerService.getCustomerById("123")).thenReturn(Mono.just(customer));

        StepVerifier.create(accountService.createAccount(Mono.just(request)))
            .expectErrorMatches(throwable -> throwable instanceof BadRequestException)
            .verify();
    }

    @Test
    void createAccount_WhenPersonalAccountWithHolders_ShouldFail() {
        AccountRequest request = new AccountRequest();
        request.setAccountType(AccountType.SAVINGS.toString());
        request.setCustomerId("123");

        AccountMember accountMember = new AccountMember();
        List<AccountMember> memberList = new ArrayList<>();
        memberList.add(accountMember);
        request.setHolders(memberList);
        request.setSigners(new ArrayList<>());

        CustomerResponse customer = new CustomerResponse();
        customer.setId("123");
        customer.setType(CustomerType.PERSONAL.toString());
        customer.setStatus(CustomerStatus.ACTIVE.toString());

        when(customerService.getCustomerById("123")).thenReturn(Mono.just(customer));
        when(accountRepository.findByCustomerId("123")).thenReturn(Flux.empty());

        StepVerifier.create(accountService.createAccount(Mono.just(request)))
            .expectErrorMatches(throwable ->
                throwable instanceof BadRequestException)
            .verify();
    }

    @Test
    void createAccount_WhenPersonalAccountWithSigners_ShouldFail() {
        AccountRequest request = new AccountRequest();
        request.setAccountType(AccountType.SAVINGS.toString());
        request.setCustomerId("123");

        AccountMember accountMember = new AccountMember();
        List<AccountMember> memberList = new ArrayList<>();
        memberList.add(accountMember);
        request.setHolders(new ArrayList<>());
        request.setSigners(memberList);

        CustomerResponse customer = new CustomerResponse();
        customer.setId("123");
        customer.setType(CustomerType.PERSONAL.toString());
        customer.setStatus(CustomerStatus.ACTIVE.toString());

        when(customerService.getCustomerById("123")).thenReturn(Mono.just(customer));
        when(accountRepository.findByCustomerId("123")).thenReturn(Flux.empty());

        StepVerifier.create(accountService.createAccount(Mono.just(request)))
            .expectErrorMatches(throwable -> throwable instanceof BadRequestException)
            .verify();
    }

    @Test
    void createAccount_RejectsDuplicateSavingsAccountForPersonalCustomer() {
        AccountRequest request = new AccountRequest();
        request.setAccountType(AccountType.SAVINGS.toString());
        request.setCustomerId("123");
        request.setHolders(new ArrayList<>());
        request.setSigners(new ArrayList<>());

        CustomerResponse customer = new CustomerResponse();
        customer.setId("123");
        customer.setType(CustomerType.PERSONAL.toString());
        customer.setStatus(CustomerStatus.ACTIVE.toString());
        customer.setSubType(null);

        List<Account> accountList = new ArrayList<>();
        Account account1 = new Account();
        account1.setId("1");
        account1.setAccountType(AccountType.SAVINGS);
        accountList.add(account1);
        Flux<Account> accountFlux = Flux.fromIterable(accountList);

        when(customerService.getCustomerById("123")).thenReturn(Mono.just(customer));
        when(accountRepository.findByCustomerId("123")).thenReturn(accountFlux);

        StepVerifier.create(accountService.createAccount(Mono.just(request)))
            .expectErrorMatches(throwable -> throwable instanceof BadRequestException)
            .verify();
    }

    @Test
    void createAccount_RejectsDuplicateCheckingAccountForPersonalCustomer() {
        AccountRequest request = new AccountRequest();
        request.setAccountType(AccountType.CHECKING.toString());
        request.setCustomerId("123");
        request.setHolders(new ArrayList<>());
        request.setSigners(new ArrayList<>());

        CustomerResponse customer = new CustomerResponse();
        customer.setId("123");
        customer.setType(CustomerType.PERSONAL.toString());
        customer.setStatus(CustomerStatus.ACTIVE.toString());
        customer.setSubType(null);

        List<Account> accountList = new ArrayList<>();
        Account account1 = new Account();
        account1.setId("1");
        account1.setAccountType(AccountType.CHECKING);
        accountList.add(account1);
        Flux<Account> accountFlux = Flux.fromIterable(accountList);

        when(customerService.getCustomerById("123")).thenReturn(Mono.just(customer));
        when(accountRepository.findByCustomerId("123")).thenReturn(accountFlux);

        StepVerifier.create(accountService.createAccount(Mono.just(request)))
            .expectErrorMatches(throwable -> throwable instanceof BadRequestException)
            .verify();
    }

    @Test
    void createAccount_RejectsSavingsAccountForBusinessCustomer() {
        AccountRequest request = new AccountRequest();
        request.setAccountType(AccountType.SAVINGS.toString());
        request.setCustomerId("123");
        request.setHolders(new ArrayList<>());
        request.setSigners(new ArrayList<>());

        CustomerResponse customer = new CustomerResponse();
        customer.setId("123");
        customer.setType(CustomerType.BUSINESS.toString());
        customer.setStatus(CustomerStatus.ACTIVE.toString());
        customer.setSubType(null);

        when(customerService.getCustomerById("123")).thenReturn(Mono.just(customer));

        StepVerifier.create(accountService.createAccount(Mono.just(request)))
            .expectErrorMatches(throwable -> throwable instanceof BadRequestException)
            .verify();
    }

    @Test
    void createAccount_RejectsFixedTermAccountForBusinessCustomer() {
        AccountRequest request = new AccountRequest();
        request.setAccountType(AccountType.FIXED_TERM.toString());
        request.setCustomerId("123");
        request.setHolders(new ArrayList<>());
        request.setSigners(new ArrayList<>());

        CustomerResponse customer = new CustomerResponse();
        customer.setId("123");
        customer.setType(CustomerType.BUSINESS.toString());
        customer.setStatus(CustomerStatus.ACTIVE.toString());
        customer.setSubType(null);

        when(customerService.getCustomerById("123")).thenReturn(Mono.just(customer));

        StepVerifier.create(accountService.createAccount(Mono.just(request)))
            .expectErrorMatches(throwable -> throwable instanceof BadRequestException)
            .verify();
    }

    @Test
    void createAccount_RejectsForInactiveCustomer() {
        AccountRequest request = new AccountRequest();
        request.setAccountType(AccountType.FIXED_TERM.toString());
        request.setCustomerId("123");
        request.setHolders(new ArrayList<>());
        request.setSigners(new ArrayList<>());

        CustomerResponse customer = new CustomerResponse();
        customer.setId("123");
        customer.setType(CustomerType.BUSINESS.toString());
        customer.setStatus(CustomerStatus.INACTIVE.toString());
        customer.setSubType(null);

        when(customerService.getCustomerById("123")).thenReturn(Mono.just(customer));

        StepVerifier.create(accountService.createAccount(Mono.just(request)))
            .expectErrorMatches(throwable -> throwable instanceof BadRequestException)
            .verify();
    }

    @Test
    void createAccount_RejectsSavingsAccountForVipCustomerWithNoCreditCards() {
        AccountRequest request = new AccountRequest();
        request.setAccountType(AccountType.SAVINGS.toString());
        request.setCustomerId("123");
        request.setHolders(new ArrayList<>());
        request.setSigners(new ArrayList<>());

        CustomerResponse customer = new CustomerResponse();
        customer.setId("123");
        customer.setType(CustomerType.PERSONAL.toString());
        customer.setStatus(CustomerStatus.ACTIVE.toString());
        customer.setSubType(PersonalCustomerType.VIP.toString());

        List<Account> accountList = new ArrayList<>();
        Account account1 = new Account();
        account1.setId("1");
        account1.setAccountType(AccountType.CHECKING);
        accountList.add(account1);
        Flux<Account> accountFlux = Flux.fromIterable(accountList);

        when(customerService.getCustomerById("123")).thenReturn(Mono.just(customer));
        when(accountRepository.findByCustomerId("123")).thenReturn(accountFlux);
        when(creditCardService.getCustomerCreditCards("123")).thenReturn(Flux.empty());

        StepVerifier.create(accountService.createAccount(Mono.just(request)))
            .expectErrorMatches(throwable -> throwable instanceof BadRequestException)
            .verify();
    }

    @Test
    void createAccount_SuccessSavingsAccountForVipCustomerWithCreditCards() {
        AccountRequest request = new AccountRequest();
        request.setAccountType(AccountType.SAVINGS.toString());
        request.setCustomerId("123");
        request.setHolders(new ArrayList<>());
        request.setSigners(new ArrayList<>());

        CustomerResponse customer = new CustomerResponse();
        customer.setId("123");
        customer.setType(CustomerType.PERSONAL.toString());
        customer.setStatus(CustomerStatus.ACTIVE.toString());
        customer.setSubType(PersonalCustomerType.VIP.toString());

        Account account = new Account();
        account.setId("1");
        account.setAccountNumber("ACC-123");
        account.setAccountType(AccountType.SAVINGS);
        account.setCustomerId("123");
        account.setStatus(AccountStatus.ACTIVE);

        AccountResponse response = new AccountResponse();
        response.setId("1");
        response.setAccountNumber("ACC-123");
        response.setAccountType(AccountType.SAVINGS.toString());
        response.setCustomerId("123");
        response.setStatus(AccountStatus.ACTIVE.toString());

        List<CreditCardResponse> creditCardResponses = new ArrayList<>();
        CreditCardResponse creditCardResponse = new CreditCardResponse();
        creditCardResponse.setCardNumber("123456789101");
        creditCardResponse.setId("1");
        creditCardResponses.add(creditCardResponse);
        Flux<CreditCardResponse> creditCardResponseFlux = Flux.fromIterable(creditCardResponses);

        when(customerService.getCustomerById("123")).thenReturn(Mono.just(customer));
        when(accountFactory.getAccount(any(), any())).thenReturn(account);
        when(accountRepository.findByCustomerId("123")).thenReturn(Flux.empty());
        when(accountRepository.save(any())).thenReturn(Mono.just(account));
        when(accountMapper.getAccountResponse(account)).thenReturn(response);
        when(creditCardService.getCustomerCreditCards("123")).thenReturn(creditCardResponseFlux);

        StepVerifier.create(accountService.createAccount(Mono.just(request)))
            .expectNext(response)
            .verifyComplete();
    }

    @Test
    void createAccount_RejectsCheckingAccountForVipCustomer() {
        AccountRequest request = new AccountRequest();
        request.setAccountType(AccountType.CHECKING.toString());
        request.setCustomerId("123");
        request.setHolders(new ArrayList<>());
        request.setSigners(new ArrayList<>());

        CustomerResponse customer = new CustomerResponse();
        customer.setId("123");
        customer.setType(CustomerType.PERSONAL.toString());
        customer.setStatus(CustomerStatus.ACTIVE.toString());
        customer.setSubType(PersonalCustomerType.VIP.toString());

        Account account = new Account();
        account.setId("1");
        account.setAccountNumber("ACC-123");
        account.setAccountType(AccountType.SAVINGS);
        account.setCustomerId("123");
        account.setStatus(AccountStatus.ACTIVE);

        AccountResponse response = new AccountResponse();
        response.setId("1");
        response.setAccountNumber("ACC-123");
        response.setAccountType(AccountType.SAVINGS.toString());
        response.setCustomerId("123");
        response.setStatus(AccountStatus.ACTIVE.toString());

        List<CreditCardResponse> creditCardResponses = new ArrayList<>();
        CreditCardResponse creditCardResponse = new CreditCardResponse();
        creditCardResponse.setCardNumber("123456789101");
        creditCardResponse.setId("1");
        creditCardResponses.add(creditCardResponse);
        Flux<CreditCardResponse> creditCardResponseFlux = Flux.fromIterable(creditCardResponses);

        when(customerService.getCustomerById("123")).thenReturn(Mono.just(customer));
        when(accountFactory.getAccount(any(), any())).thenReturn(account);
        when(accountRepository.findByCustomerId("123")).thenReturn(Flux.empty());
        when(accountRepository.save(any())).thenReturn(Mono.just(account));
        when(accountMapper.getAccountResponse(account)).thenReturn(response);
        when(creditCardService.getCustomerCreditCards("123")).thenReturn(creditCardResponseFlux);

        StepVerifier.create(accountService.createAccount(Mono.just(request)))
            .expectErrorMatches(throwable -> throwable instanceof BadRequestException)
            .verify();
    }

    @Test
    void createAccount_RejectsFixedTermAccountForVipCustomer() {
        AccountRequest request = new AccountRequest();
        request.setAccountType(AccountType.FIXED_TERM.toString());
        request.setCustomerId("123");
        request.setHolders(new ArrayList<>());
        request.setSigners(new ArrayList<>());

        CustomerResponse customer = new CustomerResponse();
        customer.setId("123");
        customer.setType(CustomerType.PERSONAL.toString());
        customer.setStatus(CustomerStatus.ACTIVE.toString());
        customer.setSubType(PersonalCustomerType.VIP.toString());

        Account account = new Account();
        account.setId("1");
        account.setAccountNumber("ACC-123");
        account.setAccountType(AccountType.SAVINGS);
        account.setCustomerId("123");
        account.setStatus(AccountStatus.ACTIVE);

        AccountResponse response = new AccountResponse();
        response.setId("1");
        response.setAccountNumber("ACC-123");
        response.setAccountType(AccountType.SAVINGS.toString());
        response.setCustomerId("123");
        response.setStatus(AccountStatus.ACTIVE.toString());

        List<CreditCardResponse> creditCardResponses = new ArrayList<>();
        CreditCardResponse creditCardResponse = new CreditCardResponse();
        creditCardResponse.setCardNumber("123456789101");
        creditCardResponse.setId("1");
        creditCardResponses.add(creditCardResponse);
        Flux<CreditCardResponse> creditCardResponseFlux = Flux.fromIterable(creditCardResponses);

        when(customerService.getCustomerById("123")).thenReturn(Mono.just(customer));
        when(accountFactory.getAccount(any(), any())).thenReturn(account);
        when(accountRepository.findByCustomerId("123")).thenReturn(Flux.empty());
        when(accountRepository.save(any())).thenReturn(Mono.just(account));
        when(accountMapper.getAccountResponse(account)).thenReturn(response);
        when(creditCardService.getCustomerCreditCards("123")).thenReturn(creditCardResponseFlux);

        StepVerifier.create(accountService.createAccount(Mono.just(request)))
            .expectErrorMatches(throwable -> throwable instanceof BadRequestException)
            .verify();
    }

    @Test
    void createAccount_RejectsCheckingAccountForPymeBusinessWithNoCreditCards() {
        AccountRequest request = new AccountRequest();
        request.setAccountType(AccountType.CHECKING.toString());
        request.setCustomerId("123");
        request.setHolders(new ArrayList<>());
        request.setSigners(new ArrayList<>());

        CustomerResponse customer = new CustomerResponse();
        customer.setId("123");
        customer.setType(CustomerType.BUSINESS.toString());
        customer.setStatus(CustomerStatus.ACTIVE.toString());
        customer.setSubType(BusinessCustomerType.PYME.toString());

        List<Account> accountList = new ArrayList<>();
        Account account1 = new Account();
        account1.setId("1");
        account1.setAccountType(AccountType.CHECKING);
        accountList.add(account1);
        Flux<Account> accountFlux = Flux.fromIterable(accountList);

        when(customerService.getCustomerById("123")).thenReturn(Mono.just(customer));
        when(accountRepository.findByCustomerId("123")).thenReturn(accountFlux);
        when(creditCardService.getCustomerCreditCards("123")).thenReturn(Flux.empty());

        StepVerifier.create(accountService.createAccount(Mono.just(request)))
            .expectErrorMatches(throwable -> throwable instanceof BadRequestException)
            .verify();
    }

    @Test
    void createAccount_SuccessCheckingAccountForPymeBusinessWithCreditCards() {
        AccountRequest request = new AccountRequest();
        request.setAccountType(AccountType.CHECKING.toString());
        request.setCustomerId("123");

        AccountMember accountMember = new AccountMember();
        List<AccountMember> memberList = new ArrayList<>();
        memberList.add(accountMember);
        request.setHolders(memberList);
        request.setSigners(memberList);

        CustomerResponse customer = new CustomerResponse();
        customer.setId("123");
        customer.setType(CustomerType.BUSINESS.toString());
        customer.setStatus(CustomerStatus.ACTIVE.toString());
        customer.setSubType(BusinessCustomerType.PYME.toString());

        Account account = new Account();
        account.setId("1");
        account.setAccountNumber("ACC-123");
        account.setAccountType(AccountType.CHECKING);
        account.setCustomerId("123");
        account.setStatus(AccountStatus.ACTIVE);

        AccountResponse response = new AccountResponse();
        response.setId("1");
        response.setAccountNumber("ACC-123");
        response.setAccountType(AccountType.CHECKING.toString());
        response.setCustomerId("123");
        response.setStatus(AccountStatus.ACTIVE.toString());

        List<CreditCardResponse> creditCardResponses = new ArrayList<>();
        CreditCardResponse creditCardResponse = new CreditCardResponse();
        creditCardResponse.setCardNumber("123456789101");
        creditCardResponse.setId("1");
        creditCardResponses.add(creditCardResponse);
        Flux<CreditCardResponse> creditCardResponseFlux = Flux.fromIterable(creditCardResponses);

        when(customerService.getCustomerById("123")).thenReturn(Mono.just(customer));
        when(accountFactory.getAccount(any(), any())).thenReturn(account);
        when(accountRepository.findByCustomerId("123")).thenReturn(Flux.empty());
        when(accountRepository.save(any())).thenReturn(Mono.just(account));
        when(accountMapper.getAccountResponse(account)).thenReturn(response);
        when(creditCardService.getCustomerCreditCards("123")).thenReturn(creditCardResponseFlux);

        StepVerifier.create(accountService.createAccount(Mono.just(request)))
            .expectNext(response)
            .verifyComplete();
    }

    @Test
    void createAccount_RejectsSavingsAccountForPymeBusiness() {
        AccountRequest request = new AccountRequest();
        request.setAccountType(AccountType.SAVINGS.toString());
        request.setCustomerId("123");

        AccountMember accountMember = new AccountMember();
        List<AccountMember> memberList = new ArrayList<>();
        memberList.add(accountMember);
        request.setHolders(memberList);
        request.setSigners(memberList);

        CustomerResponse customer = new CustomerResponse();
        customer.setId("123");
        customer.setType(CustomerType.BUSINESS.toString());
        customer.setStatus(CustomerStatus.ACTIVE.toString());
        customer.setSubType(BusinessCustomerType.PYME.toString());

        Account account = new Account();
        account.setId("1");
        account.setAccountNumber("ACC-123");
        account.setAccountType(AccountType.CHECKING);
        account.setCustomerId("123");
        account.setStatus(AccountStatus.ACTIVE);

        AccountResponse response = new AccountResponse();
        response.setId("1");
        response.setAccountNumber("ACC-123");
        response.setAccountType(AccountType.CHECKING.toString());
        response.setCustomerId("123");
        response.setStatus(AccountStatus.ACTIVE.toString());

        List<CreditCardResponse> creditCardResponses = new ArrayList<>();
        CreditCardResponse creditCardResponse = new CreditCardResponse();
        creditCardResponse.setCardNumber("123456789101");
        creditCardResponse.setId("1");
        creditCardResponses.add(creditCardResponse);
        Flux<CreditCardResponse> creditCardResponseFlux = Flux.fromIterable(creditCardResponses);

        when(customerService.getCustomerById("123")).thenReturn(Mono.just(customer));
        when(accountFactory.getAccount(any(), any())).thenReturn(account);
        when(accountRepository.findByCustomerId("123")).thenReturn(Flux.empty());
        when(accountRepository.save(any())).thenReturn(Mono.just(account));
        when(accountMapper.getAccountResponse(account)).thenReturn(response);
        when(creditCardService.getCustomerCreditCards("123")).thenReturn(creditCardResponseFlux);

        StepVerifier.create(accountService.createAccount(Mono.just(request)))
            .expectErrorMatches(throwable -> throwable instanceof BadRequestException)
            .verify();
    }

    @Test
    void createAccount_RejectsFixedTermAccountForPymeBusiness() {
        AccountRequest request = new AccountRequest();
        request.setAccountType(AccountType.FIXED_TERM.toString());
        request.setCustomerId("123");

        AccountMember accountMember = new AccountMember();
        List<AccountMember> memberList = new ArrayList<>();
        memberList.add(accountMember);
        request.setHolders(memberList);
        request.setSigners(memberList);

        CustomerResponse customer = new CustomerResponse();
        customer.setId("123");
        customer.setType(CustomerType.BUSINESS.toString());
        customer.setStatus(CustomerStatus.ACTIVE.toString());
        customer.setSubType(BusinessCustomerType.PYME.toString());

        Account account = new Account();
        account.setId("1");
        account.setAccountNumber("ACC-123");
        account.setAccountType(AccountType.CHECKING);
        account.setCustomerId("123");
        account.setStatus(AccountStatus.ACTIVE);

        AccountResponse response = new AccountResponse();
        response.setId("1");
        response.setAccountNumber("ACC-123");
        response.setAccountType(AccountType.CHECKING.toString());
        response.setCustomerId("123");
        response.setStatus(AccountStatus.ACTIVE.toString());

        List<CreditCardResponse> creditCardResponses = new ArrayList<>();
        CreditCardResponse creditCardResponse = new CreditCardResponse();
        creditCardResponse.setCardNumber("123456789101");
        creditCardResponse.setId("1");
        creditCardResponses.add(creditCardResponse);
        Flux<CreditCardResponse> creditCardResponseFlux = Flux.fromIterable(creditCardResponses);

        when(customerService.getCustomerById("123")).thenReturn(Mono.just(customer));
        when(accountFactory.getAccount(any(), any())).thenReturn(account);
        when(accountRepository.findByCustomerId("123")).thenReturn(Flux.empty());
        when(accountRepository.save(any())).thenReturn(Mono.just(account));
        when(accountMapper.getAccountResponse(account)).thenReturn(response);
        when(creditCardService.getCustomerCreditCards("123")).thenReturn(creditCardResponseFlux);

        StepVerifier.create(accountService.createAccount(Mono.just(request)))
            .expectErrorMatches(throwable -> throwable instanceof BadRequestException)
            .verify();
    }

    @Test
    void getAccountsByCustomerId_EmptyResult() {
        String customerId = "123";

        when(accountRepository.findByCustomerId(customerId))
            .thenReturn(Flux.empty());

        StepVerifier.create(accountService.getAccountsByCustomerId(customerId))
            .expectNextCount(0)
            .verifyComplete();
    }

    @Test
    void getAccountBalanceByAccountNumber_Success() {
        Account account = new Account();
        account.setBalance(new BigDecimal("100.0"));
        account.setAccountNumber("12345");

        AccountBalanceResponse accountBalanceResponse = new AccountBalanceResponse();
        accountBalanceResponse.setBalance(new BigDecimal("100.0"));

        when(accountRepository.findByAccountNumber("12345")).thenReturn(Mono.just(account));
        when(accountMapper.getAccountBalanceResponse(account)).thenReturn(accountBalanceResponse);

        StepVerifier.create(accountService.getAccountBalanceByAccountNumber("12345"))
            .expectNext(accountBalanceResponse)
            .verifyComplete();
    }

    @Test
    void getAccountBalanceByAccountNumber_NotFound() {
        when(accountRepository.findByAccountNumber("12345")).thenReturn(Mono.empty());

        StepVerifier.create(accountService.getAccountBalanceByAccountNumber("12345"))
            .expectError(AccountNotFoundException.class)
            .verify();
    }

    @Test
    void updateAccount_Success() {
        String id = "123";
        Account existingAccount = new Account();
        existingAccount.setId(id);
        existingAccount.setAccountType(AccountType.SAVINGS);
        existingAccount.setStatus(AccountStatus.ACTIVE);

        AccountPatchRequest patchRequest = new AccountPatchRequest();
        patchRequest.setBalance(new BigDecimal("200.0"));
        patchRequest.setMonthlyMovements(5);

        Account updatedAccount = new Account();
        updatedAccount.setId(id);
        updatedAccount.setBalance(new BigDecimal("200.0"));
        updatedAccount.setMonthlyMovements(5);
        updatedAccount.setStatus(AccountStatus.ACTIVE);

        AccountResponse expectedResponse = new AccountResponse();
        expectedResponse.setId(id);
        expectedResponse.setBalance(new BigDecimal("200.0"));
        expectedResponse.setMonthlyMovements(5);
        expectedResponse.setStatus(AccountStatus.ACTIVE.toString());

        when(accountRepository.findById(id)).thenReturn(Mono.just(existingAccount));
        when(accountMapper.getAccountUpdateEntity(patchRequest, existingAccount)).thenReturn(updatedAccount);
        when(accountRepository.save(updatedAccount)).thenReturn(Mono.just(updatedAccount));
        when(accountMapper.getAccountResponse(updatedAccount)).thenReturn(expectedResponse);

        StepVerifier.create(accountService.updateAccount(id, Mono.just(patchRequest)))
            .expectNext(expectedResponse)
            .verifyComplete();
    }

    @Test
    void updateAccount_NotFound() {
        String id = "123";
        AccountPatchRequest request = new AccountPatchRequest();

        when(accountRepository.findById(id)).thenReturn(Mono.empty());

        StepVerifier.create(accountService.updateAccount(id, Mono.just(request)))
            .expectError(AccountNotFoundException.class)
            .verify();
    }
}
