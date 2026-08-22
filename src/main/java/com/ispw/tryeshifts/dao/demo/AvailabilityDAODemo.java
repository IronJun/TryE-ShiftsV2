package com.ispw.tryeshifts.dao.demo;

import com.ispw.tryeshifts.dao.AvailabilityDAO;
import com.ispw.tryeshifts.dao.InMemory;
import com.ispw.tryeshifts.entity.Availability;
import com.ispw.tryeshifts.excpetion.DataFetchException;
import com.ispw.tryeshifts.excpetion.DuplicateEntityException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;

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
    public void deleteAvailabilitiesByUser(String email, String workplaceName,String weekId) throws EntityNotFoundException {
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
    public void deleteSpecificAvailability(String email, String workplaceName, String weekId, String day, String fullTime) throws EntityNotFoundException, DataFetchException {
        db.getAvailabilities().removeIf(a->
                a.getUserEmail().equalsIgnoreCase(email) &&
                        a.getWorkplaceName().equalsIgnoreCase(workplaceName) &&
                        a.getWeekId().equals(weekId) &&
                        a.getDay().equalsIgnoreCase(day) &&
                        a.getFullShift().replace(" ", "").equals(fullTime.replace(" ", "")));
    }
}
