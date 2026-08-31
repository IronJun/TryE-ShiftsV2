package com.ispw.tryeshifts.dao.jdbc;

import com.ispw.tryeshifts.dao.DBconnection;
import com.ispw.tryeshifts.dao.NotificationDAO;
import com.ispw.tryeshifts.entity.Notification;
import com.ispw.tryeshifts.exception.DataFetchException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAOJdbc implements NotificationDAO {

    @Override
    public List<Notification> findByUserEmail(String email) throws DataFetchException {
        List<Notification> result = new ArrayList<>();
        if(email == null|| email.isEmpty()){
            return result;
        }
        String query = "SELECT dest_user, message, type, is_read,timestamp FROM notification WHERE dest_user = ?";
        try(Connection conn = DBconnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(query)) {
            return executeNotificationQuery(email,pstmt);
        }catch (SQLException e){
            throw new DataFetchException("Error while trying to find notifications by email",e);
        }
    }

    private List<Notification> executeNotificationQuery(String email, PreparedStatement pstmt) throws SQLException {
            List<Notification> result = new ArrayList<>();
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
            return result;
    }

    @Override
    public void markAllAsread(String email) throws DataFetchException {
        if(email == null|| email.isEmpty()){
            throw new IllegalArgumentException("Email address cannot be empty");
        }
        String query = "UPDATE notification SET is_read = ? WHERE dest_user = ?";
        try(Connection conn = DBconnection.getInstance().getConnection();
        PreparedStatement pstmt = conn.prepareStatement(query)){
            pstmt.setBoolean(1, true);
            pstmt.setString(2,email);
            pstmt.executeUpdate();
        }catch (SQLException e){
            throw new DataFetchException("Error while trying to update notifications by email",e);
        }
    }

    @Override
    public void saveNotification(Notification notif) throws DataFetchException {
        if(notif.getDestUser() == null){
            throw new IllegalArgumentException("Email address cannot be empty");
        }
        String query = "INSERT INTO notification (dest_user, message, type, is_read, timestamp) VALUES (?, ?, ?, ?, ?)";
        try(Connection conn = DBconnection.getInstance().getConnection();
        PreparedStatement pstmt = conn.prepareStatement(query)){
            pstmt.setString(1, notif.getDestUser());
            pstmt.setString(2, notif.getMessage());
            pstmt.setString(3, notif.getType());
            pstmt.setBoolean(4, false); // Appena creata è non letta
            pstmt.setString(5, "Right nowP");

            pstmt.executeUpdate();
        }catch (SQLException e){
            throw new DataFetchException("Error while trying to save notification",e);
        }
    }

    @Override
    public void deleteNotification(String email) throws DataFetchException {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email address cannot be empty");
        }

        String query = "DELETE FROM notification WHERE dest_user = ?";

        try (Connection conn = DBconnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, email);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new DataFetchException("Errore JDBC durante la cancellazione delle notifiche: ",e);
        }
    }

    public int countNotificationByUserEmail(String email) throws DataFetchException {
        String query = "SELECT COUNT(*) AS total FROM notification Where dest_user = ?";
        int count = 0;

        try(Connection conn = DBconnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(query)){
            count = queryNotifications(email,pstmt);
        } catch (SQLException e) {
            throw new DataFetchException("general error",e);
        }
        return count;
    }

    private int queryNotifications(String email, PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, email);
        int count = 0;
        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                count = rs.getInt("total");

            }
        }
        return count;
    }
}

