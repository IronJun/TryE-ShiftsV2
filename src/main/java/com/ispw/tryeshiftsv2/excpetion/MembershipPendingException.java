package com.ispw.tryeshiftsv2.excpetion;

public class MembershipPendingException extends RuntimeException {
    public MembershipPendingException(String message) {
        super(message);
    }
}
