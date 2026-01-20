package com.ispw.tryeshifts.bean;

public class AvailabilityBean {
    private String userEmail;
    private String workplaceName;
    private String day;
    private String startShift;
    private String endShifts;
    private String weekId;

    public AvailabilityBean(String userEmail, String workplaceName, String day,String startShift,String endShifts,String weekId) {
        this.userEmail = userEmail;
        this.workplaceName = workplaceName;
        this.day = day;
        this.startShift = startShift;
        this.endShifts = endShifts;
        this.weekId = weekId;
    }

    public String getWeekId() {
        return weekId;
    }
    public void setWeekId(String weekId) {
        this.weekId = weekId;
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
    public String getStartShift() {return startShift;}
    public String getEndShifts() {return endShifts;}
}
