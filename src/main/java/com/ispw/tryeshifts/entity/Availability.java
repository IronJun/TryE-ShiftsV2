package com.ispw.tryeshifts.entity;

public class Availability {
    private final String userEmail;
    private String workplaceName;
    private final String day;
    private final String startShift;
    private final String endShift;
    private final String weekId;

    public Availability(String userEmail, String workplaceName, String day, String startShift, String endShift,String weekId) {
        this.userEmail = userEmail;
        this.workplaceName = workplaceName;
        this.day = day;
        this.startShift = startShift;
        this.endShift = endShift;
        this.weekId = weekId;
    }
    public String getWeekId() { return weekId; }
    public String getUserEmail() {
        return userEmail;
    }
    public String getWorkplaceName() {
        return workplaceName;
    }
    public String getDay() {
        return day;
    }
    public String getEndShift() {
        return endShift;
    }
    public String getStartShift() {
        return startShift;
    }
    public String getFullShift() {
        return startShift + "-" + endShift;
    }
    public void setWorkplaceName(String workplaceName) {this.workplaceName = workplaceName;}
}

