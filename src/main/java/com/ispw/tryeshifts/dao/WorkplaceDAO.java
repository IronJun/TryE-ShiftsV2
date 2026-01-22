package com.ispw.tryeshifts.dao;

import com.ispw.tryeshifts.entity.Workplace;
import com.ispw.tryeshifts.excpetion.DAOException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;

import java.util.List;
import java.util.Map;

public interface WorkplaceDAO {
    void saveWorkplace(Workplace wp) throws DAOException;
    void updateWorkplace(Workplace updateWp,String oldName) throws DAOException, EntityNotFoundException;
    boolean existsWorkplaceByName(String name) throws DAOException;
    Workplace findWorkplaceByName(String name)throws EntityNotFoundException,DAOException;
    List<Workplace> findWorkplacesbyEmail(String email) throws DAOException;
    List<Workplace> findAllWorkplaces() throws DAOException;
    List<Workplace> findWorkplacesByName(String name)throws EntityNotFoundException,DAOException;
    String getWeekStatus(String WorkplaceName, String weekId);
    void updateWeekStatus(String workplaceName, String weekId, String newStatus);
    void savePublishedShifts(String workplace, String weekId, Map<String, List<String>> assignments);
    Map<String, List<String>> getPublishedShiftsByWeek(String workplaceName, String weekId);
}
