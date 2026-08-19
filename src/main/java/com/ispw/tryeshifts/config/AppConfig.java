package com.ispw.tryeshifts.config;

import com.ispw.tryeshifts.dao.*;
import com.ispw.tryeshifts.dao.decorator.UserDAOCsvDecorator;
import com.ispw.tryeshifts.factory.DAOFactory;
import com.ispw.tryeshifts.factory.DemoDAOFactory;
import com.ispw.tryeshifts.factory.JdbcDAOFactory;


public class AppConfig {
    private static final boolean IS_DEMO_MODE = true;
    private static final boolean SAVE_USER_TO_CSV = false;

    private static DAOFactory daoFactory;
    private static UserDAO userDAO;
    private static MembershipDAO membershipDAO;
    private static WorkplaceDAO workplaceDAO;
    private static AvailabilityDAO availabilityDAO;
    private static NotificationDAO notificationDAO;

    private AppConfig() {
        throw new IllegalStateException("Utility class");
    }

    private static DAOFactory getDAOFactory() {
        if(daoFactory == null) {
            if(IS_DEMO_MODE){
                daoFactory = new DemoDAOFactory();
            }else{
                daoFactory = new JdbcDAOFactory();
            }
        }
        return daoFactory;
    }

    public static UserDAO getUserRepository() {
        if(userDAO == null) {
            userDAO = getDAOFactory().getUserDAO();
            if(SAVE_USER_TO_CSV){
                userDAO = new UserDAOCsvDecorator(userDAO);
            }
        }
        return userDAO;
    }

    public static MembershipDAO getMembershipRepository() {
        if(membershipDAO == null) {
            membershipDAO = getDAOFactory().getMembershipDAO();
        }
        return membershipDAO;
    }

    public static WorkplaceDAO getWorkplaceRepository() {
        if(workplaceDAO == null) {
            workplaceDAO = getDAOFactory().getWorkplaceDAO();
        }
        return workplaceDAO;
    }

    public static AvailabilityDAO getAvailabilityRepository() {
        if(availabilityDAO == null) {
            availabilityDAO = getDAOFactory().getAvailabilityDAO();
        }
        return availabilityDAO;
    }

    public static NotificationDAO getNotificationRepository() {
        if(notificationDAO == null){
            notificationDAO = getDAOFactory().getNotificationDAO();
        }
        return notificationDAO;
    }
}