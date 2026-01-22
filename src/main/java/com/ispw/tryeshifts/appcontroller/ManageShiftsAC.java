package com.ispw.tryeshifts.appcontroller;

import com.ispw.tryeshifts.AppConfig;
import com.ispw.tryeshifts.bean.AvailabilityBean;
import com.ispw.tryeshifts.bean.SessionContext;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.dao.UserDAO;
import com.ispw.tryeshifts.dao.WorkplaceDAO;
import com.ispw.tryeshifts.dao.AvailabilityDAO;
import com.ispw.tryeshifts.dao.MembershipDAO;
import com.ispw.tryeshifts.entity.Availability;
import com.ispw.tryeshifts.entity.Workplace;
import com.ispw.tryeshifts.excpetion.DAOException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;
import com.ispw.tryeshifts.excpetion.ShiftPersistenceException;

import java.util.*;
import java.util.logging.Logger;

public class ManageShiftsAC {
    private static final WorkplaceDAO workplaceRepo = AppConfig.getWorkplaceRepository();
    private static final UserDAO userRepo = AppConfig.getUserRepository();
    private static final MembershipDAO membershipRepo = AppConfig.getMembershipRepository();
    private static final AvailabilityDAO availabilityRepo = AppConfig.getAvailabilityRepository();

    private static final Logger LOGGER = Logger.getLogger(ManageShiftsAC.class.getName());

    public Map<String, List<String>> getShiftData(UserBean user, WorkplaceBean workplace,String weekId ) throws DAOException, EntityNotFoundException {
        if(workplace == null||user == null){throw new DAOException("Workplace or User not found");}
        Map<String, List<String>> viewMap = new HashMap<>();

        if(workplaceRepo.findWorkplaceByName(workplace.getWorkplaceName()) == null){throw new EntityNotFoundException("Workplace not found");}

        boolean isBoss = user.getEmail().equals(workplace.getOwnerEmail());

        List<Availability> list;

        if(isBoss){
            list = availabilityRepo.getAvailabilitiesByWorkplace(workplace.getWorkplaceName());
        }else{
            list = availabilityRepo.getAvailabilitiesByUser(user.getEmail(),workplace.getWorkplaceName());
        }

        for(Availability a : list){
            String shiftKey = a.getFullShift().replace(" ","");
            String key = a.getDay() + "_" + shiftKey;
            if(isBoss){
                viewMap.computeIfAbsent(key, k -> new ArrayList<>()).add(a.getUserEmail());
            }else{
                viewMap.computeIfAbsent(key, k -> new ArrayList<>()).add("SELECTED");
            }
        }
        return availabilityRepo.getAvailabilitiesByWeek(workplace.getWorkplaceName(),weekId);
    }

    public void saveAvailabilities(List<AvailabilityBean> beans) throws ShiftPersistenceException {
        LOGGER.info("salvataggio turni");
        if(beans == null){throw new ShiftPersistenceException("Bean non valido");}

        try {
            // Usiamo il SessionContext per sicurezza o i dati del primo bean
            String userEmail = SessionContext.getInstance().getLoggeduser().getEmail();
            String wpName = SessionContext.getInstance().getLoggedWorkplace().getWorkplaceName();

            // 1. PULIZIA: Rimuoviamo le vecchie disponibilità per evitare duplicati
            // Questo è fondamentale perché se l'utente cambia idea, il DB deve riflettere l'ultima scelta
            availabilityRepo.deleteAvailabilitiesByUser(userEmail, wpName);

            // 2. SALVATAGGIO: Trasformiamo i Bean in Entity e le salviamo nel Repository
            for (AvailabilityBean bean : beans) {
                LOGGER.info("LOG AC: Salvataggio per " + bean.getUserEmail() + " al " + bean.getDay());
                Availability entity = new Availability(
                        bean.getUserEmail(),
                        bean.getWorkplaceName(),
                        bean.getDay(),
                        bean.getStartShift(),
                        bean.getEndShifts(),
                        bean.getWeekId()// Assicurati che nel Bean si chiami shift o timeSlot come nella Entity
                );
                availabilityRepo.saveAvailability(entity);
            }
        }catch(DAOException _){
            throw new ShiftPersistenceException("Errire tecnico durante salvataggio disponibilità");
        }
    }

    public String getWeekStatusShifts(String workplaceName, String weekId) {
        // Chiamata al repository (sia esso in memoria o DB)
        String status = workplaceRepo.getWeekStatus(workplaceName, weekId);

        // Logica di fallback: se il repository restituisce null, la settimana è nuova/aperta
        return (status != null) ? status : "OPEN";
    }

    public void updateWeekStatusShifts(String workplaceName,String weekId, String status){
        workplaceRepo.updateWeekStatus(workplaceName,weekId,status);
    }

    public static Map<String, Object> getHomeScheduleData(String userEmail, String weekId) throws DAOException {
        Map<String, String> assignments = new HashMap<>();
        TreeSet<String> timeSlots = new TreeSet<>();

        List<Workplace> myWorkplaces = workplaceRepo.findWorkplacesbyEmail(userEmail);

        for (Workplace wp : myWorkplaces) {
            // Recuperiamo i turni pubblicati per questo locale
            Map<String, List<String>> shifts = workplaceRepo.getPublishedShiftsByWeek(wp.getName(), weekId);

            for (Map.Entry<String, List<String>> entry : shifts.entrySet()) {
                if (entry.getValue().stream().anyMatch(email -> email.equalsIgnoreCase(userEmail))) {
                    // Esempio entry.getKey(): "Mon_08:00-09:00"
                    String fullKey = entry.getKey(); // Es: "2026_04_Mon_00:00-01:00"
                    String cleanKey = fullKey.replace(weekId + "_", ""); // Diventa "Mon_00:00-01:00"
                    assignments.put(cleanKey, wp.getName());

                    // Aggiungiamo l'orario al set per le righe della tabella
                    String timePart = cleanKey.split("_")[1];
                    timeSlots.add(timePart);
                }
            }
        }

        // Impacchettiamo tutto in una mappa generica
        Map<String, Object> result = new HashMap<>();
        result.put("assignments", assignments);
        result.put("slots", timeSlots);
        return result;
    }


    public static List<Workplace> getUserWorkplaces(String email) throws DAOException {
        return workplaceRepo.findWorkplacesbyEmail(email);
    }

}
