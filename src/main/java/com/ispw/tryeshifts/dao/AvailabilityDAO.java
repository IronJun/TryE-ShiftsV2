package com.ispw.tryeshifts.dao;

import com.ispw.tryeshifts.entity.Availability;
import com.ispw.tryeshifts.exception.DataFetchException;
import com.ispw.tryeshifts.exception.DuplicateEntityException;
import com.ispw.tryeshifts.exception.EntityNotFoundException;

import java.util.List;
import java.util.Map;

public interface AvailabilityDAO {
    void saveAvailability(Availability availability)throws DuplicateEntityException, DataFetchException;
    void deleteAvailabilitiesByUser(String email,String workplaceName,String weekId)throws DataFetchException;
    List<Availability> getAvailabilitiesByWorkplace(String workplaceName,String weekId)throws DataFetchException;
    List<Availability> getAvailabilitiesByUser(String email,String workplaceName,String weekId)throws DataFetchException;
    Map<String, List<String>> getAvailabilitiesByWeek(String workplaceName, String weekId)throws DataFetchException;
    void deleteSpecificAvailability(Availability availability)throws EntityNotFoundException, DataFetchException;
}
