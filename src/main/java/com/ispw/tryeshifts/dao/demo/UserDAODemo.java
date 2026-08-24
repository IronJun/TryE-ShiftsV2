package com.ispw.tryeshifts.dao.demo;

import com.ispw.tryeshifts.dao.InMemory;
import com.ispw.tryeshifts.dao.UserDAO;
import com.ispw.tryeshifts.entity.UserInfo;

import com.ispw.tryeshifts.excpetion.DuplicateEntityException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;


public class UserDAODemo implements UserDAO {

    private final InMemory db = InMemory.getInstance();


    public void save(UserInfo user) throws DuplicateEntityException {
        if(user == null){throw new IllegalArgumentException("Error fetching user");}

        if(db.getUsers().containsKey(user.getEmail())){
            throw new DuplicateEntityException("User", user.getEmail());
        }

        db.getUsers().put(user.getEmail(), user);
    }


    public UserInfo findByEmail(String email) {
        if(email == null){throw new IllegalArgumentException("Error fetching user");}
        return  db.getUsers().get(email);
    }


    public void updateUser(UserInfo updatedUser) throws EntityNotFoundException {
        if(!db.getUsers().containsKey(updatedUser.getEmail())){throw new EntityNotFoundException("User", updatedUser.getEmail());}
        db.getUsers().put(updatedUser.getEmail(),updatedUser);
    }
}
