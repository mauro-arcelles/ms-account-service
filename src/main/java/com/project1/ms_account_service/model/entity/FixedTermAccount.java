package com.project1.ms_account_service.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
public class FixedTermAccount extends Account {
    private LocalDateTime endDay;
    private Integer maxMonthlyMovements;
    private LocalDateTime availableDayForMovements;
}
