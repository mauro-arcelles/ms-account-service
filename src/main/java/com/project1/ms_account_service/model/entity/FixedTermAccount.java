package com.project1.ms_account_service.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@TypeAlias("fixedTermAccount")
public class FixedTermAccount extends Account {
    private LocalDateTime endDay;
    private Integer maxMonthlyMovements;
    private Integer availableDayForMovements;
    private Integer termInMonths;
}
