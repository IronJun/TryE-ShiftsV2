package com.ispw.tryeshifts.entity;

import java.util.List;

public class Workplace {
    private String id;
    private String name;
    private String ownerEmail;
    private String address;
    private List<String> shifts;
    private List<String> selectedDays;
    private List<UserInfo> users;

    public Workplace(){}

    public Workplace(String name,String address,List<String> selectedDays,List<String> shifts,String ownerEmail){
        this.name = name;
        this.address = address;
        this.selectedDays = selectedDays;
        this.shifts = shifts;
        this.ownerEmail = ownerEmail;
    }
    public String getOwnerEmail(){return this.ownerEmail;}
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {this.name = name;}
    public String getAddress() {return address;}
    public void setAddress(String address) {this.address = address;}
    public List<String> getShifts() {return shifts;}
    public void setShifts(List<String> shifts) {this.shifts = shifts;}
    public List<String> getSelectedDays() {return selectedDays;}
    public void setSelectedDays(List<String> selectedDays) {this.selectedDays = selectedDays;}
    public List<UserInfo> getUsers() {return users;}
    public void setUsers(List<UserInfo> users) {this.users = users;}
}
