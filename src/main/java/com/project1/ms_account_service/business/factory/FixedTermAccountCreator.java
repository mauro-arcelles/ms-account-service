package com.project1.ms_account_service.business.factory;

import com.project1.ms_account_service.business.mapper.AccountMapper;
import com.project1.ms_account_service.model.AccountRequest;
import com.project1.ms_account_service.model.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Component
public class FixedTermAccountCreator implements AccountCreator {

    @Value("${account.config.fixedterm.maxMonthlyMovements}")
    private Integer fixedTermMaxMonthlyMovements;

    @Value("${account.config.fixedterm.availableDayForMovements}")
    private Integer availableDayForMovements;

    @Value("${account.config.fixedterm.maxMonthlyMovementsNoFee}")
    private Integer fixedTermMaxMonthlyMovementsNoFee;

    @Value("${account.config.fixedterm.transactionCommissionFeePercentage}")
    private BigDecimal fixedTermTransactionCommissionFeePercentage;

    @Autowired
    private AccountMapper accountMapper;

    @Override
    public Account createAccount(AccountRequest request, CustomerType customerType) {
        com.project1.ms_account_service.model.FixedTermAccount fixedTermRequest = (com.project1.ms_account_service.model.FixedTermAccount) request;

        return FixedTermAccount.builder()
            .maxMonthlyMovements(fixedTermMaxMonthlyMovements)
            .termInMonths(fixedTermRequest.getTermInMonths())
            .availableDayForMovements(availableDayForMovements)
            .endDay(LocalDateTime.now().plusMonths(fixedTermRequest.getTermInMonths()))
            .maintenanceFee(BigDecimal.ZERO)
            .maxMonthlyMovementsNoFee(fixedTermMaxMonthlyMovementsNoFee)
            .transactionCommissionFeePercentage(fixedTermTransactionCommissionFeePercentage)
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
