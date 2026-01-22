package com.ispw.tryeshifts.dao;

import com.ispw.tryeshifts.entity.Membership;
import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.entity.Workplace;
import com.ispw.tryeshifts.excpetion.DAOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MembershipDAOJdbc implements MembershipDAO {
    private final String userEmail = "user_email";
    private final String isAccepted = "is_accepted";
    private final String workplaceStrName = "workplace_name";


    public void saveMembership(Membership m) throws DAOException {
        String query = "INSERT INTO memberships (user_email, workplace_name, role, is_accepted) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, m.getUser().getEmail());
            pstmt.setString(2, m.getWorkplace().getName());
            pstmt.setString(3, m.getRole());
            pstmt.setBoolean(4, m.isAccepted());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore salvataggio membership: " + e.getMessage());
        }
    }
    public void updateMembership(Membership updateMembership) throws DAOException {
        String query = "UPDATE memberships SET role = ?, is_accepted = ? WHERE user_email = ? AND workplace_name = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, updateMembership.getRole());
            pstmt.setBoolean(2, updateMembership.isAccepted());
            pstmt.setString(3, updateMembership.getUser().getEmail());
            pstmt.setString(4, updateMembership.getWorkplace().getName());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore aggiornamento membership: " + e.getMessage());
        }
    }
    public void removeMembership(Membership membership) throws DAOException {
        String query = "DELETE FROM memberships WHERE user_email = ? AND workplace_name = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, membership.getUser().getEmail());
            pstmt.setString(2, membership.getWorkplace().getName());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore rimozione membership: " + e.getMessage());
        }
    }
    public Membership findMembership(String email, String workplaceName) throws DAOException {
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
            throw new DAOException("Errore findMembership: " + e.getMessage());
        }
        return null;
    }
    public List<Membership> getMembershipByUser(String email) throws DAOException {
        List<Membership> list = new ArrayList<>();
        String query = "SELECT workplace_name, role, is_accepted FROM memberships WHERE user_email = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Workplace wp = new Workplace();
                    wp.setName(rs.getString(workplaceStrName));
                    UserInfo ui = new UserInfo(email, null, null);
                    list.add(new Membership(ui, wp, rs.getString("role"), rs.getBoolean(isAccepted)));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Errore getMembershipByUser: " + e.getMessage());
        }
        return list;
    }
    public List<Membership> getPendingRequestsForOwner(String ownerEmail) throws DAOException {
        List<Membership> list = new ArrayList<>();
        // Questa query unisce memberships e workplaces per trovare le richieste di quei posti che appartengono all'owner
        String query = "SELECT m.user_email, m.workplace_name, m.role, m.is_accepted " +
                "FROM memberships m JOIN workplaces w ON m.workplace_name = w.name " +
                "WHERE w.owner_email = ? AND m.is_accepted = false";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, ownerEmail);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Workplace wp = new Workplace();
                    wp.setName(rs.getString(workplaceStrName));
                    UserInfo ui = new UserInfo(rs.getString(userEmail), null, null);
                    list.add(new Membership(ui, wp, rs.getString("role"), rs.getBoolean(isAccepted)));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Errore getPendingRequestsForOwner: " + e.getMessage());
        }
        return list;
    }
    public List<Membership> getMembershipsByWorkplace(String workplaceName) throws DAOException {
        List<Membership> list = new ArrayList<>();
        String query = "SELECT user_email, role, is_accepted FROM memberships WHERE workplace_name = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, workplaceName);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    UserInfo ui = new UserInfo(rs.getString(userEmail), null, null);
                    Workplace wp = new Workplace();
                    wp.setName(workplaceName);
                    list.add(new Membership(ui, wp, rs.getString("role"), rs.getBoolean(isAccepted)));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Errore getMembershipsByWorkplace: " + e.getMessage());
        }
        return list;
    }
    public boolean isUserMemberOf(String email, String workplaceName) throws DAOException {
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
            throw new DAOException("Errore nel controllo permessi: " + e.getMessage());
        }
    }
}
