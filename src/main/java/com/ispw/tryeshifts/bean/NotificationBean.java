package com.ispw.tryeshifts.bean;

public class NotificationBean {
    private String destUser;
    private String message;
    private String type;
    private boolean isRead;
    private String timestamp;

    public NotificationBean(String message, String type, boolean isRead, String timestamp) {
        this.message = message;
        this.type = type;
        this.isRead = isRead;
        this.timestamp = timestamp;
    }
    public NotificationBean(String message, String type) {
        this.message = message;
        this.type = type;
    }
    public NotificationBean(String email,String message, String type) {
        this.destUser = email;
        this.message = message;
        this.type = type;
    }

    public String getDestUser() {
        return destUser;
    }
    public String getMessage() {return message;}
    public String getType() {return type;}
    public boolean isRead() {return isRead;}
    public String getTimestamp() {return timestamp;}
}
