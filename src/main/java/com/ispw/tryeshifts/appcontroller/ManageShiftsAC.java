package com.ispw.tryeshifts.appcontroller;

import com.ispw.tryeshifts.config.AppConfig;
import com.ispw.tryeshifts.bean.AvailabilityBean;
import com.ispw.tryeshifts.session.SessionContext;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.dao.WorkplaceDAO;
import com.ispw.tryeshifts.dao.AvailabilityDAO;
import com.ispw.tryeshifts.entity.Availability;
import com.ispw.tryeshifts.exception.*;
import com.ispw.tryeshifts.utils.KeyGenerator;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.time.ZoneId;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class ManageShiftsAC {
    private  final WorkplaceDAO workplaceRepo = AppConfig.getWorkplaceRepository();
    private  final AvailabilityDAO availabilityRepo = AppConfig.getAvailabilityRepository();
    private final  Pattern shiftSeparator = Pattern.compile(" - ");
    private final Pattern timeSeparator = Pattern.compile(":");

    private  final Logger logger = Logger.getLogger(ManageShiftsAC.class.getName());

    public Map<String, List<String>> getShiftData(UserBean user, WorkplaceBean workplace ,String weekId) throws BaseException {
        if (workplace == null || user == null || weekId == null) {
            throw new NullPointerException("Workplace or User passed null");
        }
        Map<String, List<String>> viewMap = new HashMap<>();

        workplaceRepo.findWorkplaceByName(workplace.getWorkplaceName()); //lancia exception se non trova nullainterExcept

        boolean isBoss = user.getEmail().equals(workplace.getOwnerEmail());

        List<Availability> list;

        if (isBoss) {
            list = availabilityRepo.getAvailabilitiesByWorkplace(workplace.getWorkplaceName(), weekId);
        } else {
            list = availabilityRepo.getAvailabilitiesByUser(user.getEmail(), workplace.getWorkplaceName(), weekId);
        }

        for(Availability a : list){
            String shiftKey = a.getFullShift().replace(" ","");
            String key = KeyGenerator.buildShiftKey(weekId,a.getDay(),shiftKey);
            if(isBoss){
                viewMap.computeIfAbsent(key, k -> new ArrayList<>()).add(a.getUserEmail());
            }else{
                viewMap.computeIfAbsent(key, k -> new ArrayList<>()).add("SELECTED");
            }
        }
        return viewMap;
    }

    public void saveAvailabilities(List<AvailabilityBean> beans) throws BaseException {
        if(beans == null){throw new IllegalArgumentException("Bean passed null");}
        UserBean loggedUser = SessionContext.getInstance().getLoggeduser();
        WorkplaceBean currentWorkplace = SessionContext.getInstance().getLoggedWorkplace();

        if(loggedUser == null || currentWorkplace == null){throw new BaseException("User or Workplace passed null");}

        String userEmail = loggedUser.getEmail();
        String wpName = currentWorkplace.getWorkplaceName();
        // ATTENZIONE: Se sei nella "settimana prossima", calculateWeekId(0) ti darà la settimana SBAGLIATA.
        // Sarebbe meglio passare il weekId corrente direttamente dalla GUI come parametro.
        String currentWeekId = beans.isEmpty() ?
                calculateWeekId(1) : // Se è la settimana prossima, serve l'offset corretto
                beans.get(0).getWeekId();

        String currentWeekStatus = getWeekStatusShifts(wpName,currentWeekId);

        boolean isBoss = userEmail.equals(currentWorkplace.getOwnerEmail());
        if(!isBoss && !currentWeekStatus.equals("OPEN")){
            throw new ValidationException("Failed to save the avaialability, out of temporal window: "+currentWeekStatus, "Shifts");
        }
        // 1. CANCELLAZIONE
        availabilityRepo.deleteAvailabilitiesByUser(userEmail, wpName, currentWeekId);

        // 2. SALVATAGGIO (deve essere garantito)
        try {
            for (AvailabilityBean bean : beans) {
                Availability entity = new Availability(
                        bean.getUserEmail(),
                        bean.getWorkplaceName(),
                        bean.getDay(),
                        bean.getStartShift(),
                        bean.getEndShifts(),
                        bean.getWeekId()
                );
                availabilityRepo.saveAvailability(entity);
            }
        } catch(DataFetchException e) {
            throw new DataFetchException("Failed to save the avaialability", e);
             // Rilancia per far sapere alla GUI che è fallito
        }
    }

    public String getWeekStatusShifts(String workplaceName, String weekId) throws BaseException {
        if(workplaceName == null || weekId == null){throw new NullPointerException("Parametri di ricerca mancanti");}
        String status = workplaceRepo.getWeekStatus(workplaceName, weekId);

        // Logica di fallback: se il repository restituisce null, la settimana è nuova/aperta
        return (status != null) ? status : getAutomaticWeekStatus(weekId);
    }

    public void updateWeekStatusShifts(String workplaceName,String weekId, String status)throws BaseException{
        workplaceRepo.updateWeekStatus(workplaceName,weekId,status);
    }

    public  Map<String, Object> getHomeScheduleData(String userEmail, String weekId) throws BaseException {
        if (userEmail == null || weekId == null) {
            throw new NullPointerException("Parametri di ricerca mancanti");
        }

        TreeSet<String> timeSlots = new TreeSet<>();
        Map<String, String> assignments = workplaceRepo.getUserPublishedShiftsByWeek(userEmail, weekId);

        for(String cleanKey : assignments.keySet()){
            String timeParts = cleanKey.split("_")[1];
            timeSlots.add(timeParts);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("assignments", assignments);
        result.put("slots", timeSlots);
        return  result;
    }

    public  String calculateWeekId(int weekOffset) {
        LocalDate targetDate = LocalDate.now(ZoneId.systemDefault()).plusWeeks(weekOffset);
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int weekNum = targetDate.get(weekFields.weekOfWeekBasedYear());
        int year = targetDate.get(weekFields.weekBasedYear());
        return year + "_" + String.format("%02d", weekNum);
    }

    public  String getWeekRangeString(int offset) {
        LocalDate start = LocalDate.now(ZoneId.systemDefault()).plusWeeks(offset).with(DayOfWeek.MONDAY);
        LocalDate end = start.plusDays(6);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM");
        return start.format(formatter) + " - " + end.format(formatter);
    }



    public  String addShiftstoWorkaplce(String startM, String startH, String endM, String endH, List<String> existingShifts)throws BaseException{
        int startTotalMinutes = parseToMinutes(startH+ timeSeparator +startM);
        int endTotalMinutes = parseToMinutes(endH+ timeSeparator +endM);

        if (endTotalMinutes  <= startTotalMinutes) {
            throw new IllegalArgumentException("La fine deve essere dopo l'inizio");
        }

        String fullShift = startH + timeSeparator + startM + shiftSeparator + endH + timeSeparator + endM;


        for (String existing : existingShifts) {
            if (existing.equals(fullShift)) {
                throw new DuplicateEntityException("shifts","this shifts already exists");
            }

            // Logica Overlap
            String[] parts = shiftSeparator.split(existing);
            if (parts.length < 2) continue;
            int existStart = parseToMinutes(parts[0]);
            int existEnd = parseToMinutes(parts[1]);

            if (startTotalMinutes<existEnd && existStart<= endTotalMinutes) {
                    throw new IllegalArgumentException("not valid shift: overlapping other shifts");
            }
        }

        return fullShift;
    }

    public void removeWorkerFromShift(String email, String workplaceName, String weekId, String day, String fullTime)throws BaseException {
        if(email == null || workplaceName == null || weekId == null || day == null){
            throw new NullPointerException("Missing parameter to remove someone from the shifts");
        }
        try{
            availabilityRepo.deleteSpecificAvailability(email, workplaceName, weekId, day, fullTime);
        }catch(Exception _){
            throw new DataFetchException("Impossibile eliminare il turno dalla memoria");
        }
    }

    private  int parseToMinutes(String time) {
        String[] hm = timeSeparator.split(time.trim());

        if (hm.length < 2) {
            throw new IllegalArgumentException("Formato orario errato: " + time);
        }

        int hours = Integer.parseInt(hm[0]);
        int minutes = Integer.parseInt(hm[1]);

        return hours * 60 + minutes;
    }

    private String getAutomaticWeekStatus(String weekId){
        String[] parts = weekId.split("_");
        int year = Integer.parseInt(parts[0]);
        int week = Integer.parseInt(parts[1]);

        LocalDate targetMonday = LocalDate.ofYearDay(year, 1)
                .with(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear(), week)
                .with(DayOfWeek.MONDAY);

        LocalDateTime weDeadLine = targetMonday.minusDays(5).atTime(23,59,59);
        LocalDateTime friDeadline = targetMonday.minusDays(3).atTime(23,59,59);
        LocalDateTime sunDeadline = targetMonday.minusDays(1).atTime(23,59,59);
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());

        if(now.isAfter(friDeadline)&&now.isBefore(sunDeadline)){
            return "PUBLISHED";
        }
        if(now.isAfter(weDeadLine) && now.isBefore(friDeadline)){
            return "LOCKED";
        }
        return "OPEN";
    }
}
