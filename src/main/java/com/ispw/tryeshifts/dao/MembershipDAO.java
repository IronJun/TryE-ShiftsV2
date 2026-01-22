package com.ispw.tryeshifts.dao;

import com.ispw.tryeshifts.entity.Membership;
import com.ispw.tryeshifts.excpetion.DAOException;

import java.util.List;

public interface MembershipDAO {
    void saveMembership(Membership m) throws DAOException;
    void updateMembership(Membership updateMembership)throws DAOException;
    void removeMembership(Membership membership)throws DAOException;
    Membership findMembership(String email,String workplaceName)throws DAOException;
    List<Membership> getMembershipByUser(String email)throws DAOException;
    List<Membership> getPendingRequestsForOwner(String ownerEmail)throws DAOException;
    List<Membership> getMembershipsByWorkplace(String workplaceName)throws DAOException;
    boolean isUserMemberOf(String email,String workplaceName)throws DAOException;
}
