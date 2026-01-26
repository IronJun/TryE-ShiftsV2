package com.ispw.tryeshifts.excpetion;

public class UserNotMemberException extends SecuriryException {
    public UserNotMemberException(String message) {
        super(message);
    }
}
