package com.ispw.tryeshifts.appcontroller;

import com.ispw.tryeshifts.config.AppConfig;
import com.ispw.tryeshifts.session.SessionContext;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.dao.*;
import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.entity.Workplace;
import com.ispw.tryeshifts.exception.BaseException;
import com.ispw.tryeshifts.exception.EntityNotFoundException;
import com.ispw.tryeshifts.exception.DataFetchException;
import com.ispw.tryeshifts.utils.SecurityUtils;

import java.util.logging.Logger;

public class SettingsAC {
    private final Logger logger = Logger.getLogger(SettingsAC.class.getName());
    private final UserDAO userRepo = AppConfig.getInstance().getUserRepository();
    private final WorkplaceDAO workplaceRepo = AppConfig.getInstance().getWorkplaceRepository();


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
                logger.info("errore nell'hashing di password0");
            }
        }

        userRepo.updateUser(existingUser);

        SessionContext.getInstance().setLoggeduser(user);
    }
    public void updateWorkplace(WorkplaceBean wp, String oldname) throws BaseException {
        Workplace workplace = workplaceRepo.findWorkplaceByName(oldname);
        if(workplace == null) throw new EntityNotFoundException("Workplace",oldname );

        workplace.setName(wp.getWorkplaceName());
        workplace.setAddress(wp.getAddress());
        workplace.setSelectedDays(wp.getSelectedDays());
        workplace.setShifts(wp.getShiftsBean());

        workplaceRepo.updateWorkplace(workplace, oldname);

        SessionContext.getInstance().setLoggedWorkplace(wp);
    }
}
