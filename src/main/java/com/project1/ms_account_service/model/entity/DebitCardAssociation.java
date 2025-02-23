package com.project1.ms_account_service.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class DebitCardAssociation {
    private String accountId;

    private Integer position;
}
