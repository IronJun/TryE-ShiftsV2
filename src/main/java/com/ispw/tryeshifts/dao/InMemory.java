package com.ispw.tryeshifts.dao;

import com.ispw.tryeshifts.entity.Availability;
import com.ispw.tryeshifts.entity.Membership;
import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.entity.Workplace;
import com.ispw.tryeshifts.excpetion.DAOException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InMemory implements Repository {
    private static final Map<String, UserInfo> usersDbDemo = new HashMap<>();
    private static final Map<String, Workplace> workplacesDbDemo = new HashMap<>();
    private static final List<Membership> membershipsDbDemo = new ArrayList<>();
    private static final List<Availability> availabilities = new ArrayList<>();

    private static final Map<String, String> weekStatusDbDemo = new HashMap<>();

    // 2. Turni Pubblicati: "2026_04_Lunedì_09:00" -> Email del lavoratore scelto
    private static final Map<String, String> publishedShiftsDbDemo = new HashMap<>();




    public void save(UserInfo user) throws DAOException{
        if(user != null){
            usersDbDemo.put(user.getEmail(),user);

        }else{
            throw new DAOException("No user passed");
        }
    }

    public UserInfo findByEmail(String email) throws EntityNotFoundException{
        UserInfo user = usersDbDemo.get(email);
        if(user == null){
            throw new EntityNotFoundException("User with email: " + email + " not found");
        }
        return user;
    }

    public void saveWorkplace(Workplace wp) throws DAOException{
        if(wp != null) {
            workplacesDbDemo.put(wp.getName(), wp);
        }else{
            throw new DAOException("No workplace passed");
        }
    }

    public void saveMembership(Membership m)throws DAOException{
        if(m != null) {
            membershipsDbDemo.add(m);
        }else{
            throw new DAOException("No membership passed");
        }
    }

    public boolean existsWorkplaceByName(String name) throws DAOException{
        if(name != null) {
            return workplacesDbDemo.containsKey(name);
        }else{
            throw new DAOException("No workplace name passed");
        }
    }

    public List<Workplace> findWorkplacesbyEmail(String email) throws DAOException{
        try{
            return membershipsDbDemo.stream().filter(m -> m.getUser().getEmail().equals(email)).map(Membership::getWorkplace).toList();
        }catch(Exception _){
            throw new DAOException("Cannot resolve the Workplaces for this User");
        }
    }

    public List<Workplace> findAllWorkplaces(){
        return new ArrayList<>(workplacesDbDemo.values());
    }

    public List<Workplace> findWorkplacesByName(String name) throws EntityNotFoundException{
        if(name == null || name.isEmpty()){throw new EntityNotFoundException("Workplace name cannot be empty");}
        List<Workplace> result = new ArrayList<>();
        String lowerCaseQuery = name.toLowerCase();
        for(Workplace wp : workplacesDbDemo.values()){
            if(wp.getName().toLowerCase().contains(lowerCaseQuery)){
                result.add(wp);
            }
        }
        return result;
    }

    public boolean isUserMemberOf(String email,String workplaceName)throws DAOException{
        if(email == null || workplaceName == null){throw new DAOException("Invalid parameters");}
        for(Membership m : membershipsDbDemo){
            if(m.getUser().getEmail().equals(email) && m.getWorkplace().getName().equals(workplaceName)){
                return true;
            }
        }
        return false;
    }

    public Workplace findWorkplaceByName(String name) throws EntityNotFoundException,DAOException{
        if(name == null || name.isEmpty()){throw new DAOException("Workplace name cannot be empty");}
        Workplace wp = workplacesDbDemo.get(name);
        if(wp == null){throw new EntityNotFoundException("Workplace with name: " + name + " not found");}
        return wp;
    }

    public Membership findMembership(String email,String workplaceName)throws DAOException{
        if(email == null || workplaceName == null){throw new DAOException("Invalid parameters");}
        for(Membership m : membershipsDbDemo){
            if(m.getUser().getEmail().equals(email) && m.getWorkplace().getName().equals(workplaceName)){
                return m;
            }
        }
        return null;
    }

    public List<Membership> getMembershipByUser(String email)throws DAOException{
        if(email == null || email.isEmpty()){throw new DAOException("Invalid parameters");}
        return membershipsDbDemo.stream().filter(m -> m.getUser().getEmail().equals(email)).toList();
    }

    public void updateMembership(Membership membership) throws DAOException {
        boolean found = false;
        for(int i = 0; i< membershipsDbDemo.size(); i++){
            Membership m = membershipsDbDemo.get(i);
            if(m.getUser().getEmail().equals(membership.getUser().getEmail()) && m.getWorkplace().getName().equals(membership.getWorkplace().getName())){
                membershipsDbDemo.set(i,membership);
                found = true;
                break;
            }
        }
        if(!found){throw new DAOException("Not able to update: Membership not found");}
    }

    public List<Membership> getPendingRequestsForOwner(String ownerEmail)throws DAOException{
       List<Membership> pendingRequests = new ArrayList<>();
        for (Membership m : membershipsDbDemo) {

            // Criterio 1: La richiesta deve essere ancora da accettare

                // Criterio 2: Dobbiamo verificare se il workplace di questa richiesta
                // appartiene effettivamente all'owner che sta guardando
                // (Nota: qui assumiamo che tu possa risalire all'owner del workplace)
                if (!m.isAccepted() && isOwnerOfWorkplace(ownerEmail, m.getWorkplace().getName())) {
                    pendingRequests.add(m);
                }
        }
        return pendingRequests;
    }

    private boolean isOwnerOfWorkplace(String email, String workplaceName) throws DAOException{
        if(email == null || workplaceName == null){throw new DAOException("Invalid parameters");}
        for (Membership m : membershipsDbDemo) {
            if (m.getWorkplace().getName().equals(workplaceName) &&
                    m.getUser().getEmail().equals(email) &&
                    m.getRole().equals("MANAGER")) {
                return true;
            }
        }
        return false;
    }

    public void removeMembership(Membership membership)throws DAOException{
        if(membership == null){throw new DAOException("Invalid parameters");}
        membershipsDbDemo.remove(membership);
    }

    public List<Membership> getMembershipsByWorkplace(String workplaceName)throws DAOException {
        List<Membership> filteredList = new ArrayList<>();
        if(workplaceName == null || workplaceName.isEmpty()){throw new DAOException("Invalid parameters");}
        // Accedi alla tua lista globale (es. membershipsList)
        for (Membership m : membershipsDbDemo) {
            // Confrontiamo il nome del workplace
            if (m.getWorkplace().getName().equals(workplaceName)) {
                filteredList.add(m);
            }
        }

        return filteredList;
    }

    // 1. Salva una nuova disponibilità
    public void saveAvailability(Availability availability)throws DAOException {
        if(availability == null){throw new DAOException("Invalid parameters");}
        availabilities.add(availability);
    }

    // 2. Recupera tutte le disponibilità per un determinato Workplace (per il Boss)
    public List<Availability> getAvailabilitiesByWorkplace(String workplaceName) throws DAOException {
        if(workplaceName == null || workplaceName.isEmpty()){throw new DAOException("Invalid parameters");}
        List<Availability> result = new ArrayList<>();
        for (Availability a : availabilities) {
            if (a.getWorkplaceName().equals(workplaceName)) {
                result.add(a);
            }
        }
        return result;
    }

    // 3. Recupera le disponibilità di un singolo utente in un workplace (per il Worker)
    public List<Availability> getAvailabilitiesByUser(String email, String workplaceName) throws DAOException{
        if(email == null || workplaceName == null){throw new DAOException("Invalid parameters");}
        List<Availability> result = new ArrayList<>();
        for (Availability a : availabilities) {
            if (a.getUserEmail().equals(email) && a.getWorkplaceName().equals(workplaceName)) {
                result.add(a);
            }
        }
        return result;
    }

    // 4. Elimina le vecchie disponibilità di un utente (per la pulizia prima del salvataggio)
    public void deleteAvailabilitiesByUser(String email, String workplaceName) throws DAOException {
        if(email == null || workplaceName == null){throw new DAOException("Invalid parameters");}
        availabilities.removeIf(a ->
                a.getUserEmail().equals(email) && a.getWorkplaceName().equals(workplaceName)
        );
    }

    public void updateWorkplace(Workplace updateWp,String oldName) throws DAOException, EntityNotFoundException{
        if(!workplacesDbDemo.containsKey(oldName)){throw new EntityNotFoundException("Workplace not found");}
        String newName = updateWp.getName();
        if(!newName.equals(oldName)) {
            if (workplacesDbDemo.containsKey(newName)) {
                throw new DAOException("Workplace with name: " + newName + " already exists");
            }
            for (Availability a : availabilities) {
                if (a.getWorkplaceName().equals(oldName)) {
                    a.setWorkplaceName(newName);
                }
            }
            workplacesDbDemo.remove(oldName);
        }
        workplacesDbDemo.put(newName, updateWp);
    }

    public void updateUser(UserInfo updatedUser) throws EntityNotFoundException{
        if(!usersDbDemo.containsKey(updatedUser.getEmail())){throw new EntityNotFoundException("User not found");}
        usersDbDemo.put(updatedUser.getEmail(),updatedUser);
    }

    public Map<String, List<String>> getAvailabilitiesByWeek(String workplaceName, String weekId) {
        Map<String, List<String>> weekMap = new HashMap<>();
        for(Availability a : availabilities){
            if(a.getWorkplaceName().equals(workplaceName) && a.getWeekId().equals(weekId)){
                String cellKey = a.getDay() + "_" + a.getFullShift();
                weekMap.computeIfAbsent(cellKey, k -> new ArrayList<>()).add(a.getUserEmail());
            }
        }
        return weekMap;
    }

    public String getWeekStatus(String WorkplaceName, String weekId){
        return weekStatusDbDemo.get(WorkplaceName + "_" + weekId);
    }

    public void updateWeekStatus(String workplaceName, String weekId, String newStatus) {
        // Usiamo la stessa chiave usata per il recupero
        weekStatusDbDemo.put(workplaceName + "_" + weekId, newStatus);
    }

    public void savePublishedShifts(String workplace, String weekId, Map<String, String> assignments) {
        assignments.forEach((cellKey, email) -> {
            publishedShiftsDbDemo.put(workplace + "_" + weekId + "_" + cellKey, email);
        });
    }
    @Override
    public Map<String, String> getPublishedShiftsByWeek(String workplaceName, String weekId) {
        Map<String, String> filteredAssignments = new HashMap<>();

        // Il prefisso che identifica univocamente la settimana per quel posto di lavoro
        String prefix = workplaceName + "_" + weekId + "_";

        for (Map.Entry<String, String> entry : publishedShiftsDbDemo.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(prefix)) {
                // Rimuoviamo il prefisso per restituire alla UI solo la "CellKey"
                // (es. "Mon_08:00-09:00") così la Factory la trova subito
                String cellKey = key.substring(prefix.length());
                filteredAssignments.put(cellKey, entry.getValue());
            }
        }

        return filteredAssignments;
    }
}

