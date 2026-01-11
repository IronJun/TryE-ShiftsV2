package com.ispw.tryeshifts.appcontroller;

import com.ispw.tryeshifts.AppConfig;
import com.ispw.tryeshifts.bean.SessionContext;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.dao.Repository;
import com.ispw.tryeshifts.entity.Membership;
import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.entity.Workplace;
import com.ispw.tryeshifts.excpetion.*;

public class CreateWorkplaceAC {
    public static boolean createWorkplace(WorkplaceBean wp) throws InvalidDataException, DuplicateEntityException, UserNotFoundException ,DAOException{
        if(wp.getWorkplaceName().isEmpty()){throw new InvalidDataException("Workplace name cannot be empty");}

        Repository repo = AppConfig.getRepository();
        if(repo.existsWorkplaceByName(wp.getWorkplaceName())){throw new DuplicateEntityException("This Workplace name is taken");}

        UserInfo owner;
        try{
            owner = repo.findByEmail(wp.getOwnerEmail());
        }catch(EntityNotFoundException e){
            throw new UserNotFoundException("Owner not found");
        }


        Workplace newWp = new Workplace(wp.getWorkplaceName(),wp.getAddress(),wp.getSelectedDays(),wp.getShiftsBean(),wp.getOwnerEmail());
        //newWp.setName(wp.getWorkplaceName());
        newWp.setId(java.util.UUID.randomUUID().toString());

        Membership membership = new Membership(owner,newWp,"MANAGER",true);

        repo.saveWorkplace(newWp);
        repo.saveMembership(membership);

        return true;
    }

    public void updateWorkplaceAC(WorkplaceBean wp,String oldname) throws InvalidDataException, DuplicateEntityException, UserNotFoundException, DAOException, EntityNotFoundException {
        var repo = AppConfig.getRepository();
        Workplace workplace = repo.findWorkplaceByName(oldname);
        if(workplace == null) throw new EntityNotFoundException("Workplace not found");

        workplace.setName(wp.getWorkplaceName());
        workplace.setAddress(wp.getAddress());
        workplace.setSelectedDays(wp.getSelectedDays());
        workplace.setShifts(wp.getShiftsBean());

        repo.updateWorkplace(workplace, oldname);
//        if (!wp.getWorkplaceName().equals(oldname)) {
//            // Opzionale: implementa un metodo nel DAO che rinomina il workplaceName
//            // in tutte le Availability associate a oldName.
//            repo.renameWorkplaceInAvailabilities(oldName, updatedBean.getWorkplaceName());
//        }
        SessionContext.getInstance().setLoggedWorkplace(wp);
    }
}
