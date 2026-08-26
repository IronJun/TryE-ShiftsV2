package com.ispw.tryeshifts.appcontroller;

import com.ispw.tryeshifts.config.AppConfig;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.dao.*;
import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.exception.*;
import com.ispw.tryeshifts.utils.SecurityUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SignupAC {
    private final Logger logger = Logger.getLogger(SignupAC.class.getName());


    public void registerUser(UserBean userbean) throws BaseException {

        UserDAO userRepo = AppConfig.getInstance().getUserRepository();

        if (isDataInvalid(userbean)) {
            throw new ValidationException("All fields must be completed","Form");
        }
        if (pwdNotMatch(userbean.getPassword(), userbean.getPwdRep())) {
            throw new ValidationException("Password doesn't match","PwdRep");
        }
        if(userbean.getPassword().length() < 6){
            throw new ValidationException("Password too short","Password");
        }
        if(!userbean.getEmail().contains("@") || !userbean.getEmail().contains(".")){
            throw new ValidationException("Email address is invalid","Email");
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
            logger.log(Level.SEVERE, "Errore di persistenza durante la registrazione\n", e);
        }



    }

    private boolean isDataInvalid(UserBean bean) {
        return bean.getEmail().isEmpty() || bean.getName().isEmpty() ||
                bean.getPassword().isEmpty() || bean.getSurname().isEmpty();
    }

    private boolean pwdNotMatch(String pwd, String pwd2) {
        return !pwd.equals(pwd2);
    }

}

