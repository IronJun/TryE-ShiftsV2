package com.ispw.tryeshifts.entity;

public class Availability {
    private final String userEmail;
    private String workplaceName;
    private final String day;
    private final String startShift;
    private final String endShift;

    public Availability(String userEmail, String workplaceName, String day, String startShift, String endShift) {
        this.userEmail = userEmail;
        this.workplaceName = workplaceName;
        this.day = day;
        this.startShift = startShift;
        this.endShift = endShift;
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
    public String getFullShift() {
        return startShift + "-" + endShift;
    }
    public void setWorkplaceName(String workplaceName) {this.workplaceName = workplaceName;}
}

