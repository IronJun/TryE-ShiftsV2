package com.ispw.tryeshifts.appController;

import com.ispw.tryeshifts.AppConfig;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.dao.Repository;
import com.ispw.tryeshifts.entity.Membership;
import com.ispw.tryeshifts.entity.Workplace;
import com.ispw.tryeshifts.excpetion.DAOException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;
import com.ispw.tryeshifts.excpetion.MembershipPendingException;
import com.ispw.tryeshifts.excpetion.UserNotMemberException;

import java.util.logging.Logger;

public class AccessWorkplaceAC {
    private static final Logger LOGGER = Logger.getLogger(AccessWorkplaceAC.class.getName());

    private AccessWorkplaceAC(){
        throw new IllegalStateException("Utility class");
    }
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
