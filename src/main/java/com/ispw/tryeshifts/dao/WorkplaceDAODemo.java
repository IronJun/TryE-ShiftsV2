package com.ispw.tryeshifts.dao;

import com.ispw.tryeshifts.entity.Availability;
import com.ispw.tryeshifts.entity.Membership;
import com.ispw.tryeshifts.entity.Workplace;
import com.ispw.tryeshifts.excpetion.DAOException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkplaceDAODemo implements WorkplaceDAO {
    private final InMemory db = InMemory.getInstance();

    public void saveWorkplace(Workplace wp) throws DAOException {
        if(wp != null) {
            db.getWorkplaces().put(wp.getName(), wp);
        }else{
            throw new DAOException("No workplace passed");
        }
    }
    public void updateWorkplace(Workplace updateWp,String oldName) throws DAOException, EntityNotFoundException {
        if(!db.getWorkplaces().containsKey(oldName)){throw new EntityNotFoundException("Workplace not found");}
        String newName = updateWp.getName();
        if(!newName.equals(oldName)) {
            if (db.getWorkplaces().containsKey(newName)) {
                throw new DAOException("Workplace with name: " + newName + " already exists");
            }
            for (Availability a : db.getAvailabilities()) {
                if (a.getWorkplaceName().equals(oldName)) {
                    a.setWorkplaceName(newName);
                }
            }
            db.getWorkplaces().remove(oldName);
        }
        db.getWorkplaces().put(newName, updateWp);
    }
    public boolean existsWorkplaceByName(String name) throws DAOException{
        if(name != null) {
            return db.getWorkplaces().containsKey(name);
        }else{
            throw new DAOException("No workplace name passed");
        }
    }
    public Workplace findWorkplaceByName(String name) throws EntityNotFoundException,DAOException{
        if(name == null || name.isEmpty()){throw new DAOException("Workplace name cannot be empty");}
        Workplace wp = db.getWorkplaces().get(name);
        if(wp == null){throw new EntityNotFoundException("Workplace with name: " + name + " not found");}
        return wp;
    }
    public List<Workplace> findWorkplacesbyEmail(String email) throws DAOException{
        try{
            return db.getMemberships().stream().filter(m -> m.getUser().getEmail().equals(email)).map(Membership::getWorkplace).toList();
        }catch(Exception _){
            throw new DAOException("Cannot resolve the Workplaces for this User");
        }
    }
    public List<Workplace> findAllWorkplaces(){
        return new ArrayList<>(db.getWorkplaces().values());
    }
    public List<Workplace> findWorkplacesByName(String name) throws EntityNotFoundException{
        if(name == null || name.isEmpty()){throw new EntityNotFoundException("Workplace name cannot be empty");}
        List<Workplace> result = new ArrayList<>();
        String lowerCaseQuery = name.toLowerCase();
        for(Workplace wp : db.getWorkplaces().values()){
            if(wp.getName().toLowerCase().contains(lowerCaseQuery)){
                result.add(wp);
            }
        }
        return result;
    }
    public String getWeekStatus(String WorkplaceName, String weekId){
        return db.getWeekStatusDbDemo().get(WorkplaceName + "_" + weekId);
    }
    public void updateWeekStatus(String workplaceName, String weekId, String newStatus) {
        // Usiamo la stessa chiave usata per il recupero
        db.getWeekStatusDbDemo().put(workplaceName + "_" + weekId, newStatus);
    }
    public void savePublishedShifts(String workplace, String weekId, Map<String, List<String>> assignments) {
        assignments.forEach((cellKey, email) -> {
            String fullKey = workplace + "_" + weekId + "_" + cellKey;
            db.getPublishedShifts().put(fullKey, new ArrayList<>(email));
        });
    }
    public Map<String, List<String>> getPublishedShiftsByWeek(String workplaceName, String weekId) {
        Map<String, List<String>> filteredAssignments = new HashMap<>();

        // Il prefisso che identifica univocamente la settimana per quel posto di lavoro
        String prefix = workplaceName + "_" + weekId + "_";

        for (Map.Entry<String, List<String>> entry : db.getPublishedShifts().entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(prefix)) {
                // Rimuoviamo il prefisso per restituire alla UI solo la "CellKey"
                // (es. "Mon_08:00-09:00") così la Factory la trova subito
                String cellKey = key.substring(prefix.length());
                filteredAssignments.put(cellKey, new ArrayList<>(entry.getValue()));
            }
        }

        return filteredAssignments;
    }
}
