package com.ispw.tryeshifts.dao.decorator;

import com.ispw.tryeshifts.dao.UserDAO;
import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.exception.DataFetchException;
import com.ispw.tryeshifts.exception.DuplicateEntityException;
import com.ispw.tryeshifts.exception.EntityNotFoundException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAOCsvDecorator implements UserDAO {
    private static final String FILE_PATH = "persistency/users.csv";
    private UserDAO wrappedDAO;

    public UserDAOCsvDecorator(UserDAO wrappedDAO) {
        this.wrappedDAO = wrappedDAO;
    }

    @Override
    public void save(UserInfo user) throws DuplicateEntityException, DataFetchException {
        wrappedDAO.save(user);
        try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(FILE_PATH,true)))) {
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
        wrappedDAO.updateUser(updateUser);
        List<String> lines = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))){
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
        try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(FILE_PATH)))){
            for(String l : lines){
                out.println(l);
            }
        }catch(IOException e){
            throw new DataFetchException("could not update the csv file",e);
        }
    }

    @Override
    public UserInfo findByEmail(String email) throws DataFetchException {
        UserInfo user = wrappedDAO.findByEmail(email);
        if(user != null){
            return user;
        }
        try(BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))){
            String line;
            while((line = br.readLine()) != null){
                String[] parts = line.split(";");

                if(parts[0].equals(email)){
                    UserInfo recoveredUser = new UserInfo(parts[0],parts[1],parts[2],parts[3]);
                    wrappedDAO.save(recoveredUser);
                    return recoveredUser;
                }
            }
        }catch (IOException | DuplicateEntityException e){
            throw new DataFetchException("Error reading the CSV file ",e);
        }
        return null;
    }
}
