package com.project1.ms_account_service.business;

import com.project1.ms_account_service.exception.BadRequestException;
import com.project1.ms_account_service.exception.InvalidAccountTypeException;
import com.project1.ms_account_service.model.AccountBalanceResponse;
import com.project1.ms_account_service.model.AccountPatchRequest;
import com.project1.ms_account_service.model.AccountRequest;
import com.project1.ms_account_service.model.AccountResponse;
import com.project1.ms_account_service.model.entity.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class AccountMapper {
    @Value("${account.config.checking.maintenanceFee}")
    private Double checkingAccountMaintenanceFee;

    @Value("${account.config.fixedterm.maxMonthlyMovements}")
    private Integer fixedTermMaxMonthlyMovements;

    @Value("${account.config.fixedterm.availableDayForMovements}")
    private Integer availableDayForMovements;

    @Value("${account.config.savings.maxMonthlyMovements}")
    private Integer savingsMaxMonthlyMovements;

    public Account getAccountCreationEntity(AccountRequest request) {
        Account account;
        AccountType accountType = AccountType.valueOf(request.getAccountType());
        switch (accountType) {
            case SAVINGS:
                account = SavingsAccount.builder()
                        .maintenanceFee(0.0)
                        .maxMonthlyMovements(savingsMaxMonthlyMovements)
                        .build();
                break;
            case CHECKING:
                account = CheckingAccount.builder()
                        .maintenanceFee(checkingAccountMaintenanceFee)
                        .build();
                break;
            case FIXED_TERM:
                com.project1.ms_account_service.model.FixedTermAccount fixedTermRequest = (com.project1.ms_account_service.model.FixedTermAccount) request;
                account = FixedTermAccount.builder()
                        .maxMonthlyMovements(fixedTermMaxMonthlyMovements)
                        .termInMonths(fixedTermRequest.getTermInMonths())
                        .availableDayForMovements(availableDayForMovements)
                        .endDay(LocalDateTime.now().plusMonths(fixedTermRequest.getTermInMonths()))
                        .maintenanceFee(0.0)
                        .build();
                break;
            default:
                throw new InvalidAccountTypeException();
        }

        // Add common account properties
        account = account.toBuilder()
                .monthlyMovements(0)
                .accountType(AccountType.valueOf(request.getAccountType()))
                .customerId(request.getCustomerId())
                .balance(request.getInitialBalance())
                .creationDate(LocalDateTime.now())
                .status(AccountStatus.ACTIVE)
                .accountNumber(Account.generateAccountNumber())
                .holders(request.getHolders()
                        .stream()
                        .map(this::getAccountMember)
                        .collect(Collectors.toList())
                )
                .signers(request.getSigners()
                        .stream()
                        .map(this::getAccountMember)
                        .collect(Collectors.toList())
                )
                .build();

        return account;
    }

    public AccountMember getAccountMember(com.project1.ms_account_service.model.AccountMember accountMemberRequest) {
        return AccountMember.builder()
                .dni(accountMemberRequest.getDni())
                .name(accountMemberRequest.getName())
                .lastName(accountMemberRequest.getLastName())
                .email(accountMemberRequest.getEmail())
                .build();
    }

    public com.project1.ms_account_service.model.AccountMember getAccountMemberResponse(AccountMember accountMember) {
        com.project1.ms_account_service.model.AccountMember response = new com.project1.ms_account_service.model.AccountMember();
        response.setDni(accountMember.getDni());
        response.setEmail(accountMember.getEmail());
        response.setName(accountMember.getName());
        response.setLastName(accountMember.getLastName());
        return response;
    }

    public Account getAccountUpdateEntity(AccountPatchRequest request, Account existingAccount) {
        if (request.getBalance() == null &&
                request.getMonthlyMovements() == null &&
                request.getStatus() == null) {
            throw new BadRequestException("At least one field must be provided");
        }
        Optional<Integer> optionalMonthlyMovements = Optional.ofNullable(request.getMonthlyMovements());
        if (optionalMonthlyMovements.isPresent()) {
            if (existingAccount.getAccountType().equals(AccountType.SAVINGS)) {
                SavingsAccount savingsAccount = (SavingsAccount) existingAccount;
                if (optionalMonthlyMovements.get() > savingsAccount.getMaxMonthlyMovements()) {
                    throw new BadRequestException("Max monthly movements limit reached. The monthly movements available: " + savingsAccount.getMaxMonthlyMovements());
                }
            }
            if (existingAccount.getAccountType().equals(AccountType.FIXED_TERM)) {
                FixedTermAccount fixedTermAccount = (FixedTermAccount) existingAccount;
                if (optionalMonthlyMovements.get() > fixedTermAccount.getMaxMonthlyMovements()) {
                    throw new BadRequestException("Max monthly movements limit reached. The monthly movements available: " + fixedTermAccount.getMaxMonthlyMovements());
                }
            }
        }
        Optional.ofNullable(request.getBalance()).ifPresent(existingAccount::setBalance);
        Optional.ofNullable(request.getMonthlyMovements()).ifPresent(existingAccount::setMonthlyMovements);
        Optional.ofNullable(request.getStatus())
                .ifPresent(status -> existingAccount.setStatus(AccountStatus.valueOf(status)));
        return existingAccount;
    }

    public AccountResponse getAccountResponse(Account account) {
        AccountResponse accountResponse = new AccountResponse();
        accountResponse.setId(account.getId());
        accountResponse.setAccountNumber(account.getAccountNumber());
        accountResponse.setAccountType(account.getAccountType().toString());
        accountResponse.setBalance(account.getBalance());
        accountResponse.setCreationDate(account.getCreationDate());
        accountResponse.setCustomerId(account.getCustomerId());
        accountResponse.setMaintenanceFee(account.getMaintenanceFee());
        accountResponse.setMonthlyMovements(account.getMonthlyMovements());
        accountResponse.setStatus(account.getStatus().toString());
        if (account.getAccountType().equals(AccountType.SAVINGS)) {
            accountResponse.setMaxMonthlyMovements(((SavingsAccount) account).getMonthlyMovements());
        }
        if (account.getAccountType().equals(AccountType.FIXED_TERM)) {
            FixedTermAccount fixedTermAccount = (FixedTermAccount) account;
            accountResponse.setMaxMonthlyMovements(fixedTermAccount.getMaxMonthlyMovements());
            accountResponse.setEndDay(fixedTermAccount.getEndDay());
            accountResponse.setAvailableDayForMovements(fixedTermAccount.getAvailableDayForMovements());
        }
        if (!account.getHolders().isEmpty()) {
            accountResponse.setHolders(account.getHolders()
                    .stream()
                    .map(this::getAccountMemberResponse)
                    .collect(Collectors.toList())
            );
        }
        if (!account.getSigners().isEmpty()) {
            accountResponse.setSigners(account.getSigners()
                    .stream()
                    .map(this::getAccountMemberResponse)
                    .collect(Collectors.toList())
            );
        }
        return accountResponse;
    }

    public AccountBalanceResponse getAccountBalanceResponse(Account account) {
        AccountBalanceResponse accountBalanceResponse = new AccountBalanceResponse();
        accountBalanceResponse.setBalance(account.getBalance());
        return accountBalanceResponse;
    }
}
