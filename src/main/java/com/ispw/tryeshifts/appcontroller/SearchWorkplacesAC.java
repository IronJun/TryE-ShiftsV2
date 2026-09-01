package com.ispw.tryeshifts.appcontroller;

import com.ispw.tryeshifts.config.AppConfig;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.dao.WorkplaceDAO;
import com.ispw.tryeshifts.entity.Workplace;
import com.ispw.tryeshifts.exception.BaseException;

import java.util.ArrayList;
import java.util.List;

public class SearchWorkplacesAC {
    private final WorkplaceDAO workplaceRepo;

    public SearchWorkplacesAC(WorkplaceDAO workplaceRepo) {
        this.workplaceRepo = workplaceRepo;
    }
    public SearchWorkplacesAC() {
        this(AppConfig.getInstance().getWorkplaceRepository());
    }

    public List<WorkplaceBean> getAllWorkplaces()throws BaseException{

        List<Workplace> workplaceEntities = workplaceRepo.findAllWorkplaces();

        if (workplaceEntities == null || workplaceEntities.isEmpty()) {
            return new ArrayList<>();
        }
        List<WorkplaceBean> resultBeans = new ArrayList<>();
        for (Workplace entity : workplaceEntities) {
            resultBeans.add(toBean(entity));
        }

        return resultBeans;
    }
    public List<WorkplaceBean> searchByName(String query) throws BaseException {

        if (query == null || query.isEmpty()) {
            return new ArrayList<>();
        }

        List<Workplace> entity = workplaceRepo.findWorkplacesByName(query);

        List<WorkplaceBean> result = new ArrayList<>();
        for (Workplace wp : entity) {
           result.add(toBean(wp));
        }
        return result;

    }
    public List<WorkplaceBean> getWorkplacesByEmail(String email)throws BaseException{
        List<Workplace> workplaceEntities = workplaceRepo.findWorkplacesByEmail(email);
        if (workplaceEntities == null || workplaceEntities.isEmpty()) {
            return new ArrayList<>();
        }
        List<WorkplaceBean> resultBeans = new ArrayList<>();

        for (Workplace entity : workplaceEntities) {
            resultBeans.add(toBean(entity));
        }
        return resultBeans;
    }
    private WorkplaceBean toBean(Workplace entity){
        return new WorkplaceBean(entity.getName(), entity.getAddress(), entity.getSelectedDays(),entity.getShifts(),entity.getOwnerEmail());
    }
}
