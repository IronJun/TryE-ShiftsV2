package com.ispw.tryeshifts.dao;

import com.ispw.tryeshifts.entity.Membership;
import com.ispw.tryeshifts.excpetion.DataFetchException;
import com.ispw.tryeshifts.excpetion.DuplicateEntityException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;

import java.util.List;

public interface MembershipDAO {
    void saveMembership(Membership m) throws DuplicateEntityException, DataFetchException;
    void updateMembership(Membership updateMembership)throws EntityNotFoundException,DataFetchException;
    void removeMembership(Membership membership)throws EntityNotFoundException,DataFetchException;
    Membership findMembership(String email,String workplaceName)throws DataFetchException;
    List<Membership> getMembershipsByWorkplace(String workplaceName)throws DataFetchException;
    boolean isUserMemberOf(String email,String workplaceName)throws DataFetchException;
}
