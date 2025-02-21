package com.project1.ms_account_service.business.factory;

import com.project1.ms_account_service.business.AccountMapper;
import com.project1.ms_account_service.model.AccountRequest;
import com.project1.ms_account_service.model.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Component
public class SavingsAccountCreator implements AccountCreator {

    @Value("${account.config.savings.maxMonthlyMovements}")
    private Integer savingsMaxMonthlyMovements;

    @Value("${account.config.savings.maxMonthlyMovementsNoFee}")
    private Integer savingsMaxMonthlyMovementsNoFee;

    @Value("${account.config.savings.transactionCommissionFeePercentage}")
    private BigDecimal savingsTransactionCommissionFeePercentage;

    @Autowired
    private AccountMapper accountMapper;

    @Override
    public Account createAccount(AccountRequest request, CustomerType customerType) {
        return SavingsAccount.builder()
            .maintenanceFee(BigDecimal.ZERO)
            .maxMonthlyMovements(savingsMaxMonthlyMovements)
            .maxMonthlyMovementsNoFee(savingsMaxMonthlyMovementsNoFee)
            .transactionCommissionFeePercentage(savingsTransactionCommissionFeePercentage)
            .monthlyMovements(0)
            .accountType(AccountType.valueOf(request.getAccountType()))
            .customerType(customerType)
            .customerId(request.getCustomerId())
            .balance(request.getInitialBalance())
            .creationDate(LocalDateTime.now())
            .status(AccountStatus.ACTIVE)
            .accountNumber(Account.generateAccountNumber())
            .holders(request.getHolders()
                .stream()
                .map(accountMapper::getAccountMember)
                .collect(Collectors.toList())
            )
            .signers(request.getSigners()
                .stream()
                .map(accountMapper::getAccountMember)
                .collect(Collectors.toList())
            )
            .build();
    }
}
