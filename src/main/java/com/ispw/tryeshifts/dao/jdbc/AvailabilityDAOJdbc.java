package com.ispw.tryeshifts.dao.jdbc;

import com.ispw.tryeshifts.dao.AvailabilityDAO;
import com.ispw.tryeshifts.dao.DBconnection;
import com.ispw.tryeshifts.entity.Availability;
import com.ispw.tryeshifts.exception.DataFetchException;
import com.ispw.tryeshifts.exception.DuplicateEntityException;
import com.ispw.tryeshifts.exception.EntityNotFoundException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AvailabilityDAOJdbc implements AvailabilityDAO {
    private static final String USER_EMAIL = "user_email";
    private static final String DAY_NAME = "day_name";
    private static final String WORKPLACE_STR_NAME = "workplace_name";
    private static final String START_SHIFT = "start_shift";
    private static final String END_SHIFT = "end_shift";
    private static final String WEEK_ID = "week_id";


    public void saveAvailability(Availability availability) throws DuplicateEntityException, DataFetchException {
        String query = "INSERT INTO availabilities (user_email, workplace_name,week_id, day_name, start_shift, end_shift) VALUES (?, ?, ?, ?, ?,?)";

        try (Connection conn = DBconnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, availability.getUserEmail());
            pstmt.setString(2, availability.getWorkplaceName());
            pstmt.setString(4, availability.getDay());

            // Supponendo che startShift ed endShift siano stringhe "HH:mm"
            pstmt.setString(5, availability.getStartShift());
            pstmt.setString(6, availability.getEndShift());
            pstmt.setString(3, availability.getWeekId());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState()) || e.getErrorCode() == 1062) {
                throw new DuplicateEntityException("Availability", availability.getUserEmail() + " il " + availability.getDay(),e);
            }
            // 2. Errore generico di database (Connessione, permessi, tabella mancante)
            throw new DataFetchException("Impossible to save availability to database",e);
        }
    }

    public void deleteAvailabilitiesByUser(String email, String workplaceName,String weekId) throws DataFetchException {
        String query = "DELETE FROM availabilities WHERE user_email = ? AND workplace_name = ? AND week_id = ?";
        try (Connection conn = DBconnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            pstmt.setString(2, workplaceName);
            pstmt.setString(3, weekId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataFetchException("Error during the deletion of this availability: " ,e);
        }
    }
    public List<Availability> getAvailabilitiesByWorkplace(String workplaceName,String weekId) throws DataFetchException {
        List<Availability> list = new ArrayList<>();
        String query = "SELECT user_email, day_name, start_shift, end_shift, week_id " +
                "FROM availabilities WHERE workplace_name = ? AND week_id = ?";

        try (Connection conn = DBconnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, workplaceName);
            pstmt.setString(2, weekId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Ricostruiamo l'oggetto Availability
                    list.add(new Availability(
                            rs.getString(USER_EMAIL),
                            workplaceName,
                            rs.getString(DAY_NAME),
                            rs.getString(START_SHIFT),
                            rs.getString(END_SHIFT),
                            rs.getString(WEEK_ID)

                    ));
                }
            }
        } catch (SQLException e) {
            throw new DataFetchException("Error fetch workplace availability" + workplaceName + ": ",e);
        }
        return list;
    }
    public List<Availability> getAvailabilitiesByUser(String email, String workplaceName,String weekId) throws DataFetchException {
        List<Availability> list = new ArrayList<>();
        String query = "SELECT workplace_name, week_id, day_name, start_shift, end_shift " +
                "FROM availabilities WHERE user_email = ? AND workplace_name = ? AND week_id = ?";
        try (Connection conn = DBconnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, email);
            pstmt.setString(2, workplaceName);
            pstmt.setString(3, weekId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Availability(
                            email,
                            rs.getString(WORKPLACE_STR_NAME),
                            rs.getString(DAY_NAME),
                            rs.getString(START_SHIFT),
                            rs.getString(END_SHIFT),
                            rs.getString(WEEK_ID)
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DataFetchException("Error loading availability: " ,e);
        }
        return list;
    }
    public Map<String, List<String>> getAvailabilitiesByWeek(String workplaceName, String weekId) throws DataFetchException {
        Map<String, List<String>> availabilitiesMap = new HashMap<>();
        String query = "SELECT user_email, day_name, start_shift, end_shift " +
                "FROM availabilities " +
                "WHERE workplace_name = ? AND week_id = ?";

        try (Connection conn = DBconnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, workplaceName);
            pstmt.setString(2, weekId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String day = rs.getString(DAY_NAME);
                    String start = rs.getString(START_SHIFT);
                    String end = rs.getString(END_SHIFT);
                    String cellKey = day + "_" + start + "-" + end;
                    String email = rs.getString(USER_EMAIL);
                    availabilitiesMap.computeIfAbsent(cellKey, k -> new ArrayList<>()).add(email);
                }
            }
        } catch (SQLException e) {
            throw new DataFetchException("Error loading Week availabilities ",e);
        }
        //Torno mappa composta da giorni associati a liste di availability
        return availabilitiesMap;
    }

    @Override
    public void deleteSpecificAvailability(Availability ava) throws EntityNotFoundException, DataFetchException {
        String cleanTime = ava.getFullShift().replace(" ","");
        String [] timeParts = cleanTime.split("-");

        if(timeParts.length < 2){
            throw new DataFetchException("Shift format invalid: "+ava.getFullShift());
        }

        String startShift = timeParts[0];
        String endShift = timeParts[1];
        String query = "DELETE FROM availabilities " +"WHERE user_email = ? "+ "AND workplace_name = ? "+ "AND week_id = ? "+ "AND day_name = ? "+"AND start_shift = ? "+"AND end_shift = ? ";

        try(Connection conn = DBconnection.getInstance().getConnection();
        PreparedStatement pstmt = conn.prepareStatement(query)){
            pstmt.setString(1,ava.getUserEmail());
            pstmt.setString(2,ava.getWorkplaceName());
            pstmt.setString(3,ava.getWeekId());
            pstmt.setString(4,ava.getDay());
            pstmt.setString(5,startShift);
            pstmt.setString(6,endShift);

            int rowsAffected = pstmt.executeUpdate();

            if(rowsAffected == 0){
                throw new EntityNotFoundException("No availability found to eliminate for: ", ava.getUserEmail());
            }
        }catch(SQLException e){
            throw new DataFetchException("Unable to cancel availability: ",e);
        }
    }

    @Override
    public List<Availability> getAvailabilitiesByUserAndWeek(String email, String weekId) throws DataFetchException {
        String query = """
            SELECT workplace_name, day_name, start_shift, end_shift, week_id
            FROM availabilities
            WHERE user_email = ? AND week_id = ?
            """;

        List<Availability> availabilities = new ArrayList<>();

        try (Connection conn = DBconnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, email);
            pstmt.setString(2, weekId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    availabilities.add(new Availability(
                            email,
                            rs.getString(WORKPLACE_STR_NAME),
                            rs.getString(DAY_NAME),
                            rs.getString(START_SHIFT),
                            rs.getString(END_SHIFT),
                            rs.getString(WEEK_ID)
                    ));
                }
            }

        } catch (SQLException e) {
            throw new DataFetchException(
                    "Unable to load the user availabilities", e);
        }

        return availabilities;
    }

    @Override
    public void deleteAvailabilitiesByWorkplace(String workplaceName)
            throws DataFetchException {
        String query = "DELETE FROM availabilities WHERE workplace_name = ?";
        try (Connection conn = DBconnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, workplaceName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataFetchException("Unable to delete workplace availabilities", e);
        }
    }

    @Override
    public void replaceAvailabilities(String userEmail, String workplaceName, String weekId, List<Availability> availabilities) throws DataFetchException {
        if (userEmail == null || workplaceName == null || weekId == null || availabilities == null) {
            throw new IllegalArgumentException("Missing availability replacement data");
        }

        String deleteQuery = """
            DELETE FROM availabilities
            WHERE user_email = ? AND workplace_name = ? AND week_id = ?
            """;

        String insertQuery = """
            INSERT INTO availabilities
            (user_email, workplace_name, week_id, day_name, start_shift, end_shift)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DBconnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement deleteStatement = conn.prepareStatement(deleteQuery);
                 PreparedStatement insertStatement = conn.prepareStatement(insertQuery)) {
                deleteStatement.setString(1, userEmail);
                deleteStatement.setString(2, workplaceName);
                deleteStatement.setString(3, weekId);
                deleteStatement.executeUpdate();
                for (Availability availability : availabilities) {
                    insertStatement.setString(1, userEmail);
                    insertStatement.setString(2, workplaceName);
                    insertStatement.setString(3, weekId);
                    insertStatement.setString(4, availability.getDay());
                    insertStatement.setString(5, availability.getStartShift());
                    insertStatement.setString(6, availability.getEndShift());
                    insertStatement.addBatch();
                }
                if (!availabilities.isEmpty()) {
                    insertStatement.executeBatch();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new DataFetchException(
                        "Unable to replace the user's availabilities", e
                );
            }

        } catch (SQLException e) {
            throw new DataFetchException(
                    "Database error while replacing availabilities", e
            );
        }
    }
}
