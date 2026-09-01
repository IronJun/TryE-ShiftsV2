package com.ispw.tryeshifts.appcontroller;

import com.ispw.tryeshifts.appcontroller.utils.NotificationDispatcher;
import com.ispw.tryeshifts.bean.NotificationBean;
import com.ispw.tryeshifts.config.AppConfig;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.dao.UserDAO;
import com.ispw.tryeshifts.dao.WorkplaceDAO;
import com.ispw.tryeshifts.dao.MembershipDAO;
import com.ispw.tryeshifts.entity.Membership;
import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.entity.Workplace;
import com.ispw.tryeshifts.exception.BaseException;

import com.ispw.tryeshifts.exception.EntityNotFoundException;
import com.ispw.tryeshifts.exception.ValidationException;

import java.util.ArrayList;
import java.util.List;

public class ManageMembersAC {
    private final WorkplaceDAO workplaceRepo;
    private final UserDAO userRepo;
    private final MembershipDAO membershipRepo;

    //Constructors
    public ManageMembersAC(WorkplaceDAO workplaceRepo, UserDAO userRepo, MembershipDAO membershipRepo) {
        this.workplaceRepo = workplaceRepo;
        this.userRepo = userRepo;
        this.membershipRepo = membershipRepo;
    }

    public ManageMembersAC(){
        this(AppConfig.getInstance().getWorkplaceRepository(), AppConfig.getInstance().getUserRepository(), AppConfig.getInstance().getMembershipRepository());
    }

    //usefully methods
    public List<UserBean> getActiveMembers(String wpName) throws BaseException{
        List<Membership> memberships = membershipRepo.getMembershipsByWorkplace(wpName);
        List<UserBean> active = new ArrayList<>();
        for (Membership m : memberships) {
            if (m.isAccepted()) {
                UserBean ub = toBeanFromMem(m);
                ub.setRole(m.getRole());
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
            throw new ValidationException("This request has been already accepted", "Status");
        }
            if (accept) {
                m.setAccepted(true);
                membershipRepo.updateMembership(m);
                NotificationDispatcher nf = new NotificationDispatcher();
                NotificationBean notifBean = new NotificationBean(m.getUser().getEmail(),"You have been accepted to : "+workplaceName, "ACCEPTED");
                nf.sendUserNotif(notifBean);
            } else {
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
                beans.add(toBeanFromMem(m));
            }

        }
        return beans;
    }

    private UserBean toBeanFromMem(Membership m){
        return new UserBean(m.getUser().getEmail(),m.getUser().getName(),m.getUser().getSurname());
    }
}
