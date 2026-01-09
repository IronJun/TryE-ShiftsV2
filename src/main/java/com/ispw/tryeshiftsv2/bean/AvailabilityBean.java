package com.ispw.tryeshiftsv2.bean;

public class AvailabilityBean {
    private String userEmail;
    private String workplaceName;
    private String day;
    private String shift;
    private String StartShift;
    private String EndShift;

    public AvailabilityBean(String userEmail, String workplaceName, String day,String StartShift,String EndShift) {
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
    public String getStartShift() {return StartShift;}
    public String getEndShift() {return EndShift;}
}
