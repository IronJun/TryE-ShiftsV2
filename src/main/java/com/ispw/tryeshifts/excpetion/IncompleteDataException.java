package com.ispw.tryeshifts.excpetion;

public class IncompleteDataException extends ValidationException {
    public IncompleteDataException(String fieldName) {
        super("il campo " + fieldName + " è obbligatorio.",fieldName);
    }
}
