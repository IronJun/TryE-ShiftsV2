package com.ispw.tryeshifts.dao;

import com.ispw.tryeshifts.entity.Membership;
import com.ispw.tryeshifts.excpetion.DAOException;

import java.util.ArrayList;
import java.util.List;

public class MembershipDAODemo implements MembershipDAO{
    private final InMemory db = InMemory.getInstance();

    public void saveMembership(Membership m)throws DAOException {
        if(m != null) {
            db.getMemberships().add(m);
        }else{
            throw new DAOException("No membership passed");
        }
    }
    public void updateMembership(Membership membership) throws DAOException {
        boolean found = false;
        for(int i = 0; i< db.getMemberships().size(); i++){
            Membership m = db.getMemberships().get(i);
            if(m.getUser().getEmail().equals(membership.getUser().getEmail()) && m.getWorkplace().getName().equals(membership.getWorkplace().getName())){
                db.getMemberships().set(i,membership);
                found = true;
                break;
            }
        }
        if(!found){throw new DAOException("Not able to update: Membership not found");}
    }
    public void removeMembership(Membership membership)throws DAOException{
        if(membership == null){throw new DAOException("Invalid parameters");}
        db.getMemberships().remove(membership);
    }
    public Membership findMembership(String email,String workplaceName)throws DAOException{
        if(email == null || workplaceName == null){throw new DAOException("Invalid parameters");}
        for(Membership m : db.getMemberships()){
            if(m.getUser().getEmail().equals(email) && m.getWorkplace().getName().equals(workplaceName)){
                return m;
            }
        }
        return null;
    }
    public List<Membership> getMembershipsByWorkplace(String workplaceName)throws DAOException {
        List<Membership> filteredList = new ArrayList<>();
        if(workplaceName == null || workplaceName.isEmpty()){throw new DAOException("Invalid parameters");}
        // Accedi alla tua lista globale (es. membershipsList)
        for (Membership m : db.getMemberships()) {
            // Confrontiamo il nome del workplace
            if (m.getWorkplace().getName().equals(workplaceName)) {
                filteredList.add(m);
            }
        }

        return filteredList;
    }
    public List<Membership> getPendingRequestsForOwner(String ownerEmail)throws DAOException{
        List<Membership> pendingRequests = new ArrayList<>();
        for (Membership m : db.getMemberships()) {

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
    public List<Membership> getMembershipByUser(String email)throws DAOException{
        if(email == null || email.isEmpty()){throw new DAOException("Invalid parameters");}
        return db.getMemberships().stream().filter(m -> m.getUser().getEmail().equals(email)).toList();
    }
    public boolean isUserMemberOf(String email,String workplaceName)throws DAOException{
        if(email == null || workplaceName == null){throw new DAOException("Invalid parameters");}
        for(Membership m : db.getMemberships()){
            if(m.getUser().getEmail().equals(email) && m.getWorkplace().getName().equals(workplaceName)){
                return true;
            }
        }
        return false;
    }

    private boolean isOwnerOfWorkplace(String email, String workplaceName) throws DAOException{
        if(email == null || workplaceName == null){throw new DAOException("Invalid parameters");}
        for (Membership m : db.getMemberships()) {
            if (m.getWorkplace().getName().equals(workplaceName) &&
                    m.getUser().getEmail().equals(email) &&
                    m.getRole().equals("MANAGER")) {
                return true;
            }
        }
        return false;
    }

}
