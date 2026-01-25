package com.ispw.tryeshifts.entity;

public class Membership {
    private UserInfo user;
    private Workplace workplace;
    private String role;
    private boolean isAccepted;

    public Membership(UserInfo user,Workplace workplace,String role,boolean isAccepted){
        this.user = user;
        this.workplace = workplace;
        this.role = role;
        this.isAccepted = isAccepted;
    }

    public void setAccepted(boolean isAccepted){this.isAccepted = isAccepted;}
    public String getRole(){return this.role;}
    public Workplace getWorkplace(){return workplace;}
    public UserInfo getUser(){return user;}
    public boolean isAccepted() {
        return isAccepted;
    }
}
