package com.ispw.tryeshifts.exception;

public class EntityException extends BaseException {
    private final String entityName;

    public EntityException(String message, String entityName) {
        super(message);
        this.entityName = entityName;
    }

    public EntityException(String message,String entityName, Throwable cause) {
        super(message, cause);
        this.entityName = entityName;
    }

    public String getEntityName() {
        return entityName;
    }
}
