package com.ispw.tryeshifts.dao.decorator;

import com.ispw.tryeshifts.dao.UserDAO;
import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.excpetion.DataFetchException;
import com.ispw.tryeshifts.excpetion.DuplicateEntityException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;

import javax.xml.crypto.Data;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;

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
        }catch (IOException _){
            throw new DataFetchException("I/O Error during the file writing");
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
        }catch (IOException _){
            throw new DataFetchException("could not update the csv file");
        }
        try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(FILE_PATH)))){
            for(String l : lines){
                out.println(l);
            }
        }catch(IOException _){
            throw new DataFetchException("could not update the csv file");
        }
    }

    @Override
    public UserInfo findByEmail(String email) throws DataFetchException {
        return wrappedDAO.findByEmail(email);
    }
}
