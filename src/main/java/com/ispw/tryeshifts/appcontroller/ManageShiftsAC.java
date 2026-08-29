package com.ispw.tryeshifts.appcontroller;

import com.ispw.tryeshifts.appcontroller.utils.WeekStatusCalc;
import com.ispw.tryeshifts.config.AppConfig;
import com.ispw.tryeshifts.bean.AvailabilityBean;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.dao.WorkplaceDAO;
import com.ispw.tryeshifts.dao.AvailabilityDAO;
import com.ispw.tryeshifts.entity.Availability;
import com.ispw.tryeshifts.exception.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Pattern;

public class ManageShiftsAC {
    private  final WorkplaceDAO workplaceRepo ;
    private  final AvailabilityDAO availabilityRepo;
    private final  Pattern shiftSeparator = Pattern.compile(" - ");
    private final Pattern timeSeparator = Pattern.compile(":");

    public ManageShiftsAC(WorkplaceDAO workplaceRepo, AvailabilityDAO availabilityRepo) {
        this.workplaceRepo = workplaceRepo;
        this.availabilityRepo = availabilityRepo;
    }
    public ManageShiftsAC() {
        this(AppConfig.getInstance().getWorkplaceRepository(), AppConfig.getInstance().getAvailabilityRepository());
    }


    public Map<String, List<String>> getShiftData(UserBean user, WorkplaceBean workplace ,String weekId) throws BaseException {
        if (workplace == null || user == null || weekId == null) {
            throw new NullPointerException("Workplace or User passed null");
        }
        Map<String, List<String>> viewMap = new HashMap<>();

        workplaceRepo.findWorkplaceByName(workplace.getWorkplaceName());//throws exceptio if workplace name passed is not a real workpalce

        boolean isBoss = user.getEmail().equals(workplace.getOwnerEmail());

        List<Availability> list;

        if (isBoss) {
            list = availabilityRepo.getAvailabilitiesByWorkplace(workplace.getWorkplaceName(), weekId);
        } else {
            list = availabilityRepo.getAvailabilitiesByUser(user.getEmail(), workplace.getWorkplaceName(), weekId);
        }

        for(Availability a : list){
            String shiftKey = a.getFullShift().replace(" ","");
            String key = weekId + "_" + a.getDay() + "_" + shiftKey;
            if(isBoss){
                viewMap.computeIfAbsent(key, k -> new ArrayList<>()).add(a.getUserEmail());
            }else{
                viewMap.computeIfAbsent(key, k -> new ArrayList<>()).add("SELECTED");
            }
        }
        return viewMap;
    }

    public void saveAvailabilities(List<AvailabilityBean> beans,UserBean user, WorkplaceBean workplace,String weekId) throws BaseException {
        if(beans == null){throw new IllegalArgumentException("Bean passed null");}


        if(user == null || workplace == null){throw new BaseException("User or Workplace passed null");}

        String userEmail = user.getEmail();
        String wpName = workplace.getWorkplaceName();

        /*String currentWeekId = beans.isEmpty() ?
                calculateWeekId(1) : // Se è la settimana prossima, serve l'offset corretto
                beans.get(0).getWeekId();*/

        String currentWeekStatus = getWeekStatusShifts(wpName,weekId);

        boolean isBoss = userEmail.equals(workplace.getOwnerEmail());
        if(!isBoss && !currentWeekStatus.equals("OPEN")){
            throw new ValidationException("Failed to save the avaialability, out of temporal window: "+currentWeekStatus, "Shifts");
        }
        // 1. CANCELLAZIONE disponibilità cambiate
        availabilityRepo.deleteAvailabilitiesByUser(userEmail, wpName, weekId);

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
        if(workplaceName == null || weekId == null){throw new NullPointerException("Missing research parameter");}
        String status = workplaceRepo.getWeekStatus(workplaceName, weekId);
        if(status != null) {
            return status;
        }else{
            WeekStatusCalc autoStatus = new  WeekStatusCalc();
            return autoStatus.getAutomaticWeekStatus(weekId);
        }
    }

    public void updateWeekStatusShifts(String workplaceName,String weekId, String status)throws BaseException{
        if(workplaceName == null){
            throw new NullPointerException("Workplacename passed null");
        }
        workplaceRepo.updateWeekStatus(workplaceName,weekId,status);
    }

    public  Map<String, Object> getHomeScheduleData(String userEmail, String weekId) throws BaseException {
        if (userEmail == null || weekId == null) {
            throw new NullPointerException("Missing research parameter");
        }

        //TreeSet per salvare orari in modo ordinato e senza duplicati
        TreeSet<String> timeSlots = new TreeSet<>();
        //Mappa per collegare i diversi wrokpalce con i diversi giorni di lavoro
        Map<String, String> assignments = workplaceRepo.getUserPublishedShiftsByWeek(userEmail, weekId);

        for(String cleanKey : assignments.keySet()){
            String timeParts = cleanKey.split("_")[1];
            timeSlots.add(timeParts);
        }
        //Mappa che tornerò popolata da una mappa associata ad una string e da un oggetto TreeSet assocaito ad una string
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
            throw new IllegalArgumentException("The Shifts must end after it started");
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
            String[] fullShift = fullTime.split("-");
            Availability ava = new Availability(email,workplaceName,day,fullShift[0],fullShift[1],weekId);
            availabilityRepo.deleteSpecificAvailability(ava);
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

}
