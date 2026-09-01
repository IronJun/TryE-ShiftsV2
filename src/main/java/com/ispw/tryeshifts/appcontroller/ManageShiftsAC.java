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


        String currentWeekStatus = getWeekStatusShifts(wpName,weekId);

        boolean isBoss = userEmail.equals(workplace.getOwnerEmail());
        if(!isBoss && !currentWeekStatus.equals("OPEN")){
            throw new ValidationException("Failed to save the avaialability, out of temporal window: "+currentWeekStatus, "Shifts");
        }
        validateNoCrossWorkplaceConflicts(beans,userEmail,wpName,weekId);
        List<Availability> availabilities = new ArrayList<>();
        for (AvailabilityBean bean : beans) {
            availabilities.add(new Availability(userEmail, wpName, bean.getDay(), bean.getStartShift(), bean.getEndShifts(), weekId));
        }
        availabilityRepo.replaceAvailabilities(userEmail, wpName, weekId, availabilities
        );
    }

    //torna lo status se è locked o published
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
        WeekFields weekFields = WeekFields.ISO;
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

    public String addShiftstoWorkaplce(
            String startM, String startH,
            String endM, String endH,
            List<String> existingShifts
    ) throws BaseException {

        String startTime = startH + ":" + startM;
        String endTime = endH + ":" + endM;

        if (parseToMinutes(startTime) == parseToMinutes(endTime)) {
            throw new IllegalArgumentException(
                    "Un turno non può iniziare e finire allo stesso orario");
        }

        ShiftInterval newShift =
                createShiftInterval("Mon", startTime, endTime);

        String fullShift = startTime + " - " + endTime;

        for (String existing : existingShifts) {
            if (existing.equals(fullShift)) {
                throw new DuplicateEntityException(
                        "shifts", "This shift already exists");
            }

            String[] parts = shiftSeparator.split(existing);
            if (parts.length != 2) {
                continue;
            }

            ShiftInterval existingShift = createShiftInterval(
                    "Mon",
                    parts[0].trim(),
                    parts[1].trim()
            );

            if (repeatedDailyShiftsOverlap(newShift, existingShift)) {
                throw new IllegalArgumentException(
                        "Not valid shift: overlapping another shift");
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

    private void validateNoCrossWorkplaceConflicts(List<AvailabilityBean> newAvailabilities, String userEmail, String currentWorkplace, String weekId) throws BaseException {

        List<Availability> savedAvailabilities =
                availabilityRepo.getAvailabilitiesByUserAndWeek(userEmail, weekId);

        for (AvailabilityBean newAvailability : newAvailabilities) {
            for (Availability savedAvailability : savedAvailabilities) {

                // Le disponibilità del workplace corrente vengono sostituite
                // dal salvataggio attuale: non devono essere confrontate.
                if (savedAvailability.getWorkplaceName().equals(currentWorkplace)) {
                    continue;
                }

                if (shiftsOverlap(
                        newAvailability.getDay(),
                        newAvailability.getStartShift(),
                        newAvailability.getEndShifts(),
                        savedAvailability.getDay(),
                        savedAvailability.getStartShift(),
                        savedAvailability.getEndShift())) {

                    String newShift = formatShift(
                            newAvailability.getDay(),
                            newAvailability.getStartShift(),
                            newAvailability.getEndShifts()
                    );

                    String existingShift = formatShift(
                            savedAvailability.getDay(),
                            savedAvailability.getStartShift(),
                            savedAvailability.getEndShift()
                    );

                    throw new ValidationException(
                            "The shift "
                                    + newShift
                                    + " could not be saved for:  "
                                    + currentWorkplace
                                    + " because it's overlapping with:  "
                                    + existingShift
                                    + " for  "
                                    + savedAvailability.getWorkplaceName()
                                    + ".",
                            "shifts"
                    );
                }
            }
        }
    }
    private boolean shiftsOverlap(String firstDay, String firstStart, String firstEnd, String secondDay, String secondStart, String secondEnd) {
        ShiftInterval first = createShiftInterval(firstDay, firstStart, firstEnd);
        ShiftInterval second = createShiftInterval(secondDay, secondStart, secondEnd);

        // Intervalli consecutivi, es. 08:00-10:00 e 10:00-12:00,
        // non sono sovrapposti.
        return intervalsOverlap(first, second);
    }
    private ShiftInterval createShiftInterval(
            String day,
            String startTime,
            String endTime
    ) {
        int dayOffset = getDayOffset(day);
        int start = dayOffset + parseToMinutes(startTime);
        int end = dayOffset + parseToMinutes(endTime);

        // Il turno termina il giorno successivo, es. 18:00-02:00.
        if (end < start) {
            end += 24 * 60;
        }

        return new ShiftInterval(start, end);
    }
    private int getDayOffset(String day) {
        return switch (day) {
            case "Mon" -> 0;
            case "Tue" -> 24 * 60;
            case "Wed" -> 2 * 24 * 60;
            case "Thu" -> 3 * 24 * 60;
            case "Fri" -> 4 * 24 * 60;
            case "Sat" -> 5 * 24 * 60;
            case "Sun" -> 6 * 24 * 60;
            default -> throw new IllegalArgumentException(
                    "Giorno non valido: " + day);
        };
    }
    private String formatShift(String day, String start, String end) {
        return day + " " + start + " - " + end;
    }

    private record ShiftInterval(int start, int end) {
    }
    private boolean intervalsOverlap(
            ShiftInterval first,
            ShiftInterval second
    ) {
        return first.start() < second.end()
                && second.start() < first.end();
    }

    private boolean repeatedDailyShiftsOverlap(
            ShiftInterval first,
            ShiftInterval second
    ) {
        int dayMinutes = 24 * 60;

        // Serve per confrontare anche, per esempio:
        // 18:00-02:00 con 01:00-03:00.
        for (int offset = -dayMinutes; offset <= dayMinutes;
             offset += dayMinutes) {

            ShiftInterval shiftedSecond = new ShiftInterval(
                    second.start() + offset,
                    second.end() + offset
            );

            if (intervalsOverlap(first, shiftedSecond)) {
                return true;
            }
        }

        return false;
    }
}
