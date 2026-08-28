package com.ispw.tryeshifts.dao.jdbc;

import com.ispw.tryeshifts.dao.DBconnection;
import com.ispw.tryeshifts.dao.WorkplaceDAO;
import com.ispw.tryeshifts.entity.Workplace;
import com.ispw.tryeshifts.exception.DataFetchException;
import com.ispw.tryeshifts.exception.DuplicateEntityException;
import com.ispw.tryeshifts.exception.EntityNotFoundException;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WorkplaceDAOJdbc implements WorkplaceDAO {

    private static final String USER_NAME = "name";
    private static final String USER_ADDRESS = "address";
    private static final String OWNER_EMAIL = "owner_email";
    private static final String DAY_NAME = "day_name";


    public void saveWorkplace(Workplace wp) throws DataFetchException {
        if(wp == null) {
            throw new IllegalArgumentException("wp cannot be null");
        }
        Connection conn = null;
        try {
            conn = DBconnection.getInstance().getConnection(); // Usa il tuo metodo di connessione
            conn.setAutoCommit(false);

            // Delego le operazioni a metodi specializzati
            int generateID = insertMainWorkplace(conn, wp);
            insertWorkplaceDays(conn,generateID, wp);
            insertWorkplaceShifts(conn,generateID, wp);

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { Logger.getLogger(WorkplaceDAOJdbc.class.getName()).log(Level.SEVERE, null, ex); }
            }
            throw new DataFetchException("Errore nel salvataggio del workplace: ", e);
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { Logger.getLogger(WorkplaceDAOJdbc.class.getName()).log(Level.SEVERE, null, e); }
        }
    }
    public void updateWorkplace(Workplace updateWp, String oldName) throws DataFetchException,DuplicateEntityException,EntityNotFoundException {
        String sql = "UPDATE workplaces SET name = ?, address = ? WHERE TRIM(name) = TRIM(?)";
        try (Connection conn = DBconnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, updateWp.getName());
            pstmt.setString(2, updateWp.getAddress());
            pstmt.setString(3, oldName);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new EntityNotFoundException("Workplace", oldName);
            }
        } catch (SQLException e) {
            // Gestione errore duplicato (se il nuovo nome esiste già)
            if ("23505".equals(e.getSQLState()) || e.getErrorCode() == 1062) {
                throw new DuplicateEntityException("Workplace", updateWp.getName(),e);
            }
            throw new DataFetchException("Errore database: ",e);
        }
    }
    public boolean existsWorkplaceByName(String name) throws DataFetchException {
        String query = "SELECT COUNT(*) FROM workplaces WHERE name = ?";
        try (Connection conn = DBconnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new DataFetchException("Errore durante la ricerca del workplace: ",e);
        }
    }
    public Workplace findWorkplaceByName(String name) throws DataFetchException {
        String query = "SELECT id, name, address, owner_email FROM workplaces WHERE name = ?";

        try (Connection conn = DBconnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Workplace wp = new Workplace(
                            rs.getString(USER_NAME),
                            rs.getString(USER_ADDRESS),
                            null, null,
                            rs.getString(OWNER_EMAIL)
                    );
                    wp.setId(String.valueOf(rs.getInt("id")));

                    // USIAMO LA CLASSE DI SUPPORTO
                    fillWorkplaceDetails(wp, conn);

                    return wp;
                }
            }
        } catch (SQLException e) {
            throw new DataFetchException("Errore nel recupero della lista workplace: ",e);
        }
        return null;
    }
    public List<Workplace> findWorkplacesbyEmail(String email) throws DataFetchException {
        List<Workplace> list = new ArrayList<>();

        // Questa query prende:
        // 1. I workplace dove l'utente è il PROPRIETARIO
        // 2. I workplace dove l'utente è un MEMBRO ACCETTATO
        String query = "SELECT id, name, address, owner_email FROM workplaces WHERE owner_email = ? " +
                "UNION " +
                "SELECT w.id, w.name, w.address, w.owner_email " +
                "FROM workplaces w JOIN memberships m ON w.name = m.workplace_name " +
                "WHERE m.user_email = ? AND m.is_accepted = true";

        try (Connection conn = DBconnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, email);
            pstmt.setString(2, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Workplace wp = new Workplace(
                            rs.getString(USER_NAME),
                            rs.getString(USER_ADDRESS),
                            null, null,
                            rs.getString(OWNER_EMAIL)
                    );
                    wp.setId(String.valueOf(rs.getInt("id")));

                    // Usiamo il tuo metodo di supporto per caricare giorni e turni
                    fillWorkplaceDetails(wp, conn);

                    list.add(wp);
                }
            }
        } catch (SQLException e) {
            throw new DataFetchException("Errore nel recupero della lista workplace: ",e);
        }
        return list;
    }
    public List<Workplace> findAllWorkplaces() throws DataFetchException {
        List<Workplace> list = new ArrayList<>();
        String query = "SELECT id, name, address, owner_email FROM workplaces";

        try (Connection conn = DBconnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Workplace wp = new Workplace(rs.getString(USER_NAME), rs.getString(USER_ADDRESS), null, null, rs.getString(OWNER_EMAIL));
                wp.setId(String.valueOf(rs.getInt("id")));
                fillWorkplaceDetails(wp, conn);
                list.add(wp);
            }
        } catch (SQLException e) {
            throw new DataFetchException("Errore recupero totale workplace: ",e);
        }
        return list;
    }
    public List<Workplace> findWorkplacesByName(String name) throws  DataFetchException {
        List<Workplace> list = new ArrayList<>();
        // Usiamo LIKE per permettere ricerche parziali (es. "Off" trova "Officina")
        String query = "SELECT id, name, address, owner_email FROM workplaces WHERE name LIKE ?";

        try (Connection conn = DBconnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, "%" + name + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Workplace wp = new Workplace(rs.getString(USER_NAME), rs.getString(USER_ADDRESS), null, null, rs.getString(OWNER_EMAIL));
                    wp.setId(String.valueOf(rs.getInt("id")));
                    fillWorkplaceDetails(wp, conn);
                    list.add(wp);
                }
            }
        } catch (SQLException e) {
            throw new DataFetchException("Errore ricerca per nome: ",e);
        }
        return list;
    }
    public String getWeekStatus(String workplaceName, String weekId) throws DataFetchException{
        String query = "SELECT status_name FROM week_status WHERE workplace_name = ? AND week_id = ?";
        try (Connection conn = DBconnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, workplaceName);
            pstmt.setString(2, weekId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("status_name");
        } catch (SQLException e) {
            throw new DataFetchException("Impossibile recuperare lo stato della settimana:",e);
        }
        return null;
    }
    public void updateWeekStatus(String workplaceName, String weekId, String newStatus)throws DataFetchException {
        // ON DUPLICATE KEY UPDATE permette di inserire o aggiornare se esiste già
        String query = "INSERT INTO week_status (workplace_name, week_id, status_name) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE status_name = ?";
        try (Connection conn = DBconnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, workplaceName);
            pstmt.setString(2, weekId);
            pstmt.setString(3, newStatus);
            pstmt.setString(4, newStatus);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataFetchException("Impossibile aggiornare lo stato della settimana:",e);
        }
    }
    public void savePublishedShifts(String workplace, String weekId, Map<String, List<String>> assignments) throws DataFetchException{
        String query = "INSERT INTO published_shifts (workplace_name, week_id, cell_key, worker_email) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBconnection.getInstance().getConnection()) {
            conn.setAutoCommit(false); // Transazione per sicurezza
            mapPopulation(conn, workplace, weekId, assignments, query);
        } catch (SQLException e) {
            throw new DataFetchException("Errore DB: impossibile salvare le assegnazioni",e);
        }
    }
    public Map<String, List<String>> getPublishedShiftsByWeek(String workplaceName, String weekId) throws DataFetchException {
        Map<String, List<String>> shifts = new HashMap<>();
        String query = "SELECT cell_key, worker_email FROM published_shifts WHERE workplace_name = ? AND week_id = ?";
        try (Connection conn = DBconnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, workplaceName);
            pstmt.setString(2, weekId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String cellKeyFromDb = rs.getString("cell_key"); // es: "Mon_00:00-01:00"
                String email = rs.getString("worker_email");

                // RICOSTRUZIONE: Aggiungiamo il prefisso della settimana per far felice la UI
                // La UI si aspetta: "weekId_cellKey" (es: "2026_06_Mon_00:00-01:00")
                String fullKeyForUi = weekId + "_" + cellKeyFromDb;

                shifts.computeIfAbsent(fullKeyForUi, k -> new ArrayList<>()).add(email);
            }
        } catch (SQLException e) {
            throw new DataFetchException("Errore DB: impossibile recuperare le assegnazioni",e);
        }
        return shifts;
    }
    private void fillWorkplaceDetails(Workplace wp, Connection conn) throws SQLException {
        // Caricamento Giorni
        String queryDays = "SELECT day_name FROM workplace_days WHERE workplace_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(queryDays)) {
            pstmt.setInt(1, Integer.parseInt(wp.getId()));
            try (ResultSet rs = pstmt.executeQuery()) {
                List<String> days = new ArrayList<>();
                while (rs.next()) days.add(rs.getString(DAY_NAME));
                wp.setSelectedDays(days);
            }
        }
        // Caricamento Turni
        String queryShifts = "SELECT shift_name FROM workplace_shifts WHERE workplace_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(queryShifts)) {
            pstmt.setInt(1, Integer.parseInt(wp.getId()));
            try (ResultSet rs = pstmt.executeQuery()) {
                List<String> shifts = new ArrayList<>();
                while (rs.next()) shifts.add(rs.getString("shift_name"));
                wp.setShifts(shifts);
            }
        }
    }

    private int insertMainWorkplace(Connection dbc, Workplace wp) throws SQLException {
        String sql = "INSERT INTO workplaces (name, address, owner_email) VALUES (?, ?, ?)";
        // Aggiungiamo Statement.RETURN_GENERATED_KEYS
        try (PreparedStatement pstmt = dbc.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, wp.getName());
            pstmt.setString(2, wp.getAddress());
            pstmt.setString(3, wp.getOwnerEmail());
            pstmt.executeUpdate();

            // Recuperiamo l'ID appena creato
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new SQLException("Salvataggio fallito, nessun ID generato.");
                }
            }
        }
    }
    private void insertWorkplaceDays(Connection dbc,int workplaceId, Workplace wp) throws SQLException {
        String insertDay = "INSERT INTO workplace_days (workplace_id, day_name) VALUES (?, ?)";
        try (PreparedStatement pstmt = dbc.prepareStatement(insertDay)) {
            pstmt.setInt(1, workplaceId);
            for (String day : wp.getSelectedDays()) {
                if(day == null || day.isEmpty()) continue;
                pstmt.setString(2, day);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }
    private void insertWorkplaceShifts(Connection dbc, int workplaceId, Workplace wp) throws SQLException {
        // La colonna corretta è shift_name
        String insertShift = "INSERT INTO workplace_shifts (workplace_id, shift_name) VALUES (?, ?)";
        try (PreparedStatement pstmt = dbc.prepareStatement(insertShift)) {
            pstmt.setInt(1, workplaceId);
            for (String shift : wp.getShifts()) {

                pstmt.setString(2, shift); // Passa la stringa intera (es. "08:00-14:00")
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }
    private void mapPopulation(Connection conn, String workplace, String weekId, Map<String, List<String>> assignments,String query) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, workplace);
            pstmt.setString(2, weekId);
            for (Map.Entry<String, List<String>> entry : assignments.entrySet()) {
                pstmt.setString(3, entry.getKey());
                for (String email : entry.getValue()) {
                    pstmt.setString(4, email);
                    pstmt.addBatch();
                }
            }
            pstmt.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        }
    }

    public Map<String, String> getUserPublishedShiftsByWeek(String userEmail, String weekId) throws DataFetchException {
        Map<String, String> assignments = new HashMap<>();
        String query = "SELECT cell_key, workplace_name FROM published_shifts WHERE worker_email= ? AND week_id= ?";
        try(Connection conn = DBconnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(query)){
            pstmt.setString(1,userEmail);
            pstmt.setString(2,weekId);
            try(ResultSet rs = pstmt.executeQuery()){
                while(rs.next()){
                    String cellKey = rs.getString("cell_key");
                    String wpName = rs.getString("workplace_name");
                    assignments.put(cellKey, wpName);
                }
            }
        }catch (SQLException e) {
            throw new DataFetchException("DB Error: could not fetch user's shifts",e);
        }
        return assignments;
    }
}
