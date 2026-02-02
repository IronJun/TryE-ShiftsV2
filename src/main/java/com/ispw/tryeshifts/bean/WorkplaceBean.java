package com.ispw.tryeshifts.bean;

import java.util.List;

public class WorkplaceBean {
    private String workplaceName;
    private String ownerEmail;
    private String address;
    private List<String> selectedDaysBean;
    private List<String> shiftsBean;

    public WorkplaceBean(){}

    public WorkplaceBean(String workplacename, String address,List<String> selectedDays,List<String> shifts,String ownerEmail){
        this.workplaceName = workplacename;
        this.address = address;
        this.selectedDaysBean = selectedDays;
        this.shiftsBean = shifts;
        this.ownerEmail = ownerEmail;
    }

    public List<String> getSelectedDays() {return selectedDaysBean;}
    public List<String> getShiftsBean() {return shiftsBean;}
    public void setSelectedDays(List<String> selectedDays) {this.selectedDaysBean = selectedDays;}
    public void setShiftsBean(List<String> shifts) {this.shiftsBean = shifts;}
    public String getWorkplaceName(){
        return this.workplaceName;
    }
    public String getOwnerEmail(){return this.ownerEmail;}
    public void setOwnerEmail(String email){this.ownerEmail = email;}
    public void setWorkplaceName(String name){this.workplaceName = name;}
    public String getAddress(){return this.address;}
    public void setAddress(String address){this.address = address;}
}
