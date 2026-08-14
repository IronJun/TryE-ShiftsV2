package com.ispw.tryeshifts.appcontroller;

import com.ispw.tryeshifts.config.AppConfig;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.dao.WorkplaceDAO;
import com.ispw.tryeshifts.dao.MembershipDAO;
import com.ispw.tryeshifts.entity.Membership;
import com.ispw.tryeshifts.entity.Workplace;
import com.ispw.tryeshifts.excpetion.*;


public class AccessWorkplaceAC {

    private AccessWorkplaceAC(){
        throw new IllegalStateException("Utility class");
    }
    public static WorkplaceBean canAccess(UserBean user, String workplaceName) throws BaseException {
        MembershipDAO membershipDB = AppConfig.getMembershipRepository();
        WorkplaceDAO workplaceDB = AppConfig.getWorkplaceRepository();

        if(user == null){throw new BaseException("User not logged in");}

        Membership membership = membershipDB.findMembership(user.getEmail(),workplaceName);

        if(membership == null){throw new UserNotMemberException("Non sei ancora membro di questo workplace.");}
        if(!membership.isAccepted()){throw new MembershipPendingException("membership", workplaceName);}
        Workplace entity = workplaceDB.findWorkplaceByName(workplaceName);
        return new WorkplaceBean(entity.getName(),entity.getAddress(),entity.getSelectedDays(),entity.getShifts(),entity.getOwnerEmail());
    }
}
