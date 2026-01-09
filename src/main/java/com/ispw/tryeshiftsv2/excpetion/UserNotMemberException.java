package com.ispw.tryeshiftsv2.excpetion;

public class UserNotMemberException extends Exception {
    public UserNotMemberException(String message) {
        super(message);
    }
}
