package com.ispw.tryeshiftsv2.appController;

import com.ispw.tryeshiftsv2.AppConfig;
import com.ispw.tryeshiftsv2.bean.UserBean;
import com.ispw.tryeshiftsv2.dao.Repository;
import com.ispw.tryeshiftsv2.dao.SecurityUtils;
import com.ispw.tryeshiftsv2.entity.UserInfo;
import com.ispw.tryeshiftsv2.excpetion.DAOException;
import com.ispw.tryeshiftsv2.excpetion.EntityNotFoundException;
import com.ispw.tryeshiftsv2.excpetion.InvalidDataException;
import com.ispw.tryeshiftsv2.excpetion.UserAlreadyExistsException;

public class SignupAC {

    public SignupAC(){

    }

    public static void registerUser(UserBean userbean) throws InvalidDataException, UserAlreadyExistsException, DAOException {

        if (isDataInvalid(userbean)) {
            throw new InvalidDataException("Dati non validi");
        } else if (PwdNotMatch(userbean.getPassword(), userbean.getPwdRep())) {
            throw new InvalidDataException("Le password non corrispondono");
        }

        Repository repository = AppConfig.getRepository();

        try{
            if(repository.findByEmail(userbean.getEmail())!= null){
                throw new UserAlreadyExistsException("L'email: "+userbean.getEmail()+" è già in uso");
            }
        }catch (EntityNotFoundException _){

        }
        UserInfo userentity = new UserInfo(userbean.getEmail(), userbean.getName(), userbean.getSurname());


        String hashedPass = SecurityUtils.hashPassword(userbean.getPassword());
        userentity.setPasswordHash(hashedPass);


        repository.save(userentity);

    }
    private static boolean isDataInvalid(UserBean bean) {
        return bean.getEmail().isEmpty() || bean.getName().isEmpty() ||
                bean.getPassword().isEmpty() || bean.getSurname().isEmpty();
    }
    private static boolean PwdNotMatch(String pwd, String pwd2) {
        return !pwd.equals(pwd2);
    }
}

