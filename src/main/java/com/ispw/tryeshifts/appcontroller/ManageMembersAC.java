package com.ispw.tryeshifts.appcontroller;

import com.ispw.tryeshifts.AppConfig;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.dao.UserDAO;
import com.ispw.tryeshifts.dao.WorkplaceDAO;
import com.ispw.tryeshifts.dao.MembershipDAO;
import com.ispw.tryeshifts.entity.Membership;
import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.entity.Workplace;
import com.ispw.tryeshifts.excpetion.BaseException;

import com.ispw.tryeshifts.excpetion.EntityNotFoundException;
import com.ispw.tryeshifts.excpetion.ValidationException;

import java.util.ArrayList;
import java.util.List;

public class ManageMembersAC {
    private static final WorkplaceDAO workplaceRepo = AppConfig.getWorkplaceRepository();
    private static final UserDAO userRepo = AppConfig.getUserRepository();
    private static final MembershipDAO membershipRepo = AppConfig.getMembershipRepository();

    public List<UserBean> getActiveMembers(String wpName) throws BaseException{
        List<Membership> memberships = membershipRepo.getMembershipsByWorkplace(wpName);
        List<UserBean> active = new ArrayList<>();
        for (Membership m : memberships) {
            if (m.isAccepted()) {
                UserBean ub = new UserBean();
                ub.setEmail(m.getUser().getEmail());
                ub.setName(m.getUser().getName());
                ub.setSurname(m.getUser().getSurname());
                ub.setRole(m.getRole()); // Assicurati di avere setRole nel tuo UserBean
                active.add(ub);
            }
        }
        return active;
    }

    public void acceptWorker(String userEmail,String workplaceName,boolean accept)throws BaseException{
        Membership m = membershipRepo.findMembership(userEmail,workplaceName);

        if(m == null){
            throw new EntityNotFoundException("membership",userEmail+" is not in "+workplaceName);
        }
        if(m.isAccepted() && accept){
            throw new ValidationException("Questa richiesta è già stata accettata", "Status");
        }
            if (accept) {
                m.setAccepted(true);
                membershipRepo.updateMembership(m);
            } else {
                // Se rifiuta, eliminiamo semplicemente la richiesta/membership
                membershipRepo.removeMembership(m);
            }
    }

    public void requestJoin(UserBean userBean, String workplaceName) throws BaseException {
        UserInfo user = userRepo.findByEmail(userBean.getEmail());
        Workplace wp = workplaceRepo.findWorkplaceByName(workplaceName);

        if(user == null){throw new EntityNotFoundException("User",userBean.getEmail());}

        if(membershipRepo.isUserMemberOf(userBean.getEmail(),workplaceName)){
            throw new ValidationException("You already have a pendant request","membership");
        }

        Membership request = new Membership(user,wp,"Worker",false);
        membershipRepo.saveMembership(request);

    }

    public List<UserBean> getPendingRequests(String workplaceName) throws BaseException{
        List<Membership> allMembers = membershipRepo.getMembershipsByWorkplace(workplaceName);
        List<UserBean> beans = new ArrayList<>();

        for (Membership m : allMembers) {
            if(!m.isAccepted()){
                UserBean ub = new UserBean();
                ub.setEmail(m.getUser().getEmail());
                ub.setName(m.getUser().getName());
                ub.setSurname(m.getUser().getSurname());
                beans.add(ub);
            }

        }
        return beans;
    }

}
