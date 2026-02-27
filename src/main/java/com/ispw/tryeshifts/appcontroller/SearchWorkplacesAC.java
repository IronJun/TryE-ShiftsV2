package com.ispw.tryeshifts.appcontroller;

import com.ispw.tryeshifts.AppConfig;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.dao.WorkplaceDAO;
import com.ispw.tryeshifts.entity.Workplace;
import com.ispw.tryeshifts.excpetion.BaseException;

import java.util.ArrayList;
import java.util.List;

public class SearchWorkplacesAC {
    private static final WorkplaceDAO workplaceRepo = AppConfig.getWorkplaceRepository();

    private SearchWorkplacesAC(){
        throw new IllegalStateException("Utility class");
    }

    public static List<WorkplaceBean> getAllWorkplaces()throws BaseException{

        List<Workplace> workplaceEntities = workplaceRepo.findAllWorkplaces();

        if (workplaceEntities == null || workplaceEntities.isEmpty()) {
            return new ArrayList<>();
        }
        List<WorkplaceBean> resultBeans = new ArrayList<>();
        for (Workplace entity : workplaceEntities) {
            // Usiamo un costruttore o i setter (meglio costruttore se disponibile per brevità)
            WorkplaceBean bean = new WorkplaceBean();
            bean.setWorkplaceName(entity.getName());
            bean.setAddress(entity.getAddress());
            // Se la Home o la lista richiede l'email del proprietario:
            bean.setOwnerEmail(entity.getOwnerEmail());
            resultBeans.add(bean);
        }

        return resultBeans;
    }
    public static List<WorkplaceBean> searchByName(String query) throws BaseException {

        if (query == null || query.isEmpty()) {
            return new ArrayList<>();
        }

        List<Workplace> entity = workplaceRepo.findWorkplacesByName(query);

        List<WorkplaceBean> result = new ArrayList<>();
        for (Workplace wp : entity) {
           WorkplaceBean wpbean = new WorkplaceBean();
           wpbean.setWorkplaceName(wp.getName());
           wpbean.setAddress(wp.getAddress());
           result.add(wpbean);
        }
        return result;

    }
    public static List<WorkplaceBean> getWorkplacesByEmail(String email)throws BaseException{
        List<Workplace> workplaceEntities = workplaceRepo.findWorkplacesbyEmail(email);
        if (workplaceEntities == null || workplaceEntities.isEmpty()) {
            return new ArrayList<>();
        }
        List<WorkplaceBean> resultBeans = new ArrayList<>();

        for (Workplace entity : workplaceEntities) {
            WorkplaceBean bean = new WorkplaceBean();
            bean.setWorkplaceName((entity).getName());
            bean.setAddress((entity).getAddress());
            resultBeans.add(bean);
        }
        return resultBeans;
    }
}
