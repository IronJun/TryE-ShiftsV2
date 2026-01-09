package com.ispw.tryeshiftsv2.appController;

import com.ispw.tryeshiftsv2.AppConfig;
import com.ispw.tryeshiftsv2.bean.UserBean;
import com.ispw.tryeshiftsv2.dao.Repository;
import com.ispw.tryeshiftsv2.dao.SecurityUtils;
import com.ispw.tryeshiftsv2.entity.UserInfo;
import com.ispw.tryeshiftsv2.excpetion.*;

public class LoginAC {
    public static UserBean loginUser(UserBean userBean) throws UserNotFoundException, InvalidCredentialException, DAOException {

        Repository repository = AppConfig.getRepository();

        UserInfo savedUser;
        try{
            savedUser=repository.findByEmail(userBean.getEmail());
        }catch(EntityNotFoundException e){
            throw new UserNotFoundException("Errore di recupero utente");
        }


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
