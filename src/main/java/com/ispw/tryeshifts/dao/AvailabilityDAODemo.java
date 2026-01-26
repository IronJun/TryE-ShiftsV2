package com.ispw.tryeshifts.dao;

import com.ispw.tryeshifts.entity.Availability;
import com.ispw.tryeshifts.excpetion.DuplicateEntityException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AvailabilityDAODemo implements AvailabilityDAO{
    private final InMemory db = InMemory.getInstance();

    public void saveAvailability(Availability availability)throws DuplicateEntityException {
        if(availability == null){throw new NullPointerException("Invalid parameters");}
        for(Availability a : db.getAvailabilities()){
            if(a.equals(availability)){throw new DuplicateEntityException("Availability", availability.toString());}
        }
        db.getAvailabilities().add(availability);
    }
    public List<Availability> getAvailabilitiesByWorkplace(String workplaceName){
        if(workplaceName == null || workplaceName.isEmpty()){throw new NullPointerException("Invalid parameters");}
        List<Availability> result = new ArrayList<>();
        for (Availability a : db.getAvailabilities()) {
            if (a.getWorkplaceName().equals(workplaceName)) {
                result.add(a);
            }
        }
        return result;
    }
    public List<Availability> getAvailabilitiesByUser(String email, String workplaceName){
        if(email == null || workplaceName == null){throw new NullPointerException("Invalid parameters");}
        List<Availability> result = new ArrayList<>();
        for (Availability a : db.getAvailabilities()) {
            if (a.getUserEmail().equals(email) && a.getWorkplaceName().equals(workplaceName)) {
                result.add(a);
            }
        }
        return result;
    }
    public void deleteAvailabilitiesByUser(String email, String workplaceName) throws EntityNotFoundException {
        if(email == null || workplaceName == null){throw new NullPointerException("Invalid parameters");}
        boolean removed = db.getAvailabilities().removeIf(a ->
                a.getUserEmail().equals(email) && a.getWorkplaceName().equals(workplaceName)
        );
        if (!removed) {
            throw new EntityNotFoundException("Availability", email + " in " + workplaceName);
        }
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
}
