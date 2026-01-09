package com.ispw.tryeshiftsv2.bean;

//classe che funziona da cassetto per il passaggio dei dati tra le view, singleton

public class SessionContext {
    private static SessionContext instance;
    private UserBean loggeduser;
    private WorkplaceBean loggedWorkplace;

    private SessionContext(){}

    public static SessionContext getInstance(){
        if(instance == null) instance = new SessionContext();
        return instance;
    }

    public UserBean getLoggeduser() {return loggeduser;}
    public void setLoggeduser(UserBean loggeduser) {this.loggeduser = loggeduser;}
    public WorkplaceBean getLoggedWorkplace() {return loggedWorkplace;}
    public void setLoggedWorkplace(WorkplaceBean loggedWorkplace) {this.loggedWorkplace = loggedWorkplace;}
}
