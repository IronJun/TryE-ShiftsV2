package com.ispw.tryeshifts.excpetion;

public class MembershipPendingException extends RuntimeException {
    public MembershipPendingException(String message) {
        super(message);
    }
}
