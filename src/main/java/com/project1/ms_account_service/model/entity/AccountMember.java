package com.project1.ms_account_service.model.entity;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
public class AccountMember {
    private String name;
    private String lastName;
    private String dni;
    private String email;
}
