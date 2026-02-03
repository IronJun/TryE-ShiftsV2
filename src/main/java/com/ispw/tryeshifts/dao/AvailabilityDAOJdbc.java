package com.ispw.tryeshifts.dao;

import com.ispw.tryeshifts.entity.Availability;
import com.ispw.tryeshifts.excpetion.DataFetchException;
import com.ispw.tryeshifts.excpetion.DuplicateEntityException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class AvailabilityDAOJdbc implements AvailabilityDAO{
    private String msg;
    private static final String userEmail = "user_email";
    private static final String dayName = "day_name";
    private static final String workplaceStrName = "workplace_name";
    private static final String startShift = "start_shift";
    private static final String endShift = "end_shift";
    private static final String queryWeekId = "week_id";


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
                        availability.getUserEmail() + " il " + availability.getDay());
            }
            // 2. Errore generico di database (Connessione, permessi, tabella mancante)
            throw new DataFetchException("Impossibile salvare la disponibilità nel database");        }
    }

    public void deleteAvailabilitiesByUser(String email, String workplaceName) throws DataFetchException {
        String query = "DELETE FROM availabilities WHERE user_email = ? AND workplace_name = ?";

        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, email);
            pstmt.setString(2, workplaceName);

        } catch (SQLException e) {
            throw new DataFetchException("Errore durante la cancellazione delle disponibilità: " + e.getMessage());
        }
    }
    public List<Availability> getAvailabilitiesByWorkplace(String workplaceName) throws DataFetchException {
        List<Availability> list = new ArrayList<>();
        String query = "SELECT user_email, day_name, start_shift, end_shift, week_id FROM availabilities WHERE workplace_name = ?";

        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, workplaceName);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Ricostruiamo l'oggetto Availability
                    list.add(new Availability(
                            rs.getString(userEmail),
                            workplaceName,
                            rs.getString(dayName),
                            rs.getString(startShift),
                            rs.getString(endShift),
                            rs.getString(queryWeekId)

                    ));
                }
            }
        } catch (SQLException e) {
            throw new DataFetchException("Errore nel recupero disponibilità per il workplace " + workplaceName + ": " + e.getMessage());
        }
        return list;
    }
    public List<Availability> getAvailabilitiesByUser(String email, String workplaceName) throws DataFetchException {
        List<Availability> list = new ArrayList<>();
        String query = "SELECT workplace_name, week_id, day_name, start_shift, end_shift FROM availabilities WHERE user_email = ? AND workplace_name = ?";

        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, email);
            pstmt.setString(2, workplaceName);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Availability(
                            email,
                            rs.getString(workplaceStrName),
                            rs.getString(dayName),
                            rs.getString(startShift),
                            rs.getString(startShift),
                            rs.getString(queryWeekId)
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DataFetchException("Errore nel recupero disponibilità: " + e.getMessage());
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
                    String day = rs.getString("day_name");
                    String start = rs.getString("start_shift");
                    String end = rs.getString("end_shift");
                    String cellKey = day + "_" + start + "-" + end;

                    // 2. Recuperiamo l'email del lavoratore
                    String email = rs.getString("user_email");

                    // 3. Aggiungiamo l'email alla lista corrispondente a quella cella
                    // Se la chiave non esiste, crea una nuova ArrayList
                    availabilitiesMap.computeIfAbsent(cellKey, k -> new ArrayList<>()).add(email);
                }
            }
        } catch (SQLException _) {
            throw new DataFetchException("impossibile recupare le diposniblità dalla settimana");
        }

        return availabilitiesMap;
    }
}
