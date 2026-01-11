package com.ispw.tryeshifts.appcontroller;

import com.ispw.tryeshifts.AppConfig;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.dao.Repository;
import com.ispw.tryeshifts.entity.Workplace;
import com.ispw.tryeshifts.excpetion.DAOException;


import java.util.ArrayList;
import java.util.List;

public class GetOwnedWorkplaceAC {

    private GetOwnedWorkplaceAC(){
        throw new IllegalStateException("Utility class");
    }
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
