package com.ispw.tryeshiftsv2.excpetion;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
