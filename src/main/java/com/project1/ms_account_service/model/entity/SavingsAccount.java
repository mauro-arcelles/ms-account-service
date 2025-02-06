package com.project1.ms_account_service.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SavingsAccount extends Account {
    private Integer maxMonthlyMovements;
}
