package com.ispw.tryeshifts.entity;

public class Availability {
    private String userEmail;
    private String workplaceName;
    private String day;
    private String shift;
    private String StartShift;
    private String EndShift;

    public Availability(String userEmail, String workplaceName, String day, String StartShift, String EndShift) {
        this.userEmail = userEmail;
        this.workplaceName = workplaceName;
        this.day = day;
        this.StartShift = StartShift;
        this.EndShift = EndShift;
    }
    public String getUserEmail() {
        return userEmail;
    }
    public String getWorkplaceName() {
        return workplaceName;
    }
    public String getDay() {
        return day;
    }
    public String getShift() {
        return shift;
    }
    public String getFullShift() {
        return StartShift + "-" + EndShift;
    }
    public void setWorkplaceName(String workplaceName) {this.workplaceName = workplaceName;}
}

