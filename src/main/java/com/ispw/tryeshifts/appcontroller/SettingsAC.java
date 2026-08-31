package com.ispw.tryeshifts.appcontroller;

import com.ispw.tryeshifts.appcontroller.utils.NotificationDispatcher;
import com.ispw.tryeshifts.bean.NotificationBean;
import com.ispw.tryeshifts.config.AppConfig;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.dao.*;
import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.entity.Workplace;
import com.ispw.tryeshifts.exception.BaseException;
import com.ispw.tryeshifts.exception.EntityNotFoundException;
import com.ispw.tryeshifts.exception.DataFetchException;
import com.ispw.tryeshifts.utils.SecurityUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class SettingsAC {
    private final UserDAO userRepo;
    private final WorkplaceDAO workplaceRepo;
    private final AvailabilityDAO availabilityRepo;

    public SettingsAC(UserDAO userRepo, WorkplaceDAO workplaceRepo, AvailabilityDAO availabilityRepo) {
        this.userRepo = userRepo;
        this.workplaceRepo = workplaceRepo;
        this.availabilityRepo = availabilityRepo;
    }

    public SettingsAC() {
        this(AppConfig.getInstance().getUserRepository(), AppConfig.getInstance().getWorkplaceRepository(), AppConfig.getInstance().getAvailabilityRepository());
    }

    public UserBean updateUserProfile(UserBean user) throws BaseException {
        UserInfo existingUser = userRepo.findByEmail(user.getEmail());
        if (existingUser == null) {
            throw new EntityNotFoundException("User", user.getEmail());
        }
        existingUser.setName(user.getName());
        existingUser.setSurname(user.getSurname());

        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            try {
                String hashedPass = SecurityUtils.hashPassword(user.getPassword());
                existingUser.setPasswordHash(hashedPass);
            } catch (DataFetchException e) {
                throw new DataFetchException("persistency error while changing the password: ", e);
            }
        }

        userRepo.updateUser(existingUser);

        return new UserBean(existingUser.getEmail(), existingUser.getName(), existingUser.getSurname());
    }

    public WorkplaceBean updateWorkplace(WorkplaceBean wp, String oldName) throws BaseException {
        Workplace previousWorkplace = workplaceRepo.findWorkplaceByName(oldName);
        if (previousWorkplace == null) {
            throw new EntityNotFoundException("Workplace", oldName);
        }

        boolean scheduleChanged = hasScheduleChanged(previousWorkplace, wp);

        previousWorkplace.setName(wp.getWorkplaceName());
        previousWorkplace.setAddress(wp.getAddress());
        previousWorkplace.setSelectedDays(wp.getSelectedDays());
        previousWorkplace.setShifts(wp.getShiftsBean());

        // Salva nome, indirizzo, giorni e turni.
        workplaceRepo.updateWorkplace(previousWorkplace, oldName);

        if (scheduleChanged) {
            availabilityRepo.deleteAvailabilitiesByWorkplace(
                    wp.getWorkplaceName()
            );

            NotificationBean notification = new NotificationBean(
                    "Shifts for " + wp.getWorkplaceName()
                            + " have been modified. Please insert again your availabilities",
                    "SHIFTS"
            );

            new NotificationDispatcher().sendActiveWorkerNotifAsync(
                    wp.getWorkplaceName(),
                    notification
            );
        }
        return wp;
    }


    private boolean hasScheduleChanged(Workplace previousWorkplace, WorkplaceBean updatedWorkplace) {
        return !sameElements(previousWorkplace.getSelectedDays(), updatedWorkplace.getSelectedDays()) || !sameElements(previousWorkplace.getShifts(), updatedWorkplace.getShiftsBean());
    }

    private boolean sameElements(List<String> first, List<String> second) {
        Set<String> firstSet = new HashSet<>(first == null ? Collections.emptyList() : first);
        Set<String> secondSet = new HashSet<>(second == null ? Collections.emptyList() : second);
        return firstSet.equals(secondSet);
    }
}
