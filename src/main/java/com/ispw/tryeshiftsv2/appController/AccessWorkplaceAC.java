package com.ispw.tryeshiftsv2.appController;

import com.ispw.tryeshiftsv2.AppConfig;
import com.ispw.tryeshiftsv2.bean.UserBean;
import com.ispw.tryeshiftsv2.bean.WorkplaceBean;
import com.ispw.tryeshiftsv2.dao.Repository;
import com.ispw.tryeshiftsv2.entity.Membership;
import com.ispw.tryeshiftsv2.entity.Workplace;
import com.ispw.tryeshiftsv2.excpetion.DAOException;
import com.ispw.tryeshiftsv2.excpetion.EntityNotFoundException;
import com.ispw.tryeshiftsv2.excpetion.MembershipPendingException;
import com.ispw.tryeshiftsv2.excpetion.UserNotMemberException;

public class AccessWorkplaceAC {
    public static WorkplaceBean canAccess(UserBean user, String workplaceName) throws UserNotMemberException, MembershipPendingException, EntityNotFoundException, DAOException {
        Repository repo = AppConfig.getRepository();
        Membership membership = repo.findMembership(user.getEmail(),workplaceName);
        if(membership == null){throw new UserNotMemberException("you are not member of this workplace");}
        if(!membership.isAccepted()){throw new MembershipPendingException("your membership is pending");}

        Workplace entity = repo.findWorkplaceByName(workplaceName);
        if(entity == null){throw new EntityNotFoundException("Workplace not found");}

        return new WorkplaceBean(entity.getName(),entity.getAddress(),entity.getSelectedDays(),entity.getShifts(),entity.getOwnerEmail());
    }
}
