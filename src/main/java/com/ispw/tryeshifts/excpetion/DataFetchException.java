package com.ispw.tryeshifts.excpetion;

public class DataFetchException extends BaseException {
    public DataFetchException(String message) {
        super(message);
    }
    public DataFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}
