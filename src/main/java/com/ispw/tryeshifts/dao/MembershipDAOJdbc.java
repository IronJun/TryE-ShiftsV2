package com.ispw.tryeshifts.dao;

import com.ispw.tryeshifts.entity.Membership;
import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.entity.Workplace;
import com.ispw.tryeshifts.excpetion.DataFetchException;
import com.ispw.tryeshifts.excpetion.DuplicateEntityException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MembershipDAOJdbc implements MembershipDAO {
    private static final String userEmail = "user_email";
    private static final String isAccepted = "is_accepted";
    private static final String nome = "nome";
    private static final String cognome = "cognome";


    public void saveMembership(Membership m) throws DuplicateEntityException, DataFetchException {
        String query = "INSERT INTO memberships (user_email, workplace_name, role, is_accepted) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, m.getUser().getEmail());
            pstmt.setString(2, m.getWorkplace().getName());
            pstmt.setString(3, m.getRole());
            pstmt.setBoolean(4, m.isAccepted());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState()) || e.getErrorCode() == 1062) {
                throw new DuplicateEntityException("Membership",
                        m.getUser().getEmail() + " per " + m.getWorkplace().getName());
            }

            // 2. Errore tecnico (connessione, sintassi, etc.)
            throw new DataFetchException("Errore critico durante il salvataggio della richiesta di accesso");
        }
    }
    public void updateMembership(Membership updateMembership) throws EntityNotFoundException, DataFetchException {
        String query = "UPDATE memberships SET role = ?, is_accepted = ? WHERE user_email = ? AND workplace_name = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, updateMembership.getRole());
            pstmt.setBoolean(2, updateMembership.isAccepted());
            pstmt.setString(3, updateMembership.getUser().getEmail());
            pstmt.setString(4, updateMembership.getWorkplace().getName());
            int rowsAffected = pstmt.executeUpdate();

            // Se rowsAffected è 0, significa che la coppia email/workplace non esiste nel DB
            if (rowsAffected == 0) {
                throw new EntityNotFoundException("Membership",
                        updateMembership.getUser().getEmail() + " in " + updateMembership.getWorkplace().getName());
            }
        } catch (SQLException _) {
            throw new DataFetchException("Errore critico durante l'aggiornamento SQL della membership");
        }
    }
    public void removeMembership(Membership membership) throws EntityNotFoundException,DataFetchException {
        String query = "DELETE FROM memberships WHERE user_email = ? AND workplace_name = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, membership.getUser().getEmail());
            pstmt.setString(2, membership.getWorkplace().getName());
            int rowsAffected = pstmt.executeUpdate();

            // Se non abbiamo rimosso nulla, lanciamo l'errore di dominio
            if (rowsAffected == 0) {
                throw new EntityNotFoundException("Membership",
                        membership.getUser().getEmail() + " @ " + membership.getWorkplace().getName());
            }
        } catch (SQLException _) {
            // Errore tecnico (connessione, permessi, etc.)
            throw new DataFetchException("Errore durante la rimozione della membership dal database");
        }
    }
    public Membership findMembership(String email, String workplaceName) throws DataFetchException {
        String query = "SELECT role, is_accepted FROM memberships WHERE user_email = ? AND workplace_name = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            pstmt.setString(2, workplaceName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    UserInfo ui = new UserInfo(email, null, null); // Nome e cognome non necessari qui
                    Workplace wp = new Workplace();
                    wp.setName(workplaceName);
                    return new Membership(ui, wp, rs.getString("role"), rs.getBoolean(isAccepted));
                }
            }
        } catch (SQLException e) {
            throw new DataFetchException("Errore findMembership: " + e.getMessage());
        }
        return null;
    }
    public List<Membership> getMembershipsByWorkplace(String workplaceName) throws DataFetchException {
        List<Membership> list = new ArrayList<>();
        String query = "SELECT m.user_email, u.nome, u.cognome, m.role, m.is_accepted " +
                "FROM memberships m " +
                "JOIN users u ON m.user_email = u.email " +
                "WHERE m.workplace_name = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, workplaceName);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    UserInfo ui = new UserInfo(rs.getString(userEmail), rs.getString(nome), rs.getString(cognome));
                    Workplace wp = new Workplace();
                    wp.setName(workplaceName);
                    list.add(new Membership(ui, wp, rs.getString("role"), rs.getBoolean(isAccepted)));
                }
            }
        } catch (SQLException e) {
            throw new DataFetchException("Errore getMembershipsByWorkplace: " + e.getMessage());
        }
        return list;
    }
    public boolean isUserMemberOf(String email, String workplaceName) throws DataFetchException {
        // Controlliamo se l'utente è il PROPRIETARIO (nella tabella workplaces)
        // OPPURE se è un MEMBRO ACCETTATO (nella tabella memberships)
        String query = "SELECT COUNT(*) FROM workplaces w " +
                "LEFT JOIN memberships m ON w.name = m.workplace_name AND m.user_email = ? " +
                "WHERE w.name = ? AND (w.owner_email = ? OR m.is_accepted = true)";

        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, email);      // Per la JOIN con memberships
            pstmt.setString(2, workplaceName); // Per il filtro sul nome
            pstmt.setString(3, email);      // Per il controllo owner_email

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new DataFetchException("Errore nel controllo permessi: " + e.getMessage());
        }
    }
}
