package com.ispw.tryeshifts.appcontroller;

import com.ispw.tryeshifts.config.AppConfig;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.dao.UserDAO;
import com.ispw.tryeshifts.utils.SecurityUtils;
import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.exception.*;


public class LoginAC {
    private final UserDAO userRepo;

    public LoginAC(UserDAO userRepo) {
        this.userRepo = userRepo;
    }
    public LoginAC() {
        this(AppConfig.getInstance().getUserRepository());
    }
    public  UserBean loginUser(UserBean userBean) throws BaseException{
        UserInfo savedUser;
        savedUser=userRepo.findByEmail(userBean.getEmail());
        if(savedUser == null){throw new InvalidCredentialException("Email non registrata. Riprova.");}
        String hashedInputPassword = SecurityUtils.hashPassword(userBean.getPassword());
        if (!savedUser.getPasswordHash().equals(hashedInputPassword)) {
            throw new InvalidCredentialException("Password non corretta. Riprova.");
        }


        // 3. Login riuscito: popoliamo il bean con i dati reali del DB e lo restituiamo
        userBean.setName(savedUser.getName());
        userBean.setSurname(savedUser.getSurname());

        return userBean;
    }
    public  UserBean autoLogin(String email)throws BaseException{
        UserInfo user = userRepo.findByEmail(email);
        if(user == null){throw new EntityNotFoundException("User",email);}
        UserBean loggedUser = new UserBean();
        loggedUser.setEmail(user.getEmail());
        loggedUser.setName(user.getName());
        loggedUser.setSurname(user.getSurname());
        return loggedUser;
    }

}
