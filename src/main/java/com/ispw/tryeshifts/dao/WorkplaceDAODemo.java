package com.ispw.tryeshifts.dao;

import com.ispw.tryeshifts.entity.Membership;
import com.ispw.tryeshifts.entity.Workplace;
import com.ispw.tryeshifts.excpetion.DuplicateEntityException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkplaceDAODemo implements WorkplaceDAO {
    private final InMemory db = InMemory.getInstance();

    public void saveWorkplace(Workplace wp) throws DuplicateEntityException {
        if(wp == null) {throw new NullPointerException("Invalid parameters");}
        if(db.getWorkplaces().containsKey(wp.getName())){throw new DuplicateEntityException("Workplace", wp.getName());}
        db.getWorkplaces().put(wp.getName(),wp);
    }
    public void updateWorkplace(Workplace updateWp,String oldName) throws DuplicateEntityException, EntityNotFoundException {
        if(!db.getWorkplaces().containsKey(oldName)){throw new EntityNotFoundException("Workplace", oldName);}
        String newName = updateWp.getName();
        if(!newName.equals(oldName)) {
            if (db.getWorkplaces().containsKey(newName)) {
                throw new DuplicateEntityException("Workplace", newName);
            }
            db.getAvailabilities().stream().filter(a -> a.getWorkplaceName().equals(oldName)).forEach(a -> a.setWorkplaceName(newName));
            db.getWorkplaces().remove(oldName);
        }
        db.getWorkplaces().put(newName, updateWp);
    }
    public boolean existsWorkplaceByName(String name) {
        return name!= null && db.getWorkplaces().containsKey(name);
    }
    public Workplace findWorkplaceByName(String name)throws EntityNotFoundException {
        if (name == null || name.isEmpty()) {
            throw new NullPointerException("name passed null or empty");
        }
        Workplace wp = db.getWorkplaces().get(name);
        if (wp == null) {
            throw new EntityNotFoundException("Workplace", name);
        }
        return wp;
    }
    public List<Workplace> findWorkplacesbyEmail(String email) {
        if(email==null){throw new NullPointerException("email of the user cannot be null");}
        return db.getMemberships().stream().filter(m -> m.getUser().getEmail().equals(email)).map(Membership::getWorkplace).toList();
    }
    public List<Workplace> findAllWorkplaces(){
        return new ArrayList<>(db.getWorkplaces().values());
    }
    public List<Workplace> findWorkplacesByName(String name) throws EntityNotFoundException{
        if(name == null || name.isEmpty()){throw new EntityNotFoundException("Workplace", name);}
        List<Workplace> result = new ArrayList<>();
        String lowerCaseQuery = name.toLowerCase();
        for(Workplace wp : db.getWorkplaces().values()){
            if(wp.getName().toLowerCase().contains(lowerCaseQuery)){
                result.add(wp);
            }
        }
        return result;
    }
    public String getWeekStatus(String workplaceName, String weekId){
        return db.getWeekStatusDbDemo().get(workplaceName + "_" + weekId);
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
