package com.ispw.tryeshifts.appcontroller;

import com.ispw.tryeshifts.AppConfig;
import com.ispw.tryeshifts.bean.SessionContext;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.dao.*;
import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.excpetion.BaseException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;
import com.ispw.tryeshifts.excpetion.DataFetchException;

import java.util.logging.Logger;

public class SettingsAC {
    private static final Logger LOGGER = Logger.getLogger(SettingsAC.class.getName());
    private static final UserDAO userRepo = AppConfig.getUserRepository();

    public void updateUserProfile(UserBean user) throws BaseException {
        UserInfo existingUser = userRepo.findByEmail(user.getEmail());
        if(existingUser == null){throw new EntityNotFoundException("User",user.getEmail());}
        existingUser.setName(user.getName());
        existingUser.setSurname(user.getSurname());

        if(user.getPassword() != null && !user.getPassword().isEmpty()){
            try {
                String hashedPass = SecurityUtils.hashPassword(user.getPassword());
                existingUser.setPasswordHash(hashedPass);
            }catch (DataFetchException _){
                LOGGER.info("errore nell'hashing di password0");
            }
        }

        userRepo.updateUser(existingUser);

        SessionContext.getInstance().setLoggeduser(user);
    }
}
