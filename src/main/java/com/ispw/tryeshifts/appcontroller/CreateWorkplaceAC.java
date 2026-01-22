package com.ispw.tryeshifts.appcontroller;

import com.ispw.tryeshifts.AppConfig;
import com.ispw.tryeshifts.bean.SessionContext;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.dao.UserDAO;
import com.ispw.tryeshifts.dao.WorkplaceDAO;
import com.ispw.tryeshifts.dao.AvailabilityDAO;
import com.ispw.tryeshifts.dao.MembershipDAO;
import com.ispw.tryeshifts.entity.Membership;
import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.entity.Workplace;
import com.ispw.tryeshifts.excpetion.*;

public class CreateWorkplaceAC {
    private static final WorkplaceDAO workplaceRepo = AppConfig.getWorkplaceRepository();
    private static final UserDAO userRepo = AppConfig.getUserRepository();
    private static final MembershipDAO membershipRepo = AppConfig.getMembershipRepository();

    public static void createWorkplace(WorkplaceBean wp) throws InvalidDataException, DuplicateEntityException, UserNotFoundException ,DAOException{
        if(wp.getWorkplaceName().isEmpty()){throw new InvalidDataException("Workplace name cannot be empty");}

//        WorkplaceDAO workplaceRepo = AppConfig.getWorkplaceRepository();
//        UserDAO userRepo = AppConfig.getUserRepository();
//        MembershipDAO membershipRepo = AppConfig.getMembershipRepository();

        if(workplaceRepo.existsWorkplaceByName(wp.getWorkplaceName())){throw new DuplicateEntityException("This Workplace name is taken");}

        UserInfo owner;
        try{
            owner = userRepo.findByEmail(wp.getOwnerEmail());
        }catch(EntityNotFoundException _){
            throw new UserNotFoundException("Owner not found");
        }


        Workplace newWp = new Workplace(wp.getWorkplaceName(),wp.getAddress(),wp.getSelectedDays(),wp.getShiftsBean(),wp.getOwnerEmail());
        newWp.setId(java.util.UUID.randomUUID().toString());

        Membership membership = new Membership(owner,newWp,"MANAGER",true);

        workplaceRepo.saveWorkplace(newWp);
        membershipRepo.saveMembership(membership);
    }

    public void updateWorkplaceAC(WorkplaceBean wp,String oldname) throws DuplicateEntityException, UserNotFoundException, DAOException, EntityNotFoundException {
        Workplace workplace = workplaceRepo.findWorkplaceByName(oldname);
        if(workplace == null) throw new EntityNotFoundException("Workplace not found");

        workplace.setName(wp.getWorkplaceName());
        workplace.setAddress(wp.getAddress());
        workplace.setSelectedDays(wp.getSelectedDays());
        workplace.setShifts(wp.getShiftsBean());

        workplaceRepo.updateWorkplace(workplace, oldname);

        SessionContext.getInstance().setLoggedWorkplace(wp);
    }
}
