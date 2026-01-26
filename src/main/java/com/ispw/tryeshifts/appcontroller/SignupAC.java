package com.ispw.tryeshifts.appcontroller;

import com.ispw.tryeshifts.AppConfig;
import com.ispw.tryeshifts.SceneManager;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.dao.*;
import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.excpetion.*;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SignupAC {
    private static final Logger LOGGER = Logger.getLogger(SignupAC.class.getName());

    private SignupAC(){
        throw new IllegalStateException("Utility class");
    }

    public static void registerUser(UserBean userbean) throws BaseException {

        UserDAO userRepo = AppConfig.getUserRepository();

        if (isDataInvalid(userbean)) {
            throw new IncompleteDataException("Tutti i campi sono obbligatori");
        }
        if (pwdNotMatch(userbean.getPassword(), userbean.getPwdRep())) {
            throw new ValidationException("Le password non corrispondono","PwdRep");
        }


        if(userRepo.findByEmail(userbean.getEmail())!= null){
            throw new DuplicateEntityException("User",userbean.getEmail());
        }

        UserInfo userentity = new UserInfo(userbean.getEmail(), userbean.getName(), userbean.getSurname());

        try {
            String hashedPass = SecurityUtils.hashPassword(userbean.getPassword());
            userentity.setPasswordHash(hashedPass);
            userRepo.save(userentity);
        }catch(DataFetchException e){
            LOGGER.log(Level.SEVERE, "Errore di persistenza durante la registrazione", e);
            throw e;
        }



    }

    private static boolean isDataInvalid(UserBean bean) {
        return bean.getEmail().isEmpty() || bean.getName().isEmpty() ||
                bean.getPassword().isEmpty() || bean.getSurname().isEmpty();
    }

    private static boolean pwdNotMatch(String pwd, String pwd2) {
        return !pwd.equals(pwd2);
    }

}

