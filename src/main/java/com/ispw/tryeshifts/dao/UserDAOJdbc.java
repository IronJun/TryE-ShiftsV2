package com.ispw.tryeshifts.dao;

import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.excpetion.DAOException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDAOJdbc implements UserDAO{
    private static final Logger LOGGER = Logger.getLogger(UserDAOJdbc.class.getName());
    private String msg;

    public UserInfo findByEmail(String email) {
        // Selezioniamo solo i campi che servono al tuo costruttore
        String query = "SELECT email, nome, cognome, password FROM users WHERE email = ?";

        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Usiamo il TUO costruttore: email, nome, cognome
                    UserInfo user = new UserInfo(
                            rs.getString("email"),
                            rs.getString("nome"),
                            rs.getString("cognome")
                    );
                    user.setPasswordHash(rs.getString("password"));
                    return user;
                }
            }
        } catch (SQLException e) {
            // Usa il logger che abbiamo sistemato prima per non avere smells!
            LOGGER.log(Level.SEVERE, "Errore durante la ricerca dell'utente", e);
        }
        return null;
    }
    public void save(UserInfo user) throws DAOException {
        String query = "INSERT INTO users (email, nome, cognome,password) VALUES (?,?,?,?)";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {


            pstmt.setString(1, user.getEmail());
            pstmt.setString(2, user.getName());
            pstmt.setString(3, user.getSurname());
            pstmt.setString(4, user.getPasswordHash());

            pstmt.executeUpdate();
            msg = user.getEmail() + " registrato con successo nel database! ";
            LOGGER.log(Level.FINE, msg);
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // Codice errore MySQL per "Duplicate Entry"
                throw new DAOException("Impossibile salvare, esiste già un utente con questo indirizzo email: " + e.getMessage());
            } else {
                throw new DAOException("Errore durante il salvataggio dell'utente: " + e.getMessage());
            }
        }
    }
    public void updateUser(UserInfo updateUser) throws EntityNotFoundException, DAOException {
// Nota: Uso 'users' al plurale come abbiamo fatto per 'workplaces'
        // Usiamo l'email presente nell'oggetto user sia per i nuovi dati che per il WHERE
        String sql = "UPDATE users SET nome = ?, cognome = ?, password = ? WHERE email = ?";

        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, updateUser.getName());
            pstmt.setString(2, updateUser.getSurname());
            pstmt.setString(3, updateUser.getPasswordHash());
            pstmt.setString(4, updateUser.getEmail()); // L'email identifica chi aggiornare

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DAOException("Errore update: " + e.getMessage());
        }
    }
}
