package com.ispw.tryeshifts.appcontroller;

import com.ispw.tryeshifts.AppConfig;
import com.ispw.tryeshifts.bean.AvailabilityBean;
import com.ispw.tryeshifts.bean.SessionContext;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.dao.WorkplaceDAO;
import com.ispw.tryeshifts.dao.AvailabilityDAO;
import com.ispw.tryeshifts.entity.Availability;
import com.ispw.tryeshifts.entity.Workplace;
import com.ispw.tryeshifts.excpetion.*;
import com.ispw.tryeshifts.graphcontroller.utilities.ErrorViewManager;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ManageShiftsAC {
    private static final WorkplaceDAO workplaceRepo = AppConfig.getWorkplaceRepository();
    private static final AvailabilityDAO availabilityRepo = AppConfig.getAvailabilityRepository();

    private static final Logger LOGGER = Logger.getLogger(ManageShiftsAC.class.getName());

    public Map<String, List<String>> getShiftData(UserBean user, WorkplaceBean workplace,String weekId ) throws BaseException {
        if(workplace == null||user == null){throw new NullPointerException("Workplace or User passed null");}
        Map<String, List<String>> viewMap = new HashMap<>();

        workplaceRepo.findWorkplaceByName(workplace.getWorkplaceName()); //lancia exception se non trova nulla

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
        return viewMap;
    }

    public void saveAvailabilities(List<AvailabilityBean> beans) throws BaseException {
        LOGGER.info("salvataggio turni");
        if(beans == null){throw new NullPointerException("Bean passed null");}
        String userEmail = SessionContext.getInstance().getLoggeduser().getEmail();
        String wpName = SessionContext.getInstance().getLoggedWorkplace().getWorkplaceName();
        try {
            try {
                availabilityRepo.deleteAvailabilitiesByUser(userEmail, wpName);
            }catch(EntityNotFoundException _){
                LOGGER.info("Nessna disponibilità precendente da rimuovere per: " + userEmail);
            }
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
        }catch(DataFetchException e){
            LOGGER.log(Level.SEVERE, "Errore di persistenza durante il salvataggio", e);
            throw e;
        }
    }

    public String getWeekStatusShifts(String workplaceName, String weekId) throws BaseException {
        if(workplaceName == null || weekId == null){throw new NullPointerException("Parametri di ricerca mancanti");}
        String status = workplaceRepo.getWeekStatus(workplaceName, weekId);

        // Logica di fallback: se il repository restituisce null, la settimana è nuova/aperta
        return (status != null) ? status : "OPEN";
    }

    public void updateWeekStatusShifts(String workplaceName,String weekId, String status)throws BaseException{
        workplaceRepo.updateWeekStatus(workplaceName,weekId,status);
    }

    public static Map<String, Object> getHomeScheduleData(String userEmail, String weekId) throws BaseException {
        if (userEmail == null || weekId == null) {
            throw new NullPointerException("Parametri di ricerca mancanti");
        }

        Map<String, String> assignments = new HashMap<>();
        TreeSet<String> timeSlots = new TreeSet<>();

        List<Workplace> myWorkplaces = workplaceRepo.findWorkplacesbyEmail(userEmail);

        for (Workplace wp : myWorkplaces) {
                // Recuperiamo i turni pubblicati per questo locale
                Map<String, List<String>> shifts = workplaceRepo.getPublishedShiftsByWeek(wp.getName(), weekId);
                System.out.println("Turni trovati per oggi: " + shifts.size());
                for (Map.Entry<String, List<String>> entry : shifts.entrySet()) {
                    if (entry.getValue().stream().anyMatch(email -> email.equalsIgnoreCase(userEmail))) {
                        // Esempio entry.getKey(): "Mon_08:00-09:00"
                        String fullKey = entry.getKey(); // Es: "2026_04_Mon_00:00-01:00"
                        String cleanKey = fullKey.replace(weekId + "_", ""); // Diventa "Mon_00:00-01:00"
                        assignments.put(cleanKey, wp.getName());
                        System.out.println("Turno: " + fullKey + " assegnato a " + wp.getName());
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

    public static String calculateWeekId(int weekOffset) {
        LocalDate targetDate = LocalDate.now().plusWeeks(weekOffset);
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int weekNum = targetDate.get(weekFields.weekOfWeekBasedYear());
        int year = targetDate.get(weekFields.weekBasedYear());
        return year + "_" + String.format("%02d", weekNum);
    }

    public static String getWeekRangeString(int offset) {
        LocalDate start = LocalDate.now().plusWeeks(offset).with(DayOfWeek.MONDAY);
        LocalDate end = start.plusDays(6);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM");
        return start.format(formatter) + " - " + end.format(formatter);
    }

    public static List<Workplace> getUserWorkplaces(String email) throws BaseException{
        return workplaceRepo.findWorkplacesbyEmail(email);
    }
    public static void addShiftstoWorkaplce(ComboBox<String> startMcombo, ComboBox<String> endMcombo, ComboBox<String> startHcombo, ComboBox<String> endHcombo, Label error, ListView<String> shiftsListView){
        String startH = startHcombo.getValue();
        String startM = startMcombo.getValue();
        String endH = endHcombo.getValue();
        String endM = endMcombo.getValue();


        int startTotalMinutes = Integer.parseInt(startH) * 60 + Integer.parseInt(startM);
        int endTotalMinutes = Integer.parseInt(endH) * 60 + Integer.parseInt(endM);


        if (endTotalMinutes  <= startTotalMinutes) {
            ErrorViewManager.showError(error, "La fine deve essere dopo l'inizio");
            return;
        }

        String fullShift = startH + ":" + startM + " - " + endH + ":" + endM;
        for (String existing : shiftsListView.getItems()) {
            if (existing.equals(fullShift)) {
                ErrorViewManager.showError(error, "Turno già presente");
                return;
            }

            // Logica Overlap
            String[] parts = existing.split(" - ");
            if (parts.length < 2) continue;
            int existStart = parseToMinutes(parts[0]);
            int existEnd = parseToMinutes(parts[1]);

            if (startTotalMinutes < existEnd && existStart < startTotalMinutes + (endTotalMinutes - startTotalMinutes)) {
                if (startTotalMinutes < existEnd && existStart < endTotalMinutes) {
                    ErrorViewManager.showError(error, "Si sovrappone a: " + existing);
                    return;
                }
            }
        }

        shiftsListView.getItems().add(fullShift);
        java.util.Collections.sort(shiftsListView.getItems());
    }
    private static int parseToMinutes(String time) {
        String[] hm = time.trim().split(":");

        if (hm.length < 2) {
            throw new IllegalArgumentException("Formato orario errato: " + time);
        }

        int hours = Integer.parseInt(hm[0]);
        int minutes = Integer.parseInt(hm[1]);

        return hours * 60 + minutes;
    }
}
