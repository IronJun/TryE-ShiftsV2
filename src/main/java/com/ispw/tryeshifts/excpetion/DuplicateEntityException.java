package com.ispw.tryeshifts.excpetion;

public class DuplicateEntityException extends EntityException {
    public DuplicateEntityException(String entityName, String value) {
        super(entityName + " con valore " + value + " già esistente.", entityName );
    }
}
