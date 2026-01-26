package com.ispw.tryeshifts.dao;

import com.ispw.tryeshifts.entity.Workplace;
import com.ispw.tryeshifts.excpetion.DataFetchException;
import com.ispw.tryeshifts.excpetion.DuplicateEntityException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WorkplaceDAOJdbc implements WorkplaceDAO {
    private static final Logger LOGGER = Logger.getLogger(WorkplaceDAOJdbc.class.getName());
    private String msg;
    private final String nameUser = "name";
    private final String addressUser = "address";
    private final String ownerEmail = "owner_email";
    private final String dayName = "day_name";


    public void saveWorkplace(Workplace wp) throws DataFetchException,DuplicateEntityException {
        String insertWorkplace = "INSERT INTO workplaces (name, address, owner_email) VALUES (?, ?, ?)";
        String insertDay = "INSERT INTO workplace_days (workplace_id, day_name) VALUES (?, ?)";
        String insertShift = "INSERT INTO workplace_shifts (workplace_id, shift_name) VALUES (?, ?)";

        Connection conn = null;
        try {
            conn = DBconnection.getConnection();
            conn.setAutoCommit(false);

            int workplaceId;
            try (PreparedStatement pstmt = conn.prepareStatement(insertWorkplace, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, wp.getName());
                pstmt.setString(2, wp.getAddress());
                pstmt.setString(3, wp.getOwnerEmail());
                pstmt.executeUpdate();

                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        workplaceId = rs.getInt(1);
                        wp.setId(String.valueOf(workplaceId)); // Aggiorniamo l'oggetto Java
                    } else {
                        throw new DataFetchException("Errore: ID non generato.");
                    }
                }
            }
            try (PreparedStatement pstmtDay = conn.prepareStatement(insertDay)) {
                for (String day : wp.getSelectedDays()) {
                    pstmtDay.setInt(1, workplaceId);
                    pstmtDay.setString(2, day);
                    pstmtDay.addBatch(); // Ottimizziamo le prestazioni
                }
                pstmtDay.executeBatch();
            }

            // 3. Inserimento Turni
            try (PreparedStatement pstmtShift = conn.prepareStatement(insertShift)) {
                for (String shift : wp.getShifts()) {
                    pstmtShift.setInt(1, workplaceId);
                    pstmtShift.setString(2, shift);
                    pstmtShift.addBatch();
                }
                pstmtShift.executeBatch();
            }
            conn.commit();
            msg = "Workplace " + wp.getName() + " salvato correttamente.";
            LOGGER.log(Level.FINE, msg);
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.severe("Rollback fallito!");
                }
            }
            if ("23505".equals(e.getSQLState()) || e.getErrorCode() == 1062) {
                throw new DuplicateEntityException("Workplace", wp.getName());
            }
            throw new DataFetchException("Errore tecnico nel salvataggio del workplace");
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.warning("Errore durante la chiusura della connessione: ");
                }
            }
        }
    }
    public void updateWorkplace(Workplace updateWp, String oldName) throws DataFetchException,DuplicateEntityException,EntityNotFoundException {
        String sql = "UPDATE workplaces SET name = ?, address = ? WHERE TRIM(name) = TRIM(?)";
        try (Connection conn = DBconnection.getConnection();
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
                throw new DuplicateEntityException("Workplace", updateWp.getName());
            }
            throw new DataFetchException("Errore database: " + e.getMessage());
        }
    }
    public boolean existsWorkplaceByName(String name) throws DataFetchException {
        String query = "SELECT COUNT(*) FROM workplaces WHERE name = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new DataFetchException("Errore durante la ricerca del workplace: " + e.getMessage());
        }
    }
    public Workplace findWorkplaceByName(String name) throws DataFetchException {
        String query = "SELECT id, name, address, owner_email FROM workplaces WHERE name = ?";

        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Workplace wp = new Workplace(
                            rs.getString(nameUser),
                            rs.getString(addressUser),
                            null, null,
                            rs.getString(ownerEmail)
                    );
                    wp.setId(String.valueOf(rs.getInt("id")));

                    // USIAMO LA CLASSE DI SUPPORTO
                    fillWorkplaceDetails(wp, conn);

                    return wp;
                }
            }
        } catch (SQLException e) {
            throw new DataFetchException("Errore nel recupero della lista workplace: " + e.getMessage());
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

        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, email);
            pstmt.setString(2, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Workplace wp = new Workplace(
                            rs.getString(nameUser),
                            rs.getString(addressUser),
                            null, null,
                            rs.getString(ownerEmail)
                    );
                    wp.setId(String.valueOf(rs.getInt("id")));

                    // Usiamo il tuo metodo di supporto per caricare giorni e turni
                    fillWorkplaceDetails(wp, conn);

                    list.add(wp);
                }
            }
        } catch (SQLException e) {
            throw new DataFetchException("Errore nel recupero della lista workplace: " + e.getMessage());
        }
        return list;
    }
    public List<Workplace> findAllWorkplaces() throws DataFetchException {
        List<Workplace> list = new ArrayList<>();
        String query = "SELECT id, name, address, owner_email FROM workplaces";

        try (Connection conn = DBconnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Workplace wp = new Workplace(rs.getString(nameUser), rs.getString(addressUser), null, null, rs.getString(ownerEmail));
                wp.setId(String.valueOf(rs.getInt("id")));
                fillWorkplaceDetails(wp, conn);
                list.add(wp);
            }
        } catch (SQLException e) {
            throw new DataFetchException("Errore recupero totale workplace: " + e.getMessage());
        }
        return list;
    }
    public List<Workplace> findWorkplacesByName(String name) throws EntityNotFoundException, DataFetchException {
        List<Workplace> list = new ArrayList<>();
        // Usiamo LIKE per permettere ricerche parziali (es. "Off" trova "Officina")
        String query = "SELECT id, name, address, owner_email FROM workplaces WHERE name LIKE ?";

        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, "%" + name + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Workplace wp = new Workplace(rs.getString(nameUser), rs.getString(addressUser), null, null, rs.getString(ownerEmail));
                    wp.setId(String.valueOf(rs.getInt("id")));
                    fillWorkplaceDetails(wp, conn);
                    list.add(wp);
                }
            }
        } catch (SQLException e) {
            throw new DataFetchException("Errore ricerca per nome: " + e.getMessage());
        }

        if (list.isEmpty()) throw new EntityNotFoundException("Workplace", name);
        return list;
    }
    public String getWeekStatus(String workplaceName, String weekId) throws DataFetchException{
        String query = "SELECT status_name FROM week_status WHERE workplace_name = ? AND week_id = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, workplaceName);
            pstmt.setString(2, weekId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("status_name");
        } catch (SQLException e) {
            throw new DataFetchException("Impossibile recuperare lo stato della settimana:");
        }
        return "OPEN";
    }
    public void updateWeekStatus(String workplaceName, String weekId, String newStatus)throws DataFetchException {
        // ON DUPLICATE KEY UPDATE permette di inserire o aggiornare se esiste già
        String query = "INSERT INTO week_status (workplace_name, week_id, status_name) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE status_name = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, workplaceName);
            pstmt.setString(2, weekId);
            pstmt.setString(3, newStatus);
            pstmt.setString(4, newStatus);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataFetchException("Impossibile aggiornare lo stato della settimana:");
        }
    }
    public void savePublishedShifts(String workplace, String weekId, Map<String, List<String>> assignments) throws DataFetchException{
        String query = "INSERT INTO published_shifts (workplace_name, week_id, cell_key, worker_email) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBconnection.getConnection()) {
            conn.setAutoCommit(false); // Transazione per sicurezza
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                for (Map.Entry<String, List<String>> entry : assignments.entrySet()) {
                    for (String email : entry.getValue()) {
                        pstmt.setString(1, workplace);
                        pstmt.setString(2, weekId);
                        pstmt.setString(3, entry.getKey());
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
        } catch (SQLException e) {
            throw new DataFetchException("Errore DB: impossibile salvare le assegnazioni");
        }
    }
    public Map<String, List<String>> getPublishedShiftsByWeek(String workplaceName, String weekId) throws DataFetchException {
        Map<String, List<String>> shifts = new HashMap<>();
        String query = "SELECT cell_key, worker_email FROM published_shifts WHERE workplace_name = ? AND week_id = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, workplaceName);
            pstmt.setString(2, weekId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String key = rs.getString("cell_key");
                String email = rs.getString("worker_email");
                shifts.computeIfAbsent(key, k -> new ArrayList<>()).add(email);
            }
        } catch (SQLException e) {
            throw new DataFetchException("Errore DB: impossibile recuperare le assegnazioni");
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
                while (rs.next()) days.add(rs.getString(dayName));
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
}
