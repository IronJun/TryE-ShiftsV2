package com.ispw.tryeshifts.config;

import com.ispw.tryeshifts.dao.*;
import com.ispw.tryeshifts.dao.decorator.decorations.UserDAOCsvDecorator;
import com.ispw.tryeshifts.factory.DAOFactory;


public class AppConfig {
    private boolean isDemo = true;
    private boolean saveCSV = true;

    private DAOFactory daoFactory;
    private UserDAO userDAO;
    private MembershipDAO membershipDAO;
    private WorkplaceDAO workplaceDAO;
    private AvailabilityDAO availabilityDAO;
    private NotificationDAO notificationDAO;

    private static class LazyContainer{
        public static final AppConfig instance = new AppConfig();
    }

    public static AppConfig getInstance() {
        return  LazyContainer.instance;
    }


    private AppConfig() {
        intiDAOS();
    }

    private void intiDAOS(){
        this.daoFactory = DAOFactory.getFactory(isDemo);

        UserDAO tempUserDAO = daoFactory.getUserDAO();
        if(saveCSV){
            tempUserDAO = new UserDAOCsvDecorator(tempUserDAO);
        }
        this.userDAO = tempUserDAO;
        this.membershipDAO = daoFactory.getMembershipDAO();
        this.workplaceDAO = daoFactory.getWorkplaceDAO();
        this.availabilityDAO = daoFactory.getAvailabilityDAO();
        this.notificationDAO = daoFactory.getNotificationDAO();
    }


    public UserDAO getUserRepository() {
        return  userDAO;
    }

    public MembershipDAO getMembershipRepository() {
        return membershipDAO;
    }

    public WorkplaceDAO getWorkplaceRepository() {
        return workplaceDAO;
    }

    public AvailabilityDAO getAvailabilityRepository() {
        return availabilityDAO;
    }

    public NotificationDAO getNotificationRepository() {
        return notificationDAO;
    }

    //Metodo per il setup dei test
    public void setTestMode(boolean demoMode, boolean csvMode){
        isDemo = demoMode;
        saveCSV = csvMode;
        intiDAOS();
    }
}