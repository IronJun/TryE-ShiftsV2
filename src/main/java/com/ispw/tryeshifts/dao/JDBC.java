package com.ispw.tryeshifts.dao;

import com.ispw.tryeshifts.entity.Availability;
import com.ispw.tryeshifts.entity.Membership;
import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.entity.Workplace;
import com.ispw.tryeshifts.excpetion.DAOException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JDBC implements Repository{
    private static final Logger LOGGER = Logger.getLogger(JDBC.class.getName());

    public UserInfo findByEmail(String email) {
        // Selezioniamo solo i campi che servono al tuo costruttore
        String query = "SELECT email, nome, cognome FROM users WHERE email = ?";

        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Usiamo il TUO costruttore: email, nome, cognome
                    return new UserInfo(
                            rs.getString("email"),
                            rs.getString("nome"),
                            rs.getString("cognome")
                    );
                }
            }
        } catch (SQLException e) {
            // Usa il logger che abbiamo sistemato prima per non avere smells!
            LOGGER.log(Level.SEVERE, "Errore durante la ricerca dell'utente", e);
        }
        return null;
    }
    public void save(UserInfo user) throws DAOException{
        throw new UnsupportedOperationException("Metodo JDBC non ancora implementato");
    };
    public void updateUser(UserInfo updateUser) throws EntityNotFoundException,DAOException{
        throw new UnsupportedOperationException("Metodo JDBC non ancora implementato");
    };

    //gestione workplace
    public void saveWorkplace(Workplace wp) throws DAOException{
        throw new UnsupportedOperationException("Metodo JDBC non ancora implementato");
    };
    public void updateWorkplace(Workplace updateWp,String oldName) throws DAOException, EntityNotFoundException{
        throw new UnsupportedOperationException("Metodo JDBC non ancora implementato");
    };
    public boolean existsWorkplaceByName(String name) throws DAOException{
        throw new UnsupportedOperationException("Metodo JDBC non ancora implementato");
    };
    public Workplace findWorkplaceByName(String name)throws EntityNotFoundException,DAOException{
        throw new UnsupportedOperationException("Metodo JDBC non ancora implementato");
    };
    public List<Workplace> findWorkplacesbyEmail(String email) throws DAOException{
        throw new UnsupportedOperationException("Metodo JDBC non ancora implementato");
    };
    public List<Workplace> findAllWorkplaces() throws DAOException{
        throw new UnsupportedOperationException("Metodo JDBC non ancora implementato");
    };
    public List<Workplace> findWorkplacesByName(String name)throws EntityNotFoundException,DAOException{
        throw new UnsupportedOperationException("Metodo JDBC non ancora implementato");
    };

    //gestione membership
    public void saveMembership(Membership m) throws DAOException{
        throw new UnsupportedOperationException("Metodo JDBC non ancora implementato");
    };
    public void updateMembership(Membership updateMembership)throws DAOException{
        throw new UnsupportedOperationException("Metodo JDBC non ancora implementato");
    };
    public void removeMembership(Membership membership)throws DAOException{
        throw new UnsupportedOperationException("Metodo JDBC non ancora implementato");
    };
    public Membership findMembership(String email,String workplaceName)throws DAOException{
        throw new UnsupportedOperationException("Metodo JDBC non ancora implementato");
    };
    public List<Membership> getMembershipByUser(String email)throws DAOException{
        throw new UnsupportedOperationException("Metodo JDBC non ancora implementato");
    };
    public List<Membership> getPendingRequestsForOwner(String ownerEmail)throws DAOException{
        throw new UnsupportedOperationException("Metodo JDBC non ancora implementato");
    };
    public List<Membership> getMembershipsByWorkplace(String workplaceName)throws DAOException{
        throw new UnsupportedOperationException("Metodo JDBC non ancora implementato");
    };
    public boolean isUserMemberOf(String email,String workplaceName)throws DAOException{
        throw new UnsupportedOperationException("Metodo JDBC non ancora implementato");
    };

    //Gestione availability
    public void saveAvailability(Availability availability)throws DAOException{
        throw new UnsupportedOperationException("Metodo JDBC non ancora implementato");
    };
    public void deleteAvailabilitiesByUser(String email,String workplaceName)throws DAOException{
        throw new UnsupportedOperationException("Metodo JDBC non ancora implementato");
    };
    public List<Availability> getAvailabilitiesByWorkplace(String workplaceName)throws DAOException{
        throw new UnsupportedOperationException("Metodo JDBC non ancora implementato");
    };
    public List<Availability> getAvailabilitiesByUser(String email,String workplaceName)throws DAOException{
        throw new UnsupportedOperationException("Metodo JDBC non ancora implementato");
    };


}
