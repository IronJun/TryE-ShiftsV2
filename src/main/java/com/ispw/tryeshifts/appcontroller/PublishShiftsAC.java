package com.ispw.tryeshifts.appcontroller;

import com.ispw.tryeshifts.AppConfig;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.dao.UserDAO;
import com.ispw.tryeshifts.dao.WorkplaceDAO;
import com.ispw.tryeshifts.dao.AvailabilityDAO;
import com.ispw.tryeshifts.dao.MembershipDAO;
import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.excpetion.DAOException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PublishShiftsAC {
    private static final WorkplaceDAO workplaceRepo = AppConfig.getWorkplaceRepository();
    private static final UserDAO userRepo = AppConfig.getUserRepository();
    private static final MembershipDAO membershipRepo = AppConfig.getMembershipRepository();
    private static final AvailabilityDAO availabilityRepo = AppConfig.getAvailabilityRepository();

    public void publish(WorkplaceBean wp, String weekId){


        Map<String, List<String>> availabilities = availabilityRepo.getAvailabilitiesByWeek(wp.getWorkplaceName(),weekId);

        workplaceRepo.savePublishedShifts(wp.getWorkplaceName(), weekId, availabilities);

        // 5. Aggiorniamo lo stato della settimana
        workplaceRepo.updateWeekStatus(wp.getWorkplaceName(), weekId, "PUBLISHED");

    }

    public Map<String, List<String>> getAssignmentsForWeek(WorkplaceBean wp, String weekId) throws DAOException, EntityNotFoundException {
        // Chiediamo al repo tutti i turni pubblicati per quel contesto
        Map<String, List<String>> rawData = workplaceRepo.getPublishedShiftsByWeek(wp.getWorkplaceName(), weekId);

        // Possiamo convertire le email in nomi reali qui
        Map<String, List<String>> formattedData = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : rawData.entrySet()) {
            List<String> emails = entry.getValue();
            List<String> names = new ArrayList<>();

            for (String email : emails) {
                // Convertiamo ogni email nel nome reale
                UserInfo user = userRepo.findByEmail(email);
                String displayName = (user != null) ? user.getName() + " " + user.getSurname() : email;
                names.add(displayName);
            }

            formattedData.put(entry.getKey(), names);
        }
        return formattedData;
    }
}
