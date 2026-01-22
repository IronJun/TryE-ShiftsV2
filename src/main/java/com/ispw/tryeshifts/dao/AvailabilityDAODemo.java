package com.ispw.tryeshifts.dao;

import com.ispw.tryeshifts.entity.Availability;
import com.ispw.tryeshifts.excpetion.DAOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AvailabilityDAODemo implements AvailabilityDAO{
    private final InMemory db = InMemory.getInstance();

    public void saveAvailability(Availability availability)throws DAOException {
        if(availability == null){throw new DAOException("Invalid parameters");}
        db.getAvailabilities().add(availability);
    }
    public List<Availability> getAvailabilitiesByWorkplace(String workplaceName) throws DAOException {
        if(workplaceName == null || workplaceName.isEmpty()){throw new DAOException("Invalid parameters");}
        List<Availability> result = new ArrayList<>();
        for (Availability a : db.getAvailabilities()) {
            if (a.getWorkplaceName().equals(workplaceName)) {
                result.add(a);
            }
        }
        return result;
    }
    public List<Availability> getAvailabilitiesByUser(String email, String workplaceName) throws DAOException{
        if(email == null || workplaceName == null){throw new DAOException("Invalid parameters");}
        List<Availability> result = new ArrayList<>();
        for (Availability a : db.getAvailabilities()) {
            if (a.getUserEmail().equals(email) && a.getWorkplaceName().equals(workplaceName)) {
                result.add(a);
            }
        }
        return result;
    }
    public void deleteAvailabilitiesByUser(String email, String workplaceName) throws DAOException {
        if(email == null || workplaceName == null){throw new DAOException("Invalid parameters");}
        db.getAvailabilities().removeIf(a ->
                a.getUserEmail().equals(email) && a.getWorkplaceName().equals(workplaceName)
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
}
