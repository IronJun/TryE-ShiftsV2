package com.ispw.tryeshifts.appcontroller;

import com.ispw.tryeshifts.AppConfig;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.dao.UserDAO;
import com.ispw.tryeshifts.dao.WorkplaceDAO;
import com.ispw.tryeshifts.dao.AvailabilityDAO;
import com.ispw.tryeshifts.dao.MembershipDAO;

import com.ispw.tryeshifts.entity.Membership;
import com.ispw.tryeshifts.entity.Workplace;
import com.ispw.tryeshifts.excpetion.DAOException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;
import com.ispw.tryeshifts.excpetion.MembershipPendingException;
import com.ispw.tryeshifts.excpetion.UserNotMemberException;


import java.util.logging.Logger;

public class AccessWorkplaceAC {

    private AccessWorkplaceAC(){
        throw new IllegalStateException("Utility class");
    }
    public static WorkplaceBean canAccess(UserBean user, String workplaceName) throws UserNotMemberException, MembershipPendingException, EntityNotFoundException, DAOException {
        MembershipDAO membershipDB = AppConfig.getMembershipRepository();
        WorkplaceDAO workplaceDB = AppConfig.getWorkplaceRepository();

        Membership membership = membershipDB.findMembership(user.getEmail(),workplaceName);
        if(membership == null){throw new UserNotMemberException("you are not member of this workplace");}
        if(!membership.isAccepted()){throw new MembershipPendingException("your membership is pending");}

        Workplace entity = workplaceDB.findWorkplaceByName(workplaceName);
        if(entity == null){throw new EntityNotFoundException("Workplace not found");}

        return new WorkplaceBean(entity.getName(),entity.getAddress(),entity.getSelectedDays(),entity.getShifts(),entity.getOwnerEmail());
    }
}
