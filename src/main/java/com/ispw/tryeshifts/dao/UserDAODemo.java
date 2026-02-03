package com.ispw.tryeshifts.dao;

import com.ispw.tryeshifts.entity.UserInfo;

import com.ispw.tryeshifts.excpetion.DuplicateEntityException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;


public class UserDAODemo implements UserDAO{

    private final InMemory db = InMemory.getInstance();


    public void save(UserInfo user) throws DuplicateEntityException {
        if(user != null) db.getUsers().put(user.getEmail(), user);
        else throw new DuplicateEntityException("User", user.getEmail());
    }


    public UserInfo findByEmail(String email) {return  db.getUsers().get(email);}


    public void updateUser(UserInfo updatedUser) throws EntityNotFoundException{
        if(!db.getUsers().containsKey(updatedUser.getEmail())){throw new EntityNotFoundException("User", updatedUser.getEmail());}
        db.getUsers().put(updatedUser.getEmail(),updatedUser);
    }
}
