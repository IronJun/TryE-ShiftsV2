package com.ispw.tryeshifts.excpetion;

public class IncompleteDataException extends ValidationException {
    public IncompleteDataException(String fieldName) {
        super("Inserimenti incompleti: " + fieldName ,fieldName);
    }
}
