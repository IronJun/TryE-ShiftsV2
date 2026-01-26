package com.ispw.tryeshifts.dao;

import com.ispw.tryeshifts.entity.Membership;
import com.ispw.tryeshifts.excpetion.DuplicateEntityException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class MembershipDAODemo implements MembershipDAO{
    private final InMemory db = InMemory.getInstance();

    public void saveMembership(Membership m)throws DuplicateEntityException {
        if(m == null) {
            throw new NullPointerException("Invalid parameters");
        }else{
            for(Membership mem : db.getMemberships()){
                if(mem.getUser().getEmail().equals(m.getUser().getEmail()) && mem.getWorkplace().getName().equals(m.getWorkplace().getName())){
                    throw new DuplicateEntityException("Membership", m.getUser().getEmail()+ "in" + m.getWorkplace().getName());
                }
            }
            db.getMemberships().add(m);
        }
    }
    public void updateMembership(Membership membership) throws EntityNotFoundException {
        boolean found = false;
        for(int i = 0; i< db.getMemberships().size(); i++){
            Membership m = db.getMemberships().get(i);
            if(m.getUser().getEmail().equals(membership.getUser().getEmail()) && m.getWorkplace().getName().equals(membership.getWorkplace().getName())){
                db.getMemberships().set(i,membership);
                found = true;
                break;
            }
        }
        if(!found){throw new EntityNotFoundException("Membership", membership.getUser().getEmail());}
    }
    public void removeMembership(Membership membership)throws EntityNotFoundException{
        if(membership == null){throw new NullPointerException("Invalid parameters");}
        boolean removed = db.getMemberships().removeIf(m ->
                m.getUser().getEmail().equals(membership.getUser().getEmail()) && m.getWorkplace().getName().equals(membership.getWorkplace().getName())
        );
        if (!removed) {
            throw new EntityNotFoundException("Membership", membership.getUser().getEmail());
        }
    }
    public Membership findMembership(String email,String workplaceName){
        if(email == null || workplaceName == null){throw new NullPointerException("Invalid parameters");}
        for(Membership m : db.getMemberships()){
            if(m.getUser().getEmail().equals(email) && m.getWorkplace().getName().equals(workplaceName)){
                return m;
            }
        }
        return null;
    }
    public List<Membership> getMembershipsByWorkplace(String workplaceName) {
        List<Membership> filteredList = new ArrayList<>();
        if(workplaceName == null || workplaceName.isEmpty()){throw new NullPointerException("Invalid parameters");}
        // Accedi alla tua lista globale (es. membershipsList)
        for (Membership m : db.getMemberships()) {
            // Confrontiamo il nome del workplace
            if (m.getWorkplace().getName().equals(workplaceName)) {
                filteredList.add(m);
            }
        }

        return filteredList;
    }
    public boolean isUserMemberOf(String email,String workplaceName){
        if(email == null || workplaceName == null){throw new NullPointerException("Invalid parameters");}
        for(Membership m : db.getMemberships()){
            if(m.getUser().getEmail().equals(email) && m.getWorkplace().getName().equals(workplaceName)){
                return true;
            }
        }
        return false;
    }
}
