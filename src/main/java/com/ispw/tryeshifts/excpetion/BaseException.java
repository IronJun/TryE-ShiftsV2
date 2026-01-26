package com.ispw.tryeshifts.excpetion;

import java.time.LocalDateTime;

public class BaseException extends Exception {
    private final LocalDateTime timestamp;

    public BaseException(String message) {
        super(message);
        this.timestamp = LocalDateTime.now();
    }

    public BaseException(String message, Throwable cause) {
        super(message, cause);
        this.timestamp = LocalDateTime.now();
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
