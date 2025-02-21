package com.project1.ms_account_service.business;

import com.project1.ms_account_service.exception.BadRequestException;
import com.project1.ms_account_service.model.AccountBalanceResponse;
import com.project1.ms_account_service.model.AccountPatchRequest;
import com.project1.ms_account_service.model.AccountResponse;
import com.project1.ms_account_service.model.entity.*;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class AccountMapper {

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
                    throw new BadRequestException(
                        "Max monthly movements limit reached. The monthly movements available: " + savingsAccount.getMaxMonthlyMovements());
                }
            }
            if (existingAccount.getAccountType().equals(AccountType.FIXED_TERM)) {
                FixedTermAccount fixedTermAccount = (FixedTermAccount) existingAccount;
                if (optionalMonthlyMovements.get() > fixedTermAccount.getMaxMonthlyMovements()) {
                    throw new BadRequestException(
                        "Max monthly movements limit reached. The monthly movements available: " + fixedTermAccount.getMaxMonthlyMovements());
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
        Optional.ofNullable(account.getAccountType()).ifPresent(accountType -> accountResponse.setAccountType(accountType.toString()));
        Optional.ofNullable(account.getCustomerType()).ifPresent(customerType -> accountResponse.setCustomerType(customerType.toString()));
        accountResponse.setBalance(account.getBalance());
        accountResponse.setCreationDate(account.getCreationDate());
        accountResponse.setCustomerId(account.getCustomerId());
        accountResponse.setMaintenanceFee(account.getMaintenanceFee());
        accountResponse.setMonthlyMovements(account.getMonthlyMovements());
        Optional.ofNullable(account.getStatus()).ifPresent(accountStatus -> accountResponse.setStatus(accountStatus.toString()));
        if (AccountType.SAVINGS.equals(account.getAccountType())) {
            accountResponse.setMaxMonthlyMovements(((SavingsAccount) account).getMaxMonthlyMovements());
        }
        if (AccountType.FIXED_TERM.equals(account.getAccountType())) {
            FixedTermAccount fixedTermAccount = (FixedTermAccount) account;
            accountResponse.setMaxMonthlyMovements(fixedTermAccount.getMaxMonthlyMovements());
            accountResponse.setEndDay(fixedTermAccount.getEndDay());
            accountResponse.setAvailableDayForMovements(fixedTermAccount.getAvailableDayForMovements());
        }
        Optional.ofNullable(account.getHolders()).ifPresent(accountMembers -> {
            if (!accountMembers.isEmpty()) {
                accountResponse.setHolders(account.getHolders()
                    .stream()
                    .map(this::getAccountMemberResponse)
                    .collect(Collectors.toList())
                );
            }
        });
        Optional.ofNullable(account.getSigners()).ifPresent(accountMembers -> {
            if (!accountMembers.isEmpty()) {
                accountResponse.setSigners(account.getSigners()
                    .stream()
                    .map(this::getAccountMemberResponse)
                    .collect(Collectors.toList())
                );
            }
        });
        Optional.ofNullable(account.getMaxMonthlyMovementsNoFee()).ifPresent(accountResponse::setMaxMonthlyMovementsNoFee);
        Optional.ofNullable(account.getTransactionCommissionFeePercentage()).ifPresent(accountResponse::setTransactionCommissionFeePercentage);
        return accountResponse;
    }

    public AccountBalanceResponse getAccountBalanceResponse(Account account) {
        AccountBalanceResponse accountBalanceResponse = new AccountBalanceResponse();
        accountBalanceResponse.setBalance(account.getBalance());
        return accountBalanceResponse;
    }

    public AccountMember getAccountMember(com.project1.ms_account_service.model.AccountMember accountMemberRequest) {
        return AccountMember.builder()
            .dni(accountMemberRequest.getDni())
            .name(accountMemberRequest.getName())
            .lastName(accountMemberRequest.getLastName())
            .email(accountMemberRequest.getEmail())
            .build();
    }
}
