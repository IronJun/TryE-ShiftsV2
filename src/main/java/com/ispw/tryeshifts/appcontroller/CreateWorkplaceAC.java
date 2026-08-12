package com.ispw.tryeshifts.appcontroller;

import com.ispw.tryeshifts.config.AppConfig;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.dao.UserDAO;
import com.ispw.tryeshifts.dao.WorkplaceDAO;
import com.ispw.tryeshifts.dao.MembershipDAO;
import com.ispw.tryeshifts.entity.Membership;
import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.entity.Workplace;
import com.ispw.tryeshifts.excpetion.*;

public class CreateWorkplaceAC {
    private static final WorkplaceDAO workplaceRepo = AppConfig.getWorkplaceRepository();
    private static final UserDAO userRepo = AppConfig.getUserRepository();
    private static final MembershipDAO membershipRepo = AppConfig.getMembershipRepository();

    private CreateWorkplaceAC() {
        //utility class should not be istantiated
    }
    public static void createWorkplace(WorkplaceBean wp) throws BaseException {
        if(wp.getWorkplaceName().isEmpty()){throw new NullPointerException("Workplace name cannot be empty");}


        if(workplaceRepo.existsWorkplaceByName(wp.getWorkplaceName())){throw new DuplicateEntityException("Workplace",wp.getWorkplaceName());}

        UserInfo owner= userRepo.findByEmail(wp.getOwnerEmail());

        if(owner == null){
            throw new NullPointerException("Owner for "+wp.getWorkplaceName()+" not found");
        }
        Workplace newWp = new Workplace(wp.getWorkplaceName(),wp.getAddress(),wp.getSelectedDays(),wp.getShiftsBean(),wp.getOwnerEmail());
        newWp.setId(java.util.UUID.randomUUID().toString());
        Membership membership = new Membership(owner,newWp,"MANAGER",true);
        workplaceRepo.saveWorkplace(newWp);
        membershipRepo.saveMembership(membership);
    }


}
