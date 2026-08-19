package com.ispw.tryeshifts.bean;

public class NotificationBean {
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

    public String getMessage() {return message;}
    public String getType() {return type;}
    public boolean isRead() {return isRead;}
    public String getTimestamp() {return timestamp;}
    public void setRead(boolean isRead) {this.isRead = isRead;}
}
