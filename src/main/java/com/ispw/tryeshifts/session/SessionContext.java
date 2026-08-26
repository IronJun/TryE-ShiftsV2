package com.ispw.tryeshifts.session;


import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.bean.WorkplaceBean;


public class SessionContext {
    private UserBean loggeduser;
    private WorkplaceBean loggedWorkplace;

    private SessionContext(){}

    private static class LazyContainer {
        private static final SessionContext instance = new SessionContext();
    }
    public static SessionContext getInstance(){
        return  LazyContainer.instance;
    }

    public UserBean getLoggeduser() {return loggeduser;}
    public void setLoggeduser(UserBean loggeduser) {this.loggeduser = loggeduser;}

    public WorkplaceBean getLoggedWorkplace() {return loggedWorkplace;}
    public void setLoggedWorkplace(WorkplaceBean loggedWorkplace) {this.loggedWorkplace = loggedWorkplace;}
}
