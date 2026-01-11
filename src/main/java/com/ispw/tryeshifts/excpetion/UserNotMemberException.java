package com.ispw.tryeshifts.excpetion;

public class UserNotMemberException extends Exception {
    public UserNotMemberException(String message) {
        super(message);
    }
}
