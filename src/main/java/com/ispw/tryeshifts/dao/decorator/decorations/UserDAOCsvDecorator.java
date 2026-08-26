package com.ispw.tryeshifts.dao.decorator.decorations;

import com.ispw.tryeshifts.dao.UserDAO;
import com.ispw.tryeshifts.dao.decorator.UserDAODecorator;
import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.exception.DataFetchException;
import com.ispw.tryeshifts.exception.DuplicateEntityException;
import com.ispw.tryeshifts.exception.EntityNotFoundException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAOCsvDecorator extends UserDAODecorator {
    private final String filePath = "persistency/users.csv";

    public UserDAOCsvDecorator(UserDAO component) {
        super(component);
    }

    @Override
    public void save(UserInfo user) throws DuplicateEntityException, DataFetchException {
        super.save(user);
        try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filePath,true)))) {
            String line = String.format("%s;%s;%s;%s",
                    user.getEmail(),
                    user.getPasswordHash(),
                    user.getName(),
                    user.getSurname());
            out.println(line);
        }catch (IOException e){
            throw new DataFetchException("I/O Error during the file writing",e);
        }
    }

    @Override
    public void updateUser(UserInfo updateUser) throws EntityNotFoundException,DataFetchException{
        super.updateUser(updateUser);
        List<String> lines = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new FileReader(filePath))){
            String line;
            while((line=br.readLine())!= null){
                String[] parts = line.split(";");
                if(parts[0].equals(updateUser.getEmail())){
                    lines.add(String.format("%s;%s;%s;%s",
                            updateUser.getEmail(),
                            updateUser.getPasswordHash(),
                            updateUser.getName(),
                            updateUser.getSurname()));
                }else{
                    lines.add(line);
                }
            }
        }catch (IOException e){
            throw new DataFetchException("could not update the csv file",e);
        }
        try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filePath)))){
            for(String l : lines){
                out.println(l);
            }
        }catch(IOException e){
            throw new DataFetchException("could not update the csv file",e);
        }
    }

    @Override
    public UserInfo findByEmail(String email) throws DataFetchException {
        UserInfo user = super.findByEmail(email);
        if(user != null){
            return user;
        }
        try(BufferedReader br = new BufferedReader(new FileReader(filePath))){
            String line;
            while((line = br.readLine()) != null){
                String[] parts = line.split(";");

                if(parts[0].equals(email)){
                   return  new UserInfo(parts[0],parts[1],parts[2],parts[3]);
                }
            }
        }catch (IOException e){
            throw new DataFetchException("Error reading the CSV file ",e);
        }
        return null;
    }
}
