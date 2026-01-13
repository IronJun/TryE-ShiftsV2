package com.ispw.tryeshifts;

import com.ispw.tryeshifts.dao.InMemory;
import com.ispw.tryeshifts.dao.JDBC;
import com.ispw.tryeshifts.dao.Repository;

import java.util.logging.Logger;

public class AppConfig {
    private static Repository repository = null;
    public static final boolean IS_DEMO_MODE = true;
    private static final Logger LOGGER = Logger.getLogger(AppConfig.class.getName());


    private AppConfig(){
        throw new IllegalStateException("Utility class");
    }

    public static Repository getRepository(){
        if(repository == null){
            if(IS_DEMO_MODE){
                repository = new InMemory();
                LOGGER.info("Sistema: craeto un nuovo repository in memoria");
            }else{
                repository = new JDBC();
                LOGGER.info("Sistema in Modalità persistenza");
            }
        }
        return repository;
    }
}
