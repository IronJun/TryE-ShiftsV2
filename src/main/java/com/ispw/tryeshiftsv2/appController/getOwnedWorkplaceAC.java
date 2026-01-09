package com.ispw.tryeshiftsv2.appController;

import com.ispw.tryeshiftsv2.AppConfig;
import com.ispw.tryeshiftsv2.bean.UserBean;
import com.ispw.tryeshiftsv2.bean.WorkplaceBean;
import com.ispw.tryeshiftsv2.dao.Repository;
import com.ispw.tryeshiftsv2.entity.Membership;
import com.ispw.tryeshiftsv2.entity.Workplace;
import com.ispw.tryeshiftsv2.excpetion.DAOException;


import java.util.ArrayList;
import java.util.List;

public class getOwnedWorkplaceAC {

    public static List<WorkplaceBean> getForUser(UserBean user) throws DAOException{
        if(user == null || user.getEmail() == null){
            throw new DAOException("User not found");
        }
        Repository repo = AppConfig.getRepository();
        List<Workplace> entities = repo.findWorkplacesbyEmail(user.getEmail());
        List<WorkplaceBean> result = new ArrayList<>();

        for(Workplace wp : entities){
            WorkplaceBean bean = new WorkplaceBean();
            bean.setWorkplaceName(wp.getName());
            bean.setAddress(wp.getAddress());
            result.add(bean);
        }
        return result;
    }

}
