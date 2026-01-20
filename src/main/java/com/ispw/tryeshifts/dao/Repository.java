package com.ispw.tryeshifts.dao;


import com.ispw.tryeshifts.entity.Availability;
import com.ispw.tryeshifts.entity.Membership;
import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.entity.Workplace;
import com.ispw.tryeshifts.excpetion.DAOException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;

import java.util.List;
import java.util.Map;

public interface Repository {

    //gestione utenti
    void save(UserInfo user) throws DAOException;
    void updateUser(UserInfo updateUser) throws EntityNotFoundException,DAOException;
    UserInfo findByEmail(String email) throws EntityNotFoundException,DAOException;

    //gestione workplace
    void saveWorkplace(Workplace wp) throws DAOException;
    void updateWorkplace(Workplace updateWp,String oldName) throws DAOException, EntityNotFoundException;
    boolean existsWorkplaceByName(String name) throws DAOException;
    Workplace findWorkplaceByName(String name)throws EntityNotFoundException,DAOException;
    List<Workplace> findWorkplacesbyEmail(String email) throws DAOException;
    List<Workplace> findAllWorkplaces() throws DAOException;
    List<Workplace> findWorkplacesByName(String name)throws EntityNotFoundException,DAOException;
    String getWeekStatus(String WorkplaceName, String weekId);
    void updateWeekStatus(String workplaceName, String weekId, String newStatus);
    void savePublishedShifts(String workplace, String weekId, Map<String, String> assignments);
    Map<String, String> getPublishedShiftsByWeek(String workplaceName, String weekId);

    //gestione membership
    void saveMembership(Membership m) throws DAOException;
    void updateMembership(Membership updateMembership)throws DAOException;
    void removeMembership(Membership membership)throws DAOException;
    Membership findMembership(String email,String workplaceName)throws DAOException;
    List<Membership> getMembershipByUser(String email)throws DAOException;
    List<Membership> getPendingRequestsForOwner(String ownerEmail)throws DAOException;
    List<Membership> getMembershipsByWorkplace(String workplaceName)throws DAOException;
    boolean isUserMemberOf(String email,String workplaceName)throws DAOException;

    //Gestione availability
    void saveAvailability(Availability availability)throws DAOException;
    void deleteAvailabilitiesByUser(String email,String workplaceName)throws DAOException;
    List<Availability> getAvailabilitiesByWorkplace(String workplaceName)throws DAOException;
    List<Availability> getAvailabilitiesByUser(String email,String workplaceName)throws DAOException;
    Map<String, List<String>> getAvailabilitiesByWeek(String workplaceName, String weekId);


}
