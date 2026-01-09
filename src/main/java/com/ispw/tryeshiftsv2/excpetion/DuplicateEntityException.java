package com.ispw.tryeshiftsv2.excpetion;

public class DuplicateEntityException extends RuntimeException {
    public DuplicateEntityException(String message) {
        super(message);
    }
}
