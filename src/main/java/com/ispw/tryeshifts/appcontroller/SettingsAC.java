package com.ispw.tryeshifts.appcontroller;

import com.ispw.tryeshifts.AppConfig;
import com.ispw.tryeshifts.bean.SessionContext;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.dao.SecurityUtils;
import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.excpetion.DAOException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;
import com.ispw.tryeshifts.excpetion.FetchDataException;

import java.util.logging.Logger;

public class SettingsAC {
    private static final Logger LOGGER = Logger.getLogger(SettingsAC.class.getName());

    public void updateUserProfile(UserBean user) throws DAOException, EntityNotFoundException {
        var repo = AppConfig.getRepository();
        UserInfo existingUser = repo.findByEmail(user.getEmail());
        if(existingUser == null){throw new EntityNotFoundException("User not found");}
        existingUser.setName(user.getName());
        existingUser.setSurname(user.getSurname());

        if(user.getPassword() != null && !user.getPassword().isEmpty()){
            try {
                String hashedPass = SecurityUtils.hashPassword(user.getPassword());
                existingUser.setPasswordHash(hashedPass);
            }catch (FetchDataException _){
                LOGGER.info("errore nell'hashing di password0");
            }
        }

        repo.updateUser(existingUser);

        SessionContext.getInstance().setLoggeduser(user);
    }
}
