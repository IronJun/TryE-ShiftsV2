package com.ispw.tryeshifts.bean;

public class AvailabilityBean {
    private String userEmail;
    private String workplaceName;
    private String day;
    private String shift;
    private String startShift;
    private String endShifts;

    public AvailabilityBean(String userEmail, String workplaceName, String day,String startShift,String endShifts) {
        this.userEmail = userEmail;
        this.workplaceName = workplaceName;
        this.day = day;
        this.startShift = startShift;
        this.endShifts = endShifts;
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
    public String getStartShift() {return startShift;}
    public String getEndShifts() {return endShifts;}
}
