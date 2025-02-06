package com.project1.ms_account_service.model.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "accounts")
public abstract class Account {
    @Id
    private String id;
    private String accountNumber;
    private AccountType accountType;
    private Double balance;
    private String customerId;
    private LocalDateTime creationDate;
    private AccountStatus status;
    private Integer monthlyMovements;
    private Double maintenanceFee;
}
