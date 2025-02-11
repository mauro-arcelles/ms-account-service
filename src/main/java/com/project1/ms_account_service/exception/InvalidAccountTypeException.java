package com.project1.ms_account_service.exception;

public class InvalidAccountTypeException extends RuntimeException {
    public InvalidAccountTypeException() {
        super("Invalid account type. Should be one of: SAVINGS|CHECKING|FIXED_TERM");
    }
}
