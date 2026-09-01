package com.ispw.tryeshifts.appcontroller;

import com.ispw.tryeshifts.config.AppConfig;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.dao.*;
import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.exception.*;
import com.ispw.tryeshifts.utils.SecurityUtils;

import java.util.Locale;


public class SignupAC {


    public void registerUser(UserBean userbean) throws BaseException {

        UserDAO userRepo = AppConfig.getInstance().getUserRepository();

        if (isDataInvalid(userbean)) {
            throw new ValidationException("All fields must be completed","Form");
        }
        String normalizedEmail = userbean.getEmail().trim().toLowerCase(Locale.ROOT);
        userbean.setEmail(normalizedEmail);
        if (pwdNotMatch(userbean.getPassword(), userbean.getPwdRep())) {
            throw new ValidationException("Password doesn't match","PwdRep");
        }
        if(userbean.getPassword().length() < 6){
            throw new ValidationException("Password too short","Password");
        }
        if(!userbean.getEmail().contains("@") || !userbean.getEmail().contains(".")){
            throw new ValidationException("Email address is invalid","Email");
        }


        if(userRepo.findByEmail(normalizedEmail)!= null){
            throw new DuplicateEntityException("User",normalizedEmail);
        }

        UserInfo userEntity = new UserInfo(normalizedEmail, userbean.getName(), userbean.getSurname());

        try {
            String hashedPass = SecurityUtils.hashPassword(userbean.getPassword());
            userEntity.setPasswordHash(hashedPass);
            userRepo.save(userEntity);
        }catch(DataFetchException e){
            throw new DataFetchException("Persistency error during registration: ",e);
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

