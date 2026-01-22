package com.ispw.tryeshifts.appcontroller;

import com.ispw.tryeshifts.AppConfig;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.dao.UserDAO;
import com.ispw.tryeshifts.dao.WorkplaceDAO;
import com.ispw.tryeshifts.dao.AvailabilityDAO;
import com.ispw.tryeshifts.dao.MembershipDAO;
import com.ispw.tryeshifts.entity.Membership;
import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.entity.Workplace;
import com.ispw.tryeshifts.excpetion.DAOException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class ManageMembersAC {
    private static final WorkplaceDAO workplaceRepo = AppConfig.getWorkplaceRepository();
    private static final UserDAO userRepo = AppConfig.getUserRepository();
    private static final MembershipDAO membershipRepo = AppConfig.getMembershipRepository();

    public List<UserBean> getActiveMembers(String wpName) throws EntityNotFoundException, DAOException{

        if(workplaceRepo.findWorkplaceByName(wpName) == null){
            throw new EntityNotFoundException("Workplace: "+wpName+" not found");
        }

        List<Membership> memberships = membershipRepo.getMembershipsByWorkplace(wpName);
        List<UserBean> active = new ArrayList<>();

        for (Membership m : memberships) {
            if (m.isAccepted()) {
                UserBean ub = new UserBean();
                ub.setEmail(m.getUser().getEmail());
                ub.setRole(m.getRole()); // Assicurati di avere setRole nel tuo UserBean
                active.add(ub);
            }
        }
        return active;
    }

    public void acceptWorker(String userEmail,String workplaceName,boolean accept)throws DAOException, EntityNotFoundException{
        Membership m = membershipRepo.findMembership(userEmail,workplaceName);

        if(m == null){
            throw new EntityNotFoundException("Join request not found for user: "+userEmail+" in workplace: "+workplaceName);
        }
            if (accept) {
                m.setAccepted(true);
                membershipRepo.updateMembership(m);
            } else {
                // Se rifiuta, eliminiamo semplicemente la richiesta/membership
                membershipRepo.removeMembership(m);
            }
    }

    public void requestJoin(UserBean userBean, String workplaceName) throws DAOException, EntityNotFoundException {
        UserInfo user = userRepo.findByEmail(userBean.getEmail());
        Workplace wp = workplaceRepo.findWorkplaceByName(workplaceName);

        if(membershipRepo.isUserMemberOf(userBean.getEmail(),workplaceName)){
            throw new DAOException("You already have a pendant request");
        }

        Membership request = new Membership(user,wp,"Worker",false);
        membershipRepo.saveMembership(request);

    }

    public List<UserBean> getPendingRequests(String workplaceName) throws DAOException,EntityNotFoundException{

        if(workplaceRepo.findWorkplaceByName(workplaceName) == null){
            throw new EntityNotFoundException("Workplace: "+workplaceName+" not found");
        }

        List<Membership> allMembers = membershipRepo.getMembershipsByWorkplace(workplaceName);
        List<UserBean> beans = new ArrayList<>();

        for (Membership m : allMembers) {
            if(!m.isAccepted()){
                UserBean ub = new UserBean();
                ub.setEmail(m.getUser().getEmail());
                beans.add(ub);
            }

        }
        return beans;
    }

}
