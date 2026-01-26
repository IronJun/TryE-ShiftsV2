package com.ispw.tryeshifts.excpetion;

public class ValidationException extends BaseException {
    private final String fieldName;

    public ValidationException(String message,String fieldName) {
        super(message);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
