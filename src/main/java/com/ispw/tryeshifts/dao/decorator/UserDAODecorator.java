package com.ispw.tryeshifts.dao.decorator;

import com.ispw.tryeshifts.dao.UserDAO;
import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.exception.DataFetchException;
import com.ispw.tryeshifts.exception.DuplicateEntityException;
import com.ispw.tryeshifts.exception.EntityNotFoundException;

public abstract class UserDAODecorator implements UserDAO {
    protected UserDAO component;

    public UserDAODecorator(UserDAO component) {
        this.component = component;
    }

    public void save(UserInfo user) throws DataFetchException, DuplicateEntityException{
        this.component.save(user);
    }

    public void updateUser(UserInfo user) throws DataFetchException, EntityNotFoundException {
        this.component.updateUser(user);
    }

    public UserInfo findByEmail(String email) throws DataFetchException {
        return this.component.findByEmail(email);
    }

}
