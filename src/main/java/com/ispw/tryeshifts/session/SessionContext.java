package com.ispw.tryeshifts.session;


import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.bean.WorkplaceBean;


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
