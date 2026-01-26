package com.ispw.tryeshifts.appcontroller;

import com.ispw.tryeshifts.AppConfig;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.dao.UserDAO;
import com.ispw.tryeshifts.dao.SecurityUtils;
import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.excpetion.*;

import java.util.logging.Logger;

public class LoginAC {
    private static final Logger LOGGER = Logger.getLogger(LoginAC.class.getName());

    private LoginAC(){
        throw new IllegalStateException("Utility class");
    }
    public static UserBean loginUser(UserBean userBean) throws BaseException{
        UserDAO userRepo = AppConfig.getUserRepository();
        UserInfo savedUser;
        savedUser=userRepo.findByEmail(userBean.getEmail());
        String hashedInputPassword = SecurityUtils.hashPassword(userBean.getPassword());
        if (!savedUser.getPasswordHash().equals(hashedInputPassword)) {
            throw new InvalidCredentialException("Password non corretta. Riprova.");
        }


        // 3. Login riuscito: popoliamo il bean con i dati reali del DB e lo restituiamo
        userBean.setName(savedUser.getName());
        userBean.setSurname(savedUser.getSurname());

        return userBean;
    }

}
