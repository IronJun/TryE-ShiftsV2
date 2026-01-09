package com.ispw.tryeshiftsv2.appController;

import com.ispw.tryeshiftsv2.AppConfig;
import com.ispw.tryeshiftsv2.bean.SessionContext;
import com.ispw.tryeshiftsv2.bean.UserBean;
import com.ispw.tryeshiftsv2.dao.SecurityUtils;
import com.ispw.tryeshiftsv2.entity.UserInfo;
import com.ispw.tryeshiftsv2.excpetion.DAOException;
import com.ispw.tryeshiftsv2.excpetion.EntityNotFoundException;

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
