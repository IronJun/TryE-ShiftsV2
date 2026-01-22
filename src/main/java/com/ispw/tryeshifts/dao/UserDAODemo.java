package com.ispw.tryeshifts.dao;

import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.excpetion.DAOException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;


public class UserDAODemo implements UserDAO{
    private final InMemory db = InMemory.getInstance();

    public void save(UserInfo user) throws DAOException {
        if(user != null){
            db.getUsers().put(user.getEmail(),user);

        }else{
            throw new DAOException("No user passed");
        }
    }
    public UserInfo findByEmail(String email) throws EntityNotFoundException {
        UserInfo user = db.getUsers().get(email);
        if(user == null){
            throw new EntityNotFoundException("User with email: " + email + " not found");
        }
        return user;
    }
    public void updateUser(UserInfo updatedUser) throws EntityNotFoundException{
        if(!db.getUsers().containsKey(updatedUser.getEmail())){throw new EntityNotFoundException("User not found");}
        db.getUsers().put(updatedUser.getEmail(),updatedUser);
    }
}
