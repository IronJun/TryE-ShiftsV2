package com.ispw.tryeshifts.appController;

import com.ispw.tryeshifts.AppConfig;
import com.ispw.tryeshifts.bean.SessionContext;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.dao.SecurityUtils;
import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.excpetion.DAOException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;

public class SettingsAC {
    public void updateUserProfile(UserBean user) throws DAOException, EntityNotFoundException {
        var repo = AppConfig.getRepository();
        UserInfo existingUser = repo.findByEmail(user.getEmail());
        if(existingUser == null){throw new EntityNotFoundException("User not found");}
        existingUser.setName(user.getName());
        existingUser.setSurname(user.getSurname());

        if(user.getPassword() != null && !user.getPassword().isEmpty()){
            String hashedPass = SecurityUtils.hashPassword(user.getPassword());
            existingUser.setPasswordHash(hashedPass);
        }

        repo.updateUser(existingUser);

        SessionContext.getInstance().setLoggeduser(user);
    }
}
