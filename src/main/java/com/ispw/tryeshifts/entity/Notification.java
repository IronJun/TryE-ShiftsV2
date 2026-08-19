package com.ispw.tryeshifts.entity;

public class Notification {
    private String destUser;
    private String message;
    private String Type;
    private boolean isRead;
    private String timestamp;

    public Notification(String destUser, String message, String type, boolean isRead, String timestamp) {
        this.destUser = destUser;
        this.message = message;
        this.Type = type;
        this.isRead = isRead;
        this.timestamp = timestamp;
    }

    public String getDestUser() {
        return destUser;
    }
    public void setDestUser(String destUser) {
        this.destUser = destUser;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        this.Type = type;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        this.isRead = read;
    }

    public String getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
