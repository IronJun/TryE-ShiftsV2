package com.ispw.tryeshifts.dao;

import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.excpetion.DAOException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;

public interface UserDAO {
    void save(UserInfo user) throws DAOException;
    void updateUser(UserInfo updateUser) throws EntityNotFoundException,DAOException;
    UserInfo findByEmail(String email) throws EntityNotFoundException,DAOException;
}
