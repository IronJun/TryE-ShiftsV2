package com.ispw.tryeshifts.config;

import com.ispw.tryeshifts.dao.*;
import com.ispw.tryeshifts.dao.csv.UserDAOCsv;
import com.ispw.tryeshifts.dao.demo.AvailabilityDAODemo;
import com.ispw.tryeshifts.dao.demo.MembershipDAODemo;
import com.ispw.tryeshifts.dao.demo.UserDAODemo;
import com.ispw.tryeshifts.dao.demo.WorkplaceDAODemo;
import com.ispw.tryeshifts.dao.jdbc.AvailabilityDAOJdbc;
import com.ispw.tryeshifts.dao.jdbc.MembershipDAOJdbc;
import com.ispw.tryeshifts.dao.jdbc.UserDAOJdbc;
import com.ispw.tryeshifts.dao.jdbc.WorkplaceDAOJdbc;
import com.ispw.tryeshifts.factory.DAOFactory;
import com.ispw.tryeshifts.factory.DemoDAOFactory;
import com.ispw.tryeshifts.factory.JdbcDAOFactory;

/*
public class AppConfig {
    private static UserDAO userRepo = null;
    private static WorkplaceDAO workplaceRepo = null;
    private static MembershipDAO membershipRepo = null;
    private static AvailabilityDAO availabilityRepo = null;
    public static final boolean IS_DEMO_MODE = true;
    public static final boolean SAVE_USER_TO_CSV = false;

    private AppConfig(){
        throw new IllegalStateException("Utility class");
    }

    public static UserDAO getUserRepository(){
        if(userRepo  == null){
            if(IS_DEMO_MODE){
                if(SAVE_USER_TO_CSV){
                    userRepo = new UserDAOCsv();
                }else {
                    userRepo = new UserDAODemo();
                }
            }else{
                userRepo = new UserDAOJdbc();
            }
        }
        return userRepo;
    }
    public static WorkplaceDAO getWorkplaceRepository(){
        if(workplaceRepo == null){
            if(IS_DEMO_MODE){
                workplaceRepo = new WorkplaceDAODemo();
            }else{
                workplaceRepo = new WorkplaceDAOJdbc();
            }
        }
        return workplaceRepo;
    }
    public static AvailabilityDAO getAvailabilityRepository(){
        if(availabilityRepo == null){
            if(IS_DEMO_MODE){
                availabilityRepo = new AvailabilityDAODemo();
            }else{
                availabilityRepo = new AvailabilityDAOJdbc();
            }
        }
        return availabilityRepo;
    }
    public static MembershipDAO getMembershipRepository(){
        if(membershipRepo == null){
            if(IS_DEMO_MODE){
                membershipRepo = new MembershipDAODemo();
            }else{
                membershipRepo = new MembershipDAOJdbc();
            }
        }
        return membershipRepo;
    }
}
*/

public class AppConfig {
    private static final boolean IS_DEMO_MODE = false;
    private static DAOFactory daoFactory;

    private AppConfig() {
        throw new IllegalStateException("Utility class");
    }

    public static DAOFactory getDAOFactory() {
        if(daoFactory == null) {
            if(IS_DEMO_MODE){
                daoFactory = new DemoDAOFactory();
            }else{
                daoFactory = new JdbcDAOFactory();
            }
        }
        return daoFactory;
    }
}