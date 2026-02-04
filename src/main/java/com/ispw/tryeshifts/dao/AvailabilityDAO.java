package com.ispw.tryeshifts.dao;

import com.ispw.tryeshifts.entity.Availability;
import com.ispw.tryeshifts.excpetion.DataFetchException;
import com.ispw.tryeshifts.excpetion.DuplicateEntityException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;

import java.util.List;
import java.util.Map;

public interface AvailabilityDAO {
    void saveAvailability(Availability availability)throws DuplicateEntityException, DataFetchException;
    void deleteAvailabilitiesByUser(String email,String workplaceName,String weekId)throws EntityNotFoundException, DataFetchException;
    List<Availability> getAvailabilitiesByWorkplace(String workplaceName,String weekId)throws DataFetchException;
    List<Availability> getAvailabilitiesByUser(String email,String workplaceName,String weekId)throws DataFetchException;
    Map<String, List<String>> getAvailabilitiesByWeek(String workplaceName, String weekId)throws DataFetchException;

}
