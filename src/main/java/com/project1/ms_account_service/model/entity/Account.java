package com.project1.ms_account_service.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Document(collection = "accounts")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@TypeAlias("account")
public class Account {
    @Id
    private String id;
    private String accountNumber;
    private AccountType accountType;
    private CustomerType customerType;
    private BigDecimal balance;
    private String customerId;
    private LocalDateTime creationDate;
    private AccountStatus status;
    private Integer monthlyMovements;
    private Double maintenanceFee;
    private List<AccountMember> holders;
    private List<AccountMember> signers;
    private Integer maxMonthlyMovementsNoFee;
    private BigDecimal transactionCommissionFeePercentage;

    public static String generateAccountNumber() {
        return "ACC-" + UUID.randomUUID().toString();
    }
}
