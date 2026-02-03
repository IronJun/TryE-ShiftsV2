package com.ispw.tryeshifts.dao;

import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.excpetion.DataFetchException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAOCsv implements UserDAO{
    private static final String FILE_PATH = "persistency/users.csv";
    public void save(UserInfo user) throws DataFetchException{
        try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(FILE_PATH,true)))){
            String line = String.format("%s;%s;%s;%s",
                    user.getEmail(),
                    user.getPasswordHash(),
                    user.getName(),
                    user.getSurname());
            out.println(line);
        }catch(IOException _){
            throw new DataFetchException("Errore di I/O durante la scrittura nel file CSV");
        }
    }
    public void updateUser(UserInfo updateUser) throws DataFetchException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts[0].equalsIgnoreCase(updateUser.getEmail())) {
                    // Sostituiamo con i nuovi dati
                    lines.add(String.format("%s;%s;%s;%s",
                            updateUser.getEmail(),
                            updateUser.getPasswordHash(),
                            updateUser.getName(),
                            updateUser.getSurname()));
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException _) {
            throw new DataFetchException("impossibile aggioranre il file CSV");
        }

        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(FILE_PATH)))) {
            for (String l : lines) {
                out.println(l);
            }
        } catch (IOException _) {
            throw new DataFetchException("impossibile aggioranre il file CSV");
        }
    }
    public UserInfo findByEmail(String email) throws DataFetchException{
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length >= 4 && parts[0].equalsIgnoreCase(email)) {
                    // Ricostruisci l'oggetto con tutti i campi
                    UserInfo ui = new UserInfo(parts[0], parts[2], parts[3]);
                    ui.setPasswordHash(parts[1]); // Rimettiamo la password hashata nel bean
                    return ui;
                }
            }
        } catch (IOException _) {
            throw new DataFetchException("Errore durante la lettura del file CSV: ");
        }
        return null;
    }
}
