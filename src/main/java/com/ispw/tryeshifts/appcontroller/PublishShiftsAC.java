package com.ispw.tryeshifts.appcontroller;

import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.config.AppConfig;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.dao.*;
import com.ispw.tryeshifts.entity.Membership;
import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.excpetion.BaseException;
import com.ispw.tryeshifts.excpetion.ValidationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Manifest;

public class PublishShiftsAC {
    private final WorkplaceDAO workplaceRepo = AppConfig.getWorkplaceRepository();
    private final UserDAO userRepo = AppConfig.getUserRepository();
    private final AvailabilityDAO availabilityRepo = AppConfig.getAvailabilityRepository();
    private final NotificationDAO notificationRepo = AppConfig.getNotificationRepository();
    private final MembershipDAO membershipRepo = AppConfig.getMembershipRepository();

    public void publish(WorkplaceBean wp, String weekId) throws BaseException {

        if(wp==null || weekId == null){throw new NullPointerException("Workplace or weekId passed null");}
        Map<String, List<String>> availabilities = availabilityRepo.getAvailabilitiesByWeek(wp.getWorkplaceName(),weekId);

        if(availabilities.isEmpty()){throw new ValidationException("No availabilities found for week " + weekId,"grid");}
        //List<UserBean> workers = ManageMembersAC.getActiveMembers(wp.getWorkplaceName());
        List<Membership> memberships = membershipRepo.getMembershipsByWorkplace(wp.getWorkplaceName());
        List<String> workersEmail = new ArrayList<>();
        for(Membership membership : memberships){
            if(membership.isAccepted()){
                workersEmail.add(membership.getUser().getEmail());
            }
        }
        String message = " Shifts of "+wp.getWorkplaceName()+" has been successfully published.";
        String type = "SHIFTS";
        for(String email: workersEmail){
            if(membershipRepo.findMembership(email,wp.getWorkplaceName()).getRole() != "OWNER") {
                notificationRepo.saveNotification(email, message, type);
            }
        }
        workplaceRepo.savePublishedShifts(wp.getWorkplaceName(), weekId, availabilities);
        workplaceRepo.updateWeekStatus(wp.getWorkplaceName(), weekId, "PUBLISHED");

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
