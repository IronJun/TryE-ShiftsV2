package com.ispw.tryeshiftsv2.appController;

import com.ispw.tryeshiftsv2.AppConfig;
import com.ispw.tryeshiftsv2.bean.WorkplaceBean;
import com.ispw.tryeshiftsv2.entity.Workplace;
import com.ispw.tryeshiftsv2.excpetion.DAOException;

import java.util.ArrayList;
import java.util.List;

public class SearchWorkplacesAC {
    public List<WorkplaceBean> getAllWorkplaces() throws DAOException {
        try {
            List<Workplace> workplaceEntities = AppConfig.getRepository().findAllWorkplaces();

            List<WorkplaceBean> resultBeans = new ArrayList<>();
            for (Workplace wpentity : workplaceEntities) {
                WorkplaceBean wpbean = new WorkplaceBean();
                wpbean.setWorkplaceName(wpentity.getName());
                wpbean.setAddress(wpentity.getAddress());
                resultBeans.add(wpbean);
            }
            return resultBeans;
        }catch(Exception e){
            throw new DAOException("Errore di recupero dei workplace");
        }
    }
    public List<WorkplaceBean> searchByName(String query)throws DAOException {

        if (query == null || query.isEmpty()) {
            return new ArrayList<>();
        }

        try {

            List<Workplace> entity = AppConfig.getRepository().findWorkplacesByName(query);

            List<WorkplaceBean> result = new ArrayList<>();
            for (Workplace wp : entity) {
                WorkplaceBean wpbean = new WorkplaceBean();
                wpbean.setWorkplaceName(wp.getName());
                wpbean.setAddress(wp.getAddress());
                result.add(wpbean);
            }
            return result;
        }catch(Exception e){
            throw new DAOException("Errore di ricerca dei workplace");
        }
    }
}
