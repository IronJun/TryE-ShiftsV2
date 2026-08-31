package com.ispw.tryeshifts.exception;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class BaseException extends Exception {
    private final LocalDateTime timestamp;

    public BaseException(String message) {
        super(message);
        this.timestamp = LocalDateTime.now(ZoneId.systemDefault());
    }

    public BaseException(String message, Throwable cause) {
        super(message, cause);
        this.timestamp = LocalDateTime.now(ZoneId.systemDefault());
    }

}
