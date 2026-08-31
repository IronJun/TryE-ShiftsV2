package com.ispw.tryeshifts.exception;

public class MembershipPendingException extends ValidationException {
    public MembershipPendingException(String membership,String id) {
        super(membership+ " for " + id +" not found", membership);
    }
}
