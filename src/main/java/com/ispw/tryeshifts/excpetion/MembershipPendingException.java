package com.ispw.tryeshifts.excpetion;

public class MembershipPendingException extends ValidationException {
    public MembershipPendingException(String membership,String id) {
        super(membership+ " per " + id +" non trovata", membership);
    }
}
