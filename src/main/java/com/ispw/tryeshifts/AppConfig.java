package com.ispw.tryeshifts;

import com.ispw.tryeshifts.dao.*;

import java.util.logging.Logger;

public class AppConfig {
    private static UserDAO UserRepo = null;
    private static WorkplaceDAO workplaceRepo = null;
    private static MembershipDAO membershipRepo = null;
    private static AvailabilityDAO availabilityRepo = null;
    public static final boolean IS_DEMO_MODE = false;
    private static final Logger LOGGER = Logger.getLogger(AppConfig.class.getName());


    private AppConfig(){
        throw new IllegalStateException("Utility class");
    }

    public static UserDAO getUserRepository(){
        if(UserRepo  == null){
            if(IS_DEMO_MODE){
                UserRepo = new UserDAODemo();
                LOGGER.info("Sistema: craeto un nuovo repository in memoria");
            }else{
                UserRepo = new UserDAOJdbc();
                LOGGER.info("Sistema in Modalità persistenza");
            }
        }
        return UserRepo;
    }
    public static WorkplaceDAO getWorkplaceRepository(){
        if(workplaceRepo == null){
            if(IS_DEMO_MODE){
                workplaceRepo = new WorkplaceDAODemo();
                LOGGER.info("Sistema: craeto un nuovo repository in memoria");
            }else{
                workplaceRepo = new WorkplaceDAOJdbc();
                LOGGER.info("Sistema in Modalità persistenza");
            }
        }
        return workplaceRepo;
    }

    public static AvailabilityDAO getAvailabilityRepository(){
        if(availabilityRepo == null){
            if(IS_DEMO_MODE){
                availabilityRepo = new AvailabilityDAODemo();
                LOGGER.info("Sistema: craeto un nuovo repository in memoria");
            }else{
                availabilityRepo = new AvailabilityDAOJdbc();
                LOGGER.info("Sistema in Modalità persistenza");
            }
        }
        return availabilityRepo;
    }

    public static MembershipDAO getMembershipRepository(){
        if(membershipRepo == null){
            if(IS_DEMO_MODE){
                membershipRepo = new MembershipDAODemo();
                LOGGER.info("Sistema: craeto un nuovo repository in memoria");
            }else{
                membershipRepo = new MembershipDAOJdbc();
                LOGGER.info("Sistema in Modalità persistenza");
            }
        }
        return membershipRepo;
    }
}
