package com.ispw.tryeshifts.dao.jdbc;

import com.ispw.tryeshifts.dao.AvailabilityDAO;
import com.ispw.tryeshifts.dao.DBconnection;
import com.ispw.tryeshifts.entity.Availability;
import com.ispw.tryeshifts.excpetion.DataFetchException;
import com.ispw.tryeshifts.excpetion.DuplicateEntityException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;

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

        try (Connection conn = DBconnection.getConnection();
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
                throw new DuplicateEntityException("Availability",
                        availability.getUserEmail() + " il " + availability.getDay(),e);
            }
            // 2. Errore generico di database (Connessione, permessi, tabella mancante)
            throw new DataFetchException("Impossibile salvare la disponibilità nel database",e);        }
    }

    public void deleteAvailabilitiesByUser(String email, String workplaceName,String weekId) throws DataFetchException {
        String query = "DELETE FROM availabilities WHERE user_email = ? AND workplace_name = ? AND week_id = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            pstmt.setString(2, workplaceName);
            pstmt.setString(3, weekId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataFetchException("Errore durante la cancellazione delle disponibilità: " ,e);
        }
    }
    public List<Availability> getAvailabilitiesByWorkplace(String workplaceName,String weekId) throws DataFetchException {
        List<Availability> list = new ArrayList<>();
        String query = "SELECT user_email, day_name, start_shift, end_shift, week_id " +
                "FROM availabilities WHERE workplace_name = ? AND week_id = ?";

        try (Connection conn = DBconnection.getConnection();
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
            throw new DataFetchException("Errore nel recupero disponibilità per il workplace " + workplaceName + ": ",e);
        }
        return list;
    }

    public List<Availability> getAvailabilitiesByUser(String email, String workplaceName,String weekId) throws DataFetchException {
        List<Availability> list = new ArrayList<>();
        String query = "SELECT workplace_name, week_id, day_name, start_shift, end_shift " +
                "FROM availabilities WHERE user_email = ? AND workplace_name = ? AND week_id = ?";
        try (Connection conn = DBconnection.getConnection();
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
            throw new DataFetchException("Errore nel recupero disponibilità: " ,e);
        }
        return list;
    }
    @Override
    public Map<String, List<String>> getAvailabilitiesByWeek(String workplaceName, String weekId) throws DataFetchException {
        Map<String, List<String>> availabilitiesMap = new HashMap<>();

        // Query che seleziona l'email e compone la chiave della cella
        String query = "SELECT user_email, day_name, start_shift, end_shift " +
                "FROM availabilities " +
                "WHERE workplace_name = ? AND week_id = ?";

        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, workplaceName);
            pstmt.setString(2, weekId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // 1. Ricostruiamo la cellKey per farla corrispondere a quella della UI
                    String day = rs.getString(DAY_NAME);
                    String start = rs.getString(START_SHIFT);
                    String end = rs.getString(END_SHIFT);
                    String cellKey = day + "_" + start + "-" + end;

                    // 2. Recuperiamo l'email del lavoratore
                    String email = rs.getString(USER_EMAIL);

                    // 3. Aggiungiamo l'email alla lista corrispondente a quella cella
                    // Se la chiave non esiste, crea una nuova ArrayList
                    availabilitiesMap.computeIfAbsent(cellKey, k -> new ArrayList<>()).add(email);
                }
            }
        } catch (SQLException e) {
            throw new DataFetchException("impossibile recupare le diposniblità dalla settimana",e);
        }

        return availabilitiesMap;
    }

    @Override
    public void deleteSpecificAvailability(String email, String workplaceName, String weekId, String day, String fullTime) throws EntityNotFoundException, DataFetchException {
        String cleanTime = fullTime.replace(" ","");
        String [] timeParts = cleanTime.split("-");

        if(timeParts.length < 2){
            throw new DataFetchException("Shift format invalid: "+fullTime);
        }

        String startShift = timeParts[0];
        String endShift = timeParts[1];
        String query = "DELETE FROM availabilities" +"WHERE user_email = ?"+ "AND workplace_name = ?"+ "AND week_id = ?"+ "AND day_name = ?"+"AND start_shift = ?"+"AND end_shift = ?";

        try(Connection conn = DBconnection.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(query)){
            pstmt.setString(1,email);
            pstmt.setString(2,workplaceName);
            pstmt.setString(3,weekId);
            pstmt.setString(4,day);
            pstmt.setString(5,startShift);
            pstmt.setString(6,endShift);

            int rowsAffected = pstmt.executeUpdate();

            if(rowsAffected == 0){
                throw new EntityNotFoundException("No availability found to eliminate for: ", email);
            }
        }catch(SQLException e){
            throw new DataFetchException("Impossibile eliminare le availability: ",e);
        }
    }
}
