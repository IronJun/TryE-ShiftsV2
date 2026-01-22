package com.ispw.tryeshifts.dao;

import com.ispw.tryeshifts.entity.Availability;
import com.ispw.tryeshifts.excpetion.DAOException;

import java.util.List;
import java.util.Map;

public interface AvailabilityDAO {
    void saveAvailability(Availability availability)throws DAOException;
    void deleteAvailabilitiesByUser(String email,String workplaceName)throws DAOException;
    List<Availability> getAvailabilitiesByWorkplace(String workplaceName)throws DAOException;
    List<Availability> getAvailabilitiesByUser(String email,String workplaceName)throws DAOException;
    Map<String, List<String>> getAvailabilitiesByWeek(String workplaceName, String weekId);

}
