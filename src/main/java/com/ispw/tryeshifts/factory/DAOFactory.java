package com.ispw.tryeshifts.factory;

import com.ispw.tryeshifts.dao.*;

public abstract class DAOFactory {
    public static final String DEMO_MODE = "DEMO";
    public static final String JDBC_MODE = "JDBC";

    public static DAOFactory getFactory(boolean isDemo){
        if(isDemo){
            return new DemoDAOFactory();
        }
        return new JdbcDAOFactory();
    }
    public abstract UserDAO getUserDAO();
    public abstract WorkplaceDAO getWorkplaceDAO();
    public abstract AvailabilityDAO getAvailabilityDAO();
    public abstract MembershipDAO getMembershipDAO();
    public abstract NotificationDAO getNotificationDAO();
}
