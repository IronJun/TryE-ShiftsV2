package com.ispw.tryeshifts.dao.jdbc;

import com.ispw.tryeshifts.dao.DBconnection;
import com.ispw.tryeshifts.dao.NotificationDAO;
import com.ispw.tryeshifts.entity.Notification;
import com.ispw.tryeshifts.excpetion.BaseException;
import com.ispw.tryeshifts.excpetion.DataFetchException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAOJdbc implements NotificationDAO {
    private static final List<Notification> notifications = new ArrayList<>();

    @Override
    public List<Notification> findByUserEmail(String email) throws BaseException {
        List<Notification> result = new ArrayList<>();
        if(email == null|| email.isEmpty()){
            return result;
        }
        String query = "SELECT dest_user, message, type, is_read,timestamp FROM notifications WHERE dest_user = ?";
        try(Connection conn = DBconnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Notification n = new Notification(
                            rs.getString("dest_user"),
                            rs.getString("message"),
                            rs.getString("type"),
                            rs.getBoolean("is_read"),
                            rs.getString("timestamp")
                    );
                    result.add(n);
                }
            }
        }catch (SQLException e){
            throw new BaseException("Error while trying to find notifications by email"+e.getMessage());
        }
        return result;
    }
    @Override
    public void markAllAsread(String email) throws BaseException {
        if(email == null|| email.isEmpty()){
            throw new DataFetchException("Email address cannot be empty");
        }
        String query = "UPDATE notifications SET is_read = ? WHERE dest_user = ?";
        try(Connection conn = DBconnection.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(query)){
            pstmt.setString(1,email);
            pstmt.executeUpdate();
        }catch (SQLException e){
            throw new BaseException("Error while trying to update notifications by email"+e.getMessage());
        }
    }

    @Override
    public void saveNotification(String email, String message, String type) throws BaseException {
        if(email == null||email.isEmpty()){
            throw new DataFetchException("Email address cannot be empty");
        }
        String query = "INSERT INTO notification (dest_user, message, type, is_read, timestamp) VALUES (?, ?, ?, ?, ?)";
        try(Connection conn = DBconnection.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(query)){
            pstmt.setString(1, email);
            pstmt.setString(2, message);
            pstmt.setString(3, type);
            pstmt.setBoolean(4, false); // Appena creata è non letta
            pstmt.setString(5, "Proprio Ora");

            pstmt.executeUpdate();
        }catch (SQLException e){
            throw new BaseException("Error while trying to save notification"+e.getMessage());
        }
    }

    @Override
    public void deleteNotification(String email) throws BaseException {
        if (email == null || email.trim().isEmpty()) {
            return;
        }

        String query = "DELETE FROM notification WHERE dest_user = ?";

        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, email);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new BaseException("Errore JDBC durante la cancellazione delle notifiche: " + e.getMessage());
        }
    }
}

