package com.ispw.tryeshiftsv2.dao;

import com.ispw.tryeshiftsv2.bean.WorkplaceBean;
import com.ispw.tryeshiftsv2.entity.Availability;
import com.ispw.tryeshiftsv2.entity.Membership;
import com.ispw.tryeshiftsv2.entity.UserInfo;
import com.ispw.tryeshiftsv2.entity.Workplace;
import com.ispw.tryeshiftsv2.excpetion.DAOException;
import com.ispw.tryeshiftsv2.excpetion.EntityNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemory implements Repository {
    private static final Map<String, UserInfo> usersDbDemo = new HashMap<>();
    private static final Map<String, Workplace> workplacesDbDemo = new HashMap<>();
    private static final List<Membership> membershipsDbDemo = new ArrayList<>();
    private static final List<Availability> availabilities = new ArrayList<>();


    public void save(UserInfo user) throws DAOException{
        usersDbDemo.put(user.getEmail(),user);
    }


    public UserInfo findByEmail(String email) throws EntityNotFoundException,DAOException{
        UserInfo user = usersDbDemo.get(email);
        if(user == null){
            throw new EntityNotFoundException("User with email: " + email + " not found");
        }
        return user;
    }

    public List<Membership> getMemberships(String email) throws DAOException{
        return membershipsDbDemo.stream().filter(m -> m.getUser().getEmail().equals(email)).toList();
    }

    public void saveWorkplace(Workplace wp) throws DAOException{
        workplacesDbDemo.put(wp.getName(),wp);
    }

    public void saveMembership(Membership m)throws DAOException{
        membershipsDbDemo.add(m);
    }

    public boolean existsWorkplaceByName(String name) throws DAOException{
        // Essendo una mappa con il nome come chiave, il controllo è istantaneo
        return workplacesDbDemo.containsKey(name);
    }

    public List<Workplace> findWorkplacesbyEmail(String email) throws DAOException{
        try{
            return membershipsDbDemo.stream().filter(m -> m.getUser().getEmail().equals(email)).map(Membership::getWorkplace).toList();
        }catch(Exception e){
            throw new DAOException("Cannot resolve the Workplaces for this User");
        }
    }

    public List<Workplace> findAllWorkplaces()throws DAOException{
        return new ArrayList<>(workplacesDbDemo.values());
    }

    public List<Workplace> findWorkplacesByName(String name) throws EntityNotFoundException,DAOException{
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
        for(Membership m : membershipsDbDemo){
            if(m.getUser().getEmail().equals(email) && m.getWorkplace().getName().equals(workplaceName)){
                return true;
            }
        }
        return false;
    }

    public Workplace findWorkplaceByName(String name) throws EntityNotFoundException,DAOException{
        Workplace wp = workplacesDbDemo.get(name);
        if(wp == null){throw new EntityNotFoundException("Workplace with name: " + name + " not found");}
        return wp;
    }

    public Membership findMembership(String email,String workplaceName)throws DAOException{
        for(Membership m : membershipsDbDemo){
            if(m.getUser().getEmail().equals(email) && m.getWorkplace().getName().equals(workplaceName)){
                return m;
            }
        }
        return null;
    }

    public List<Membership> getMembershipByUser(String email)throws DAOException{
        return membershipsDbDemo.stream().filter(m -> m.getUser().getEmail().equals(email)).toList();
    }

    public void updateMembership(Membership membership) throws DAOException {
        boolean found = false;
        for(int i = 0; i<this.membershipsDbDemo.size();i++){
            Membership m = this.membershipsDbDemo.get(i);
            if(m.getUser().getEmail().equals(membership.getUser().getEmail()) && m.getWorkplace().getName().equals(membership.getWorkplace().getName())){
                this.membershipsDbDemo.set(i,membership);
                found = true;
                break;
            }
        }
        if(!found){throw new DAOException("Not able to update: Membership not found");}
    }

    public List<Membership> getPendingRequestsForOwner(String ownerEmail)throws DAOException{
       List<Membership> pendingRequests = new ArrayList<>();
        for (Membership m : this.membershipsDbDemo) {

            // Criterio 1: La richiesta deve essere ancora da accettare
            if (!m.isAccepted()) {

                // Criterio 2: Dobbiamo verificare se il workplace di questa richiesta
                // appartiene effettivamente all'owner che sta guardando
                // (Nota: qui assumiamo che tu possa risalire all'owner del workplace)
                if (isOwnerOfWorkplace(ownerEmail, m.getWorkplace().getName())) {
                    pendingRequests.add(m);
                }
            }
        }
        return pendingRequests;
    }

    private boolean isOwnerOfWorkplace(String email, String workplaceName) throws DAOException{
        for (Membership m : this.membershipsDbDemo) {
            if (m.getWorkplace().getName().equals(workplaceName) &&
                    m.getUser().getEmail().equals(email) &&
                    m.getRole().equals("MANAGER")) {
                return true;
            }
        }
        return false;
    }

    public void removeMembership(Membership membership)throws DAOException{
        this.membershipsDbDemo.remove(membership);
    }

    public List<Membership> getMembershipsByWorkplace(String workplaceName)throws DAOException {
        List<Membership> filteredList = new ArrayList<>();

        // Accedi alla tua lista globale (es. membershipsList)
        for (Membership m : this.membershipsDbDemo) {
            // Confrontiamo il nome del workplace
            if (m.getWorkplace().getName().equals(workplaceName)) {
                filteredList.add(m);
            }
        }

        return filteredList;
    }

    // 1. Salva una nuova disponibilità
    public void saveAvailability(Availability availability)throws DAOException {
        this.availabilities.add(availability);
    }

    // 2. Recupera tutte le disponibilità per un determinato Workplace (per il Boss)
    public List<Availability> getAvailabilitiesByWorkplace(String workplaceName) throws DAOException {
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
        // Usiamo removeIf che è molto più pulito e veloce
        this.availabilities.removeIf(a ->
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
            for (Availability a : this.availabilities) {
                if (a.getWorkplaceName().equals(oldName)) {
                    a.setWorkplaceName(newName);
                }
            }
            workplacesDbDemo.remove(oldName);
            System.out.println("Repository: Rimosso vecchio riferimento [" + oldName + "]");
        }
        workplacesDbDemo.put(newName, updateWp);
        System.out.println("Repository: Aggiornato workplace [" + newName + "] con successo.");
    }

    public void updateUser(UserInfo updatedUser) throws EntityNotFoundException,DAOException{
        if(!usersDbDemo.containsKey(updatedUser.getEmail())){throw new EntityNotFoundException("User not found");}
        usersDbDemo.put(updatedUser.getEmail(),updatedUser);
        System.out.println("Repository: Aggiornato utente [" + updatedUser.getEmail() + "] con successo.");
    }
}

