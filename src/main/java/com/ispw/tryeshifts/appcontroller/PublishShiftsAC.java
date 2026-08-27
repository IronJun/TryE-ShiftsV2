package com.ispw.tryeshifts.appcontroller;

import com.ispw.tryeshifts.appcontroller.utils.NotificationDispatcher;
import com.ispw.tryeshifts.appcontroller.utils.WeekStatusCalc;
import com.ispw.tryeshifts.config.AppConfig;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.dao.*;
import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.exception.BaseException;
import com.ispw.tryeshifts.exception.ValidationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PublishShiftsAC {
    private final WorkplaceDAO workplaceRepo ;
    private final UserDAO userRepo;
    private final AvailabilityDAO availabilityRepo;

    public PublishShiftsAC(WorkplaceDAO workplaceRepo, UserDAO userRepo, AvailabilityDAO availabilityRepo) {
        this.workplaceRepo = workplaceRepo;
        this.userRepo = userRepo;
        this.availabilityRepo = availabilityRepo;
    }

    public PublishShiftsAC() {
        this(AppConfig.getInstance().getWorkplaceRepository(),AppConfig.getInstance().getUserRepository(),AppConfig.getInstance().getAvailabilityRepository());
    }

    public String handlePublishAction(WorkplaceBean wp, String weekId) throws BaseException {

        if(wp==null || weekId == null){throw new NullPointerException("Workplace or weekId passed null");}

        NotificationDispatcher notifDisp  = new NotificationDispatcher();
        String pubRes;

        Map<String, List<String>> availabilities = availabilityRepo.getAvailabilitiesByWeek(wp.getWorkplaceName(),weekId);
        if(availabilities.isEmpty()){throw new ValidationException("No availabilities found for week " + weekId,"grid");}

        String weekCurrentStatus = workplaceRepo.getWeekStatus(wp.getWorkplaceName(),weekId);
        if(weekCurrentStatus == null){
            WeekStatusCalc autoWeek = new WeekStatusCalc();
            weekCurrentStatus = autoWeek.getAutomaticWeekStatus(weekId);
        }
        if("OPEN".equals(weekCurrentStatus)){
            workplaceRepo.updateWeekStatus(wp.getWorkplaceName(),weekId,"LOCKED");
            pubRes="Shifts have Been locked";
            return pubRes ;
        }else if("LOCKED".equals(weekCurrentStatus)){
            workplaceRepo.savePublishedShifts(wp.getWorkplaceName(), weekId, availabilities);
            workplaceRepo.updateWeekStatus(wp.getWorkplaceName(), weekId, "PUBLISHED");

            String message = " Shifts of "+wp.getWorkplaceName()+" has been successfully published.";
            String type = "SHIFTS";
            notifDisp.sendActiveWorkerNotifAsync(wp.getWorkplaceName(), message,type);
            pubRes = "Shifts of "+wp.getWorkplaceName()+" has been successfully published.";
            return pubRes;
        }


        return "Unable to proceed. Current status is: "+weekCurrentStatus;
    }



    public Map<String, List<String>> getAssignmentsForWeek(WorkplaceBean wp, String weekId) throws BaseException {
        // Chiediamo al repo tutti i turni pubblicati per quel contesto
        if(wp==null || weekId == null){throw new NullPointerException("Workplace or weekId passed null");}
        Map<String, List<String>> rawData = workplaceRepo.getPublishedShiftsByWeek(wp.getWorkplaceName(), weekId);
        // Possiamo convertire le email in nomi reali qui
        Map<String, List<String>> formattedData = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : rawData.entrySet()) {
            List<String> emails = entry.getValue();
            List<String> names = new ArrayList<>();

            for (String email : emails) {
                // Convertiamo ogni email nel nome reale
                if ("SELECTED".equals(email)) {
                    names.add("Lavoratore Demo"); // Fallback per la demo
                } else {
                    UserInfo user = userRepo.findByEmail(email);
                    names.add((user != null) ? user.getName() + " " + user.getSurname() : email );
                }
            }

            formattedData.put(entry.getKey(), names);
        }
        return formattedData;
    }
}
