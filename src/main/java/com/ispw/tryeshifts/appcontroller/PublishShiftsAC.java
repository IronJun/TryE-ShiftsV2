package com.ispw.tryeshifts.appcontroller;

import com.ispw.tryeshifts.AppConfig;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.dao.InMemory;
import com.ispw.tryeshifts.dao.Repository;
import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.excpetion.DAOException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;
import com.ispw.tryeshifts.graphcontroller.utilities.ShiftDistributor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PublishShiftsAC {
    Repository repo = AppConfig.getRepository();
    public boolean canBossLock(int offset){
        if(offset<0) return false;
        if(offset== 0)return true;
        if(offset==1){
            LocalDate today = LocalDate.now();
            LocalDate nextMonday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
            long daysUntilMonday = ChronoUnit.DAYS.between(today, nextMonday);

            return daysUntilMonday <=2;
        }
        return false;
    }

    public void publish(WorkplaceBean wp, String weekId){


        Map<String, List<String>> availabilities = repo.getAvailabilitiesByWeek(wp.getWorkplaceName(),weekId);
        //ShiftDistributor distributor = new ShiftDistributor();
        //Map<String, String> finalAssignments = distributor.distribute(availabilities);

        // 4. Salviamo i turni pubblicati nel repository
        repo.savePublishedShifts(wp.getWorkplaceName(), weekId, availabilities);

        // 5. Aggiorniamo lo stato della settimana
        repo.updateWeekStatus(wp.getWorkplaceName(), weekId, "PUBLISHED");

    }


    public Map<String, List<String>> getAssignmentsForWeek(WorkplaceBean wp, String weekId) throws DAOException, EntityNotFoundException {
        Repository repo = AppConfig.getRepository();
        // Chiediamo al repo tutti i turni pubblicati per quel contesto
        Map<String, List<String>> rawData = repo.getPublishedShiftsByWeek(wp.getWorkplaceName(), weekId);

        // Possiamo convertire le email in nomi reali qui
        Map<String, List<String>> formattedData = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : rawData.entrySet()) {
            List<String> emails = entry.getValue();
            List<String> names = new ArrayList<>();

            for (String email : emails) {
                // Convertiamo ogni email nel nome reale
                UserInfo user = repo.findByEmail(email);
                String displayName = (user != null) ? user.getName() + " " + user.getSurname() : email;
                names.add(displayName);
            }

            formattedData.put(entry.getKey(), names);
        }
        return formattedData;
    }
}
