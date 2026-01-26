package com.ispw.tryeshifts.excpetion;

public class EntityNotFoundException extends EntityException {
    public EntityNotFoundException(String entityName, String id) {
        super(entityName + " con identificativo: "+id +" non trovato.", entityName);
    }
}
