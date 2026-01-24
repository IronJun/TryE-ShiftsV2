package com.ispw.tryeshifts.appcontroller;

import com.ispw.tryeshifts.AppConfig;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.dao.WorkplaceDAO;
import com.ispw.tryeshifts.entity.Workplace;
import com.ispw.tryeshifts.excpetion.DAOException;

import java.util.ArrayList;
import java.util.List;

public class SearchWorkplacesAC {
    private static final WorkplaceDAO workplaceRepo = AppConfig.getWorkplaceRepository();

    public List<WorkplaceBean> getAllWorkplaces() throws DAOException {
        try {
            List<Workplace> workplaceEntities = workplaceRepo.findAllWorkplaces();

            List<WorkplaceBean> resultBeans = new ArrayList<>();
            for (Workplace wpentity : workplaceEntities) {
                WorkplaceBean wpbean = new WorkplaceBean();
                wpbean.setWorkplaceName(wpentity.getName());
                wpbean.setAddress(wpentity.getAddress());
                resultBeans.add(wpbean);
            }
            return resultBeans;
        }catch(Exception _){
            throw new DAOException("Errore di recupero dei workplace");
        }
    }
    public List<WorkplaceBean> searchByName(String query)throws DAOException {

        if (query == null || query.isEmpty()) {
            return new ArrayList<>();
        }

        try {

            List<Workplace> entity = workplaceRepo.findWorkplacesByName(query);

            List<WorkplaceBean> result = new ArrayList<>();
            for (Workplace wp : entity) {
                WorkplaceBean wpbean = new WorkplaceBean();
                wpbean.setWorkplaceName(wp.getName());
                wpbean.setAddress(wp.getAddress());
                result.add(wpbean);
            }
            return result;
        }catch(Exception _){
            throw new DAOException("Errore di ricerca dei workplace");
        }
    }
}
