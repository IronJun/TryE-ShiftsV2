package com.ispw.tryeshifts.config;

import com.ispw.tryeshifts.dao.*;
import com.ispw.tryeshifts.dao.decorator.UserDAOCsvDecorator;
import com.ispw.tryeshifts.factory.DAOFactory;


public class AppConfig {
    private boolean IS_DEMO_MODE = true;
    private boolean SAVE_USER_TO_CSV = false;

    private DAOFactory daoFactory;
    private UserDAO userDAO;
    private MembershipDAO membershipDAO;
    private WorkplaceDAO workplaceDAO;
    private AvailabilityDAO availabilityDAO;
    private NotificationDAO notificationDAO;


    private AppConfig() {

    }

    private static class LazyContainer{
        public static final AppConfig instance = new AppConfig();
    }

    public static AppConfig getInstance() {
        return  LazyContainer.instance;
    }

    private DAOFactory getDAOFactory() {
        if(daoFactory == null) {
            daoFactory = DAOFactory.getFactory(IS_DEMO_MODE);
        }
        return daoFactory;
    }

    public UserDAO getUserRepository() {
        if(userDAO == null) {
            userDAO = getDAOFactory().getUserDAO();
            if(SAVE_USER_TO_CSV){
                userDAO = new UserDAOCsvDecorator(userDAO);
            }
        }
        return userDAO;
    }

    public MembershipDAO getMembershipRepository() {
        if(membershipDAO == null) {
            membershipDAO = getDAOFactory().getMembershipDAO();
        }
        return membershipDAO;
    }

    public WorkplaceDAO getWorkplaceRepository() {
        if(workplaceDAO == null) {
            workplaceDAO = getDAOFactory().getWorkplaceDAO();
        }
        return workplaceDAO;
    }

    public AvailabilityDAO getAvailabilityRepository() {
        if(availabilityDAO == null) {
            availabilityDAO = getDAOFactory().getAvailabilityDAO();
        }
        return availabilityDAO;
    }

    public NotificationDAO getNotificationRepository() {
        if(notificationDAO == null){
            notificationDAO = getDAOFactory().getNotificationDAO();
        }
        return notificationDAO;
    }

    //Metodo per il setup dei test
    public void setTestMode(boolean demoMode, boolean csvMode){
        IS_DEMO_MODE = demoMode;
        SAVE_USER_TO_CSV = csvMode;

        daoFactory = null;
        userDAO = null;
        membershipDAO = null;
        workplaceDAO = null;
        availabilityDAO = null;
        notificationDAO = null;
    }
}