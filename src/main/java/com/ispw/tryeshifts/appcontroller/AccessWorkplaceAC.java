package com.ispw.tryeshifts.appcontroller;

import com.ispw.tryeshifts.config.AppConfig;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.dao.WorkplaceDAO;
import com.ispw.tryeshifts.dao.MembershipDAO;
import com.ispw.tryeshifts.entity.Membership;
import com.ispw.tryeshifts.entity.Workplace;
import com.ispw.tryeshifts.exception.*;


public class AccessWorkplaceAC {
    private final MembershipDAO membershipDB;
    private final WorkplaceDAO workplaceDB;

    public AccessWorkplaceAC(MembershipDAO membershipDB, WorkplaceDAO workplaceDB) {
        this.membershipDB = membershipDB;
        this.workplaceDB = workplaceDB;
    }
    public  AccessWorkplaceAC(){
        this(AppConfig.getInstance().getMembershipRepository(), AppConfig.getInstance().getWorkplaceRepository());
    }
    public  WorkplaceBean canAccess(UserBean user, String workplaceName) throws BaseException {
        if(user == null){throw new IllegalArgumentException("User not logged in");}

        Membership membership = membershipDB.findMembership(user.getEmail(),workplaceName);

        if(membership == null){throw new UserNotMemberException("You are not member of this workplace");}
        if(!membership.isAccepted()){throw new MembershipPendingException("membership", workplaceName);}
        Workplace entity = workplaceDB.findWorkplaceByName(workplaceName);
        return new WorkplaceBean(entity.getName(),entity.getAddress(),entity.getSelectedDays(),entity.getShifts(),entity.getOwnerEmail());
    }
}
