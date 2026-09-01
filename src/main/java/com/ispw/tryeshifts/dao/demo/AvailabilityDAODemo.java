package com.ispw.tryeshifts.dao.demo;

import com.ispw.tryeshifts.dao.AvailabilityDAO;
import com.ispw.tryeshifts.dao.InMemory;
import com.ispw.tryeshifts.entity.Availability;
import com.ispw.tryeshifts.exception.DuplicateEntityException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AvailabilityDAODemo implements AvailabilityDAO {
    private final InMemory db = InMemory.getInstance();

    public void saveAvailability(Availability availability)throws DuplicateEntityException {
        if(availability == null){throw new NullPointerException("Invalid parameters");}
        for(Availability a : db.getAvailabilities()){
            if(a.equals(availability)){throw new DuplicateEntityException("Availability", availability.toString());}
        }
        db.getAvailabilities().add(availability);
    }
    public List<Availability> getAvailabilitiesByWorkplace(String workplaceName,String weekId){
        if (workplaceName == null || workplaceName.isEmpty() || weekId == null) {
            throw new NullPointerException("Invalid parameters: workplace or weekId is null");
        }
        List<Availability> result = new ArrayList<>();
        for (Availability a : db.getAvailabilities()) {
            // Aggiungiamo il controllo sulla settimana
            if (a.getWorkplaceName().equals(workplaceName) && a.getWeekId().equals(weekId)) {
                result.add(a);
            }
        }
        return result;
    }
    public List<Availability> getAvailabilitiesByUser(String email, String workplaceName,String weekId){
        if (email == null || workplaceName == null || weekId == null) {
            throw new NullPointerException("Invalid parameters");
        }
        List<Availability> result = new ArrayList<>();
        for (Availability a : db.getAvailabilities()) {
            // Aggiungiamo il controllo sulla settimana per filtrare le proprie disponibilità
            if (a.getUserEmail().equals(email) &&
                    a.getWorkplaceName().equals(workplaceName) &&
                    a.getWeekId().equals(weekId)) {
                result.add(a);
            }
        }
        return result;
    }
    public void deleteAvailabilitiesByUser(String email, String workplaceName,String weekId) {
        db.getAvailabilities().removeIf(a ->
                a.getUserEmail().equals(email) &&
                        a.getWorkplaceName().equals(workplaceName) &&
                        a.getWeekId().equals(weekId)
        );
    }
    public Map<String, List<String>> getAvailabilitiesByWeek(String workplaceName, String weekId) {
        Map<String, List<String>> weekMap = new HashMap<>();
        for(Availability a : db.getAvailabilities()){
            if(a.getWorkplaceName().equals(workplaceName) && a.getWeekId().equals(weekId)){
                String cellKey = a.getDay() + "_" + a.getFullShift();
                weekMap.computeIfAbsent(cellKey, k -> new ArrayList<>()).add(a.getUserEmail());
            }
        }
        return weekMap;
    }

    @Override
    public void deleteSpecificAvailability(Availability ava) {
        db.getAvailabilities().removeIf(a->
                a.getUserEmail().equalsIgnoreCase(ava.getUserEmail()) &&
                        a.getWorkplaceName().equalsIgnoreCase(ava.getWorkplaceName()) &&
                        a.getWeekId().equals(ava.getWeekId()) &&
                        a.getDay().equalsIgnoreCase(ava.getDay()) &&
                        a.getFullShift().replace(" ", "").equals(ava.getFullShift().replace(" ", "")));
    }

    @Override
    public List<Availability> getAvailabilitiesByUserAndWeek(
            String email,
            String weekId
    ) {
        List<Availability> result = new ArrayList<>();

        for (Availability availability : db.getAvailabilities()) {
            if (availability.getUserEmail().equals(email)
                    && availability.getWeekId().equals(weekId)) {
                result.add(availability);
            }
        }

        return result;
    }

    public void deleteAvailabilitiesByWorkplace(String workplaceName) {
        db.getAvailabilities().removeIf(availability ->
                availability.getWorkplaceName().equals(workplaceName)
        );
    }
    @Override
    public void replaceAvailabilities(String userEmail, String workplaceName, String weekId, List<Availability> availabilities) {
        if (userEmail == null || workplaceName == null || weekId == null || availabilities == null) {
            throw new IllegalArgumentException("Missing availability replacement data");
        }
        db.getAvailabilities().removeIf(availability ->
                availability.getUserEmail().equals(userEmail)
                        && availability.getWorkplaceName().equals(workplaceName)
                        && availability.getWeekId().equals(weekId)
        );

        db.getAvailabilities().addAll(availabilities);
    }

}
