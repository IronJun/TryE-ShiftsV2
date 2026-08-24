package com.ispw.tryeshifts.excpetion;

public class InvalidCredentialException extends BaseException {
    public InvalidCredentialException(String message) {
        super(message);
    }
    public InvalidCredentialException(String message, Throwable cause) {
        super(message, cause);
    }
}
