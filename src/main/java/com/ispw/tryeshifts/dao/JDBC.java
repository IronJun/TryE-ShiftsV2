package com.ispw.tryeshifts.dao;

import com.ispw.tryeshifts.entity.Availability;
import com.ispw.tryeshifts.entity.Membership;
import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.entity.Workplace;
import com.ispw.tryeshifts.excpetion.DAOException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JDBC implements Repository{
    private static final Logger LOGGER = Logger.getLogger(JDBC.class.getName());
    private String msg;

    public UserInfo findByEmail(String email) {
        // Selezioniamo solo i campi che servono al tuo costruttore
        String query = "SELECT email, nome, cognome, password FROM users WHERE email = ?";

        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Usiamo il TUO costruttore: email, nome, cognome
                    UserInfo user = new UserInfo(
                            rs.getString("email"),
                            rs.getString("nome"),
                            rs.getString("cognome")
                    );
                    user.setPasswordHash(rs.getString("password"));
                    return user;
                }
            }
        } catch (SQLException e) {
            // Usa il logger che abbiamo sistemato prima per non avere smells!
            LOGGER.log(Level.SEVERE, "Errore durante la ricerca dell'utente", e);
        }
        return null;
    }
    public void save(UserInfo user) throws DAOException{
        String query = "INSERT INTO users (email, nome, cognome,password) VALUES (?,?,?,?)";
        try(Connection conn = DBconnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(query)){


            pstmt.setString(1,user.getEmail());
            pstmt.setString(2, user.getName());
            pstmt.setString(3, user.getSurname());
            pstmt.setString(4, user.getPasswordHash());

            pstmt.executeUpdate();
            msg = user.getEmail()+ " registrato con successo nel database! ";
            LOGGER.log(Level.FINE,msg);
        }catch (SQLException e){
            if (e.getErrorCode() == 1062) { // Codice errore MySQL per "Duplicate Entry"
                throw new DAOException("Impossibile salvare, esiste già un utente con questo indirizzo email: " + e.getMessage());
            } else {
                throw new DAOException("Errore durante il salvataggio dell'utente: " + e.getMessage());
            }
        }
    }
    public void updateUser(UserInfo updateUser) throws EntityNotFoundException,DAOException{
        throw new UnsupportedOperationException("Metodo JDBC non ancora implementato");
    }

    //gestione workplace
    public void saveWorkplace(Workplace wp) throws DAOException {
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
                        throw new SQLException("Errore: ID non generato.");
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
            LOGGER.log(Level.FINE,msg);
        }catch (SQLException e){
            if(conn!=null){
                try{conn.rollback();}catch (SQLException ex){LOGGER.log(Level.SEVERE,"Rollback fallito",ex);}
                }
            throw new DAOException("Errore durante il salvataggio del workplace: " + e.getMessage());
        }finally {
            if(conn!=null){
                try{
                    conn.setAutoCommit(true);
                    conn.close();
                }catch(SQLException e){
                    LOGGER.log(Level.SEVERE,"Errore durante la chiusura della connessione JDBC",e);
                }
            }
        }
    }
    public void updateWorkplace(Workplace updateWp,String oldName) throws DAOException, EntityNotFoundException{
        throw new UnsupportedOperationException("Metodo JDBC non ancora implementato");
    }
    public boolean existsWorkplaceByName(String name) throws DAOException{
        String query = "SELECT COUNT(*) FROM workplaces WHERE name = ?";
        try(Connection conn = DBconnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(query)){
            pstmt.setString(1, name);
            try(ResultSet rs = pstmt.executeQuery()){
                rs.next();
                return rs.getInt(1) > 0;
            }
        }catch (SQLException e){
            throw new DAOException("Errore durante la ricerca del workplace: " + e.getMessage());
        }
    }
    public Workplace findWorkplaceByName(String name)throws DAOException{
        String query = "SELECT id, name, address, owner_email FROM workplaces WHERE name = ?";

        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Workplace wp = new Workplace(
                            rs.getString("name"),
                            rs.getString("address"),
                            null, null,
                            rs.getString("owner_email")
                    );
                    wp.setId(String.valueOf(rs.getInt("id")));

                    // USIAMO LA CLASSE DI SUPPORTO
                    fillWorkplaceDetails(wp, conn);

                    return wp;
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Errore nel recupero della lista workplace: " + e.getMessage());
        }
        return null;
    }
    public List<Workplace> findWorkplacesbyEmail(String email) throws DAOException{
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
                            rs.getString("name"),
                            rs.getString("address"),
                            null, null,
                            rs.getString("owner_email")
                    );
                    wp.setId(String.valueOf(rs.getInt("id")));

                    // Usiamo il tuo metodo di supporto per caricare giorni e turni
                    fillWorkplaceDetails(wp, conn);

                    list.add(wp);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Errore nel recupero della lista workplace: " + e.getMessage());
        }
        return list;
    }
    public List<Workplace> findAllWorkplaces() throws DAOException{
        List<Workplace> list = new ArrayList<>();
        String query = "SELECT id, name, address, owner_email FROM workplaces";

        try (Connection conn = DBconnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Workplace wp = new Workplace(rs.getString("name"), rs.getString("address"), null, null, rs.getString("owner_email"));
                wp.setId(String.valueOf(rs.getInt("id")));
                fillWorkplaceDetails(wp, conn);
                list.add(wp);
            }
        } catch (SQLException e) { throw new DAOException("Errore recupero totale workplace: " + e.getMessage()); }
        return list;    }
    public List<Workplace> findWorkplacesByName(String name)throws EntityNotFoundException,DAOException{
        List<Workplace> list = new ArrayList<>();
        // Usiamo LIKE per permettere ricerche parziali (es. "Off" trova "Officina")
        String query = "SELECT id, name, address, owner_email FROM workplaces WHERE name LIKE ?";

        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, "%" + name + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Workplace wp = new Workplace(rs.getString("name"), rs.getString("address"), null, null, rs.getString("owner_email"));
                    wp.setId(String.valueOf(rs.getInt("id")));
                    fillWorkplaceDetails(wp, conn);
                    list.add(wp);
                }
            }
        } catch (SQLException e) { throw new DAOException("Errore ricerca per nome: " + e.getMessage()); }

        if (list.isEmpty()) throw new EntityNotFoundException("Nessun workplace trovato con nome: " + name);
        return list;    }

    //classe di supporto caricamento liste:
    private void fillWorkplaceDetails(Workplace wp, Connection conn) throws SQLException {
        // Caricamento Giorni
        String queryDays = "SELECT day_name FROM workplace_days WHERE workplace_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(queryDays)) {
            pstmt.setInt(1, Integer.parseInt(wp.getId()));
            try (ResultSet rs = pstmt.executeQuery()) {
                List<String> days = new ArrayList<>();
                while (rs.next()) days.add(rs.getString("day_name"));
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

    //gestione membership
    public void saveMembership(Membership m) throws DAOException{
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
        }    }
    public void updateMembership(Membership updateMembership)throws DAOException{
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
        }    }
    public void removeMembership(Membership membership)throws DAOException{
        String query = "DELETE FROM memberships WHERE user_email = ? AND workplace_name = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, membership.getUser().getEmail());
            pstmt.setString(2, membership.getWorkplace().getName());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Errore rimozione membership: " + e.getMessage());
        }    }
    public Membership findMembership(String email,String workplaceName)throws DAOException{
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
                    return new Membership(ui, wp, rs.getString("role"), rs.getBoolean("is_accepted"));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Errore findMembership: " + e.getMessage());
        }
        return null;    }
    public List<Membership> getMembershipByUser(String email)throws DAOException{
        List<Membership> list = new ArrayList<>();
        String query = "SELECT workplace_name, role, is_accepted FROM memberships WHERE user_email = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Workplace wp = new Workplace();
                    wp.setName(rs.getString("workplace_name"));
                    UserInfo ui = new UserInfo(email, null, null);
                    list.add(new Membership(ui, wp, rs.getString("role"), rs.getBoolean("is_accepted")));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Errore getMembershipByUser: " + e.getMessage());
        }
        return list;    }
    public List<Membership> getPendingRequestsForOwner(String ownerEmail)throws DAOException{
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
                    wp.setName(rs.getString("workplace_name"));
                    UserInfo ui = new UserInfo(rs.getString("user_email"), null, null);
                    list.add(new Membership(ui, wp, rs.getString("role"), rs.getBoolean("is_accepted")));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Errore getPendingRequestsForOwner: " + e.getMessage());
        }
        return list;    }
    public List<Membership> getMembershipsByWorkplace(String workplaceName)throws DAOException{
        List<Membership> list = new ArrayList<>();
        String query = "SELECT user_email, role, is_accepted FROM memberships WHERE workplace_name = ?";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, workplaceName);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    UserInfo ui = new UserInfo(rs.getString("user_email"), null, null);
                    Workplace wp = new Workplace();
                    wp.setName(workplaceName);
                    list.add(new Membership(ui, wp, rs.getString("role"), rs.getBoolean("is_accepted")));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Errore getMembershipsByWorkplace: " + e.getMessage());
        }
        return list;    }
    public boolean isUserMemberOf(String email,String workplaceName)throws DAOException{
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

    //Gestione availability
    public void saveAvailability(Availability availability)throws DAOException{
        String query = "INSERT INTO availabilities (user_email, workplace_name, day_name, start_shift, end_shift) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, availability.getUserEmail());
            pstmt.setString(2, availability.getWorkplaceName());
            pstmt.setString(3, availability.getDay());

            // Supponendo che startShift ed endShift siano stringhe "HH:mm"
            pstmt.setString(4, availability.getStartShift());
            pstmt.setString(5, availability.getEndShift());

            pstmt.executeUpdate();
            LOGGER.info("Disponibilità salvata per l'utente: " + availability.getUserEmail());

        } catch (SQLException e) {
            throw new DAOException("Errore nel salvataggio della disponibilità: " + e.getMessage());
        }    }
    public void deleteAvailabilitiesByUser(String email,String workplaceName)throws DAOException{
        String query = "DELETE FROM availabilities WHERE user_email = ? AND workplace_name = ?";

        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, email);
            pstmt.setString(2, workplaceName);
            int rowsAffected = pstmt.executeUpdate();
            msg="Eliminate " + rowsAffected + " disponibilità per l'utente: " + email;
            LOGGER.info(msg);

        } catch (SQLException e) {
            throw new DAOException("Errore durante la cancellazione delle disponibilità: " + e.getMessage());
        }    }
    public List<Availability> getAvailabilitiesByWorkplace(String workplaceName)throws DAOException{
        List<Availability> list = new ArrayList<>();
        String query = "SELECT user_email, day_name, start_shift, end_shift FROM availabilities WHERE workplace_name = ?";

        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, workplaceName);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Ricostruiamo l'oggetto Availability
                    list.add(new Availability(
                            rs.getString("user_email"),
                            workplaceName,
                            rs.getString("day_name"),
                            rs.getString("start_shift"),
                            rs.getString("end_shift")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Errore nel recupero disponibilità per il workplace " + workplaceName + ": " + e.getMessage());
        }
        return list;    }
    public List<Availability> getAvailabilitiesByUser(String email,String workplaceName)throws DAOException{
        List<Availability> list = new ArrayList<>();
        String query = "SELECT workplace_name, day_name, start_shift, end_shift FROM availabilities WHERE user_email = ?";

        try (Connection conn = DBconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Availability(
                            email,
                            rs.getString("workplace_name"),
                            rs.getString("day_name"),
                            rs.getString("start_shift"),
                            rs.getString("end_shift")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Errore nel recupero disponibilità: " + e.getMessage());
        }
        return list;
    }

}
