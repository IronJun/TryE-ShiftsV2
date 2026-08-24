package com.ispw.tryeshifts.dao;

import com.ispw.tryeshifts.entity.Workplace;
import com.ispw.tryeshifts.excpetion.DataFetchException;
import com.ispw.tryeshifts.excpetion.DuplicateEntityException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;

import java.util.List;
import java.util.Map;

public interface WorkplaceDAO {
    void saveWorkplace(Workplace wp) throws DuplicateEntityException,DataFetchException;
    void updateWorkplace(Workplace updateWp,String oldName) throws DataFetchException,EntityNotFoundException,DuplicateEntityException;
    boolean existsWorkplaceByName(String name) throws DataFetchException;
    Workplace findWorkplaceByName(String name)throws DataFetchException,EntityNotFoundException;
    List<Workplace> findWorkplacesbyEmail(String email)throws DataFetchException;
    List<Workplace> findAllWorkplaces()throws DataFetchException;
    List<Workplace> findWorkplacesByName(String name) throws DataFetchException;
    String getWeekStatus(String workplaceName, String weekId)throws DataFetchException;
    void updateWeekStatus(String workplaceName, String weekId, String newStatus)throws DataFetchException;
    void savePublishedShifts(String workplace, String weekId, Map<String, List<String>> assignments)throws DataFetchException;
    Map<String, List<String>> getPublishedShiftsByWeek(String workplaceName, String weekId)throws DataFetchException;
    Map<String, String> getUserPublishedShiftsByWeek(String userEmail, String weekId) throws DataFetchException;
}
