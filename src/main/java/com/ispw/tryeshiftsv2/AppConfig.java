package com.ispw.tryeshiftsv2;

import com.ispw.tryeshiftsv2.dao.InMemory;
import com.ispw.tryeshiftsv2.dao.Repository;

public class AppConfig {
    private static Repository repository = null;
    public static final boolean IS_DEMO_MODE = true;

    //public AppConfig(){}

    public static Repository getRepository(){
        if(repository == null){
            if(IS_DEMO_MODE){
            repository = new InMemory();
            System.out.println("SISTEMA: Creato nuovo repository InMemory");
            }/*else{
                repository =  //futuro db
            }*/
        }
        return repository;
    }
}
