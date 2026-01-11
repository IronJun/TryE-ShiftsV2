package com.ispw.tryeshifts.appcontroller;

import com.ispw.tryeshifts.AppConfig;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.entity.Membership;
import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.entity.Workplace;
import com.ispw.tryeshifts.excpetion.DAOException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class ManageMembersAC {
    public List<UserBean> getActiveMembers(String wpName) throws EntityNotFoundException, DAOException{
        var repo = AppConfig.getRepository();

        if(repo.findWorkplaceByName(wpName) == null){
            throw new EntityNotFoundException("Workplace: "+wpName+" not found");
        }

        List<Membership> memberships = repo.getMembershipsByWorkplace(wpName);
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
        var repo = AppConfig.getRepository();
        Membership m = repo.findMembership(userEmail,workplaceName);

        if(m == null){
            throw new EntityNotFoundException("Join request not found for user: "+userEmail+" in workplace: "+workplaceName+"");
        }
            if (accept) {
                m.setAccepted(true);
                repo.updateMembership(m);
            } else {
                // Se rifiuta, eliminiamo semplicemente la richiesta/membership
                repo.removeMembership(m);
            }
    }

    public void requestJoin(UserBean userBean, String workplaceName) throws DAOException, EntityNotFoundException {
        var repo = AppConfig.getRepository();
        UserInfo user = repo.findByEmail(userBean.getEmail());
        Workplace wp = repo.findWorkplaceByName(workplaceName);

        if(repo.isUserMemberOf(userBean.getEmail(),workplaceName)){
            throw new DAOException("You already have a pendant request");
        }

        Membership request = new Membership(user,wp,"Worker",false);
        repo.saveMembership(request);

    }

    public List<UserBean> getPendingRequests(String WorkplaceName) throws DAOException,EntityNotFoundException{
        var repo = AppConfig.getRepository();

        if(repo.findWorkplaceByName(WorkplaceName) == null){
            throw new EntityNotFoundException("Workplace: "+WorkplaceName+" not found");
        }

        List<Membership> allMembers = repo.getMembershipsByWorkplace(WorkplaceName);
        List<UserBean> beans = new ArrayList<>();

        for (Membership m : allMembers) {
            if(!m.isAccepted()){
                UserBean ub = new UserBean();
                ub.setEmail(m.getUser().getEmail());
                beans.add(ub);
            }
//            UserBean bean = new WorkplaceBean();
//            bean.setWorkplaceName(m.getWorkplace().getName());
//            // Riutilizziamo il campo ownerEmail per metterci l'email di chi fa la richiesta
//            bean.setOwnerEmail(m.getUser().getEmail());
//            beans.add(bean);
        }
        return beans;
    }

}
