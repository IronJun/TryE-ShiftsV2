package com.ispw.tryeshifts.dao;

import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.excpetion.*;

public interface UserDAO {
    void save(UserInfo user) throws DuplicateEntityException, DataFetchException;
    void updateUser(UserInfo updateUser) throws EntityNotFoundException,DataFetchException;
    UserInfo findByEmail(String email) throws DataFetchException;
}
