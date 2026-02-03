package com.ispw.tryeshifts.graphcontroller.javaFX;

import com.ispw.tryeshifts.SceneManager;
import com.ispw.tryeshifts.appcontroller.ManageShiftsAC;
import com.ispw.tryeshifts.appcontroller.PublishShiftsAC;
import com.ispw.tryeshifts.bean.AvailabilityBean;
import com.ispw.tryeshifts.bean.SessionContext;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.excpetion.BaseException;
import com.ispw.tryeshifts.excpetion.DataFetchException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;
import com.ispw.tryeshifts.graphcontroller.javaFX.utilities.*;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ShiftsGC {
    private UserBean loggeduser;
    private WorkplaceBean selectedWorkplace;
    private static final Logger LOGGER = Logger.getLogger(ShiftsGC.class.getName());
    private String msg;
    private int weekOffset = 0;
    private String currentWeekId;
    @FXML private GridPane shiftsGrid;
    @FXML private Label workplaceTitleLabel;
    @FXML private Button saveShiftsBtn;
    @FXML private Button publicShiftsBtn;
    @FXML private Label instructionLabel;
    @FXML private Label lblMode;
    @FXML private Label lblWeekDisplay;
    @FXML private Label errorlbl;
    private final Map<String, Boolean> selectedCellsMap = new HashMap<>();
    private final String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    private final String[] daysShown = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    private String shiftsMode= "Forzata" ;


    public void initialize(){
        ErrorViewManager.setupAutoHide(errorlbl);
        this.loggeduser = SessionContext.getInstance().getLoggeduser();
        WorkplaceBean info = SessionContext.getInstance().getLoggedWorkplace();
        this.weekOffset = 0;
        this.currentWeekId = ManageShiftsAC.calculateWeekId(weekOffset);
        if(lblWeekDisplay!=null){
            lblWeekDisplay.setText("Settimana: "+ ManageShiftsAC.getWeekRangeString(weekOffset));
        }

        if (info != null) {
            this.selectedWorkplace = info;
            setSelectedWorkplace(info);
        }




        ShiftsUIStrat strat;
        if(loggeduser.getEmail().equals(selectedWorkplace.getOwnerEmail())){
            strat = new BossShiftsStrat();
        }else{
            strat = new WorkerShiftsStrat();
        }
        strat.customizeUI(instructionLabel, saveShiftsBtn,publicShiftsBtn);
        lblMode.setText("Modalità pubblicazione turni: "+ shiftsMode);
        buildDynamicTable();
    }

    public void setSelectedWorkplace(WorkplaceBean wp) {
        this.selectedWorkplace = wp;
        this.workplaceTitleLabel.setText(wp.getWorkplaceName());
        // Qui potrai caricare i turni specifici di questo workplace
        buildDynamicTable();
        msg = "DEBUG UI: Workplace selezionato: " + wp.getWorkplaceName();
        LOGGER.log(Level.FINE,msg);
    }

    private void buildDynamicTable() {

        // 1. Reset e Setup Iniziale (come prima)
        try {
            shiftsGrid.setGridLinesVisible(false); // Reset
            shiftsGrid.setGridLinesVisible(true);

            shiftsGrid.getChildren().clear();
            shiftsGrid.getColumnConstraints().clear();
            shiftsGrid.getRowConstraints().clear();

            for (int i = 0; i < 8; i++) {
                ColumnConstraints col = new ColumnConstraints();
                col.setPercentWidth(100.0 / 8.0);
                shiftsGrid.getColumnConstraints().add(col);
            }

            RowConstraints headerRow = new RowConstraints();
            headerRow.setMinHeight(40);
            headerRow.setPrefHeight(40);
            headerRow.setVgrow(Priority.NEVER); // Impedisce che la riga si ridimensioni male
            shiftsGrid.getRowConstraints().add(headerRow);

            for (int i = 0; i < daysShown.length; i++) {
                Label lbl = new Label(daysShown[i].toUpperCase());
                lbl.setStyle("-fx-font-weight: bold; " +
                        "-fx-text-fill: #4B4488; " +
                        "-fx-background-color: #E8E6F3; " + // Stesso colore degli orari
                        "-fx-padding: 10; " +
                        "-fx-border-color: #8379B5; " +    // Bordo viola chiaro
                        "-fx-border-width: 0 0 1 0;");
                lbl.setMaxWidth(Double.MAX_VALUE);
                shiftsGrid.add(lbl, i + 1, 0);
            }
            Label timeHeader = new Label("ORA");
            timeHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #4B4488;");
            shiftsGrid.add(timeHeader, 0, 0);

            // 2. Recupero Dati e Scelta della Factory (Logica GoF)
            ManageShiftsAC manageShiftsAC = new ManageShiftsAC();
            PublishShiftsAC publishAC = new PublishShiftsAC();
            UserBean loggedUser = SessionContext.getInstance().getLoggeduser();
            WorkplaceBean wp = SessionContext.getInstance().getLoggedWorkplace();
            Map<String, List<String>> assignments = publishAC.getAssignmentsForWeek(wp, this.currentWeekId);
            Map<String, List<String>> shifts = manageShiftsAC.getShiftData(loggedUser, wp);
            List<String> activeDays = wp.getSelectedDays();
            String status =manageShiftsAC.getWeekStatusShifts(wp.getWorkplaceName(),this.currentWeekId);
            boolean isOwner = loggedUser.getEmail().equals(wp.getOwnerEmail());

            configureUIByStatus(status, isOwner);
            // Qui applichiamo l'Abstract Factory
            ShiftCellProvider cellProvider = null;
            if (status.equals("PUBLISHED")) {
                cellProvider = new PublishedCellFactory(assignments); // Mostra solo i nomi assegnati
            } else if (isOwner) {
                cellProvider = new OwnerCellFactory();
            } else {
                // Se è LOCKED, passiamo un flag alla factory per disabilitare i click
                boolean isLocked = status.equals("LOCKED");
                cellProvider = new WorkerCellFactory(selectedCellsMap, isLocked);
            }

            // 3. Costruzione dinamica della griglia..
            List<String> timeSlots = wp.getShiftsBean();

            for (int r = 0; r < timeSlots.size(); r++) {
                RowConstraints rowConstraint = new RowConstraints();
                rowConstraint.setMinHeight(80);
                rowConstraint.setPrefHeight(80);
                rowConstraint.setVgrow(Priority.ALWAYS); // Aiuta a mantenere la riga visibile
                shiftsGrid.getRowConstraints().add(rowConstraint);

                // Label Orario (Colonna 0)
                Label timeLbl = new Label(timeSlots.get(r));
                timeLbl.setStyle("-fx-font-weight: bold; " +
                        "-fx-text-fill: #4B4488; " +
                        "-fx-background-color: #E8E6F3; " +
                        "-fx-padding: 10; " +
                        "-fx-border-color: #8379B5; " +
                        "-fx-border-width: 0 1 1 0;");
                timeLbl.setMaxWidth(Double.MAX_VALUE);
                timeLbl.setMaxHeight(Double.MAX_VALUE);
                timeLbl.setAlignment(Pos.CENTER);
                shiftsGrid.add(timeLbl, 0, r + 1);


                for (int c = 1; c <= 7; c++) {
                    String currentDay = days[c - 1];
                    String timeKey =timeSlots.get(r).replace(" ","");
                    String cellKey = currentWeekId+"_"+ currentDay + "_" + timeKey;

                    msg = "DEBUG UI: Cerco in mappa la chiave: " +cellKey;
                    LOGGER.log(Level.FINE, msg);
                    boolean isDayActive = activeDays.contains(currentDay);
                    List<String> cellContent = shifts.getOrDefault(cellKey, new ArrayList<>());
                    if (!isOwner && cellContent.contains(loggedUser.getEmail())) {
                        selectedCellsMap.put(cellKey, true);
                        msg = "DEBUG UI: Trovata corrispondenza! Attivo " + cellKey;
                        LOGGER.log(Level.FINE, msg);
                    } else {
                        // Importante: se non c'è nel DB, assicurati che sia false nella mappa locale
                        selectedCellsMap.put(cellKey, false);
                    }
                    if (!cellContent.isEmpty()) {
                        msg = "DEBUG UI: TROVATI DATI PER: " + cellKey + " -> " + cellContent;
                        LOGGER.log(Level.FINE ,msg);
                    }
                    // DELEGA ALLA FACTORY: Non c'è più IF/ELSE qui!
                    VBox cell = cellProvider.createCell(cellKey, cellContent, isDayActive);

                    shiftsGrid.add(cell, c, r + 1);
                }
            }
        }catch(DataFetchException _){
            SceneManager.getInstance().showErrorAlert("Errore tecnico","Impossibile recuperare i turni");
        }catch(EntityNotFoundException _){
            ErrorViewManager.ScreenError("availability not found","Impossibile recuperare i turni");
        }catch(BaseException _){
            ErrorViewManager.ScreenError("Errore tecnico","Impossibile recuperare i turni");
        }
    }

    @FXML
    public void onSaveAvailability() {
        // 1. Recuperiamo i dati contestuali
        UserBean loggedUser = SessionContext.getInstance().getLoggeduser();
        WorkplaceBean wp = SessionContext.getInstance().getLoggedWorkplace();
        msg = "DEBUG SAVE: Inizio scansione mappa. Dimensioni mappa: " + selectedCellsMap.size();
        LOGGER.log(Level.FINE, msg);
        // 2. Creiamo una lista di AvailabilityBean
        List<AvailabilityBean> availabilityBeans = new ArrayList<>();

        // Scorriamo la mappa delle celle selezionate
        for (Map.Entry<String, Boolean> entry : selectedCellsMap.entrySet()) {
            msg = "DEBUG SAVE: Cella " + entry.getKey() + " stato: " + entry.getValue();
            LOGGER.log(Level.FINE, msg);
            if (Boolean.TRUE.equals(entry.getValue())) { // Se la cella è selezionata (true)
                String key = entry.getKey(); // "Mon_18:30"
                String[] parts = key.split("_", 2);

                if (parts.length >= 2) {
                    String day = parts[0];
                    String fullTime = parts[1]; // "00:00-01:00"

                    // Usiamo una Regex più sicura per il trattino
                    // Questo divide la stringa su qualunque trattino, ignorando eventuali spazi
                    String[] timeParts = fullTime.split("-");

                    if (timeParts.length >= 2) {
                        String start = timeParts[0].trim();
                        String end = timeParts[1].trim();

                        // LOG DI CONTROLLO FINALE
                        msg ="DEBUG SUCCESS: Creato bean per " + day + " dalle " + start + " alle " + end;
                        LOGGER.log(Level.FINE, msg);
                        AvailabilityBean bean = new AvailabilityBean(
                                loggedUser.getEmail(),
                                wp.getWorkplaceName(),
                                day,
                                start,
                                end,
                                this.currentWeekId
                        );
                        availabilityBeans.add(bean);
                    } else {
                        msg ="DEBUG FAIL: timeParts ha lunghezza " + timeParts.length + " per la stringa: [" + fullTime + "]";
                        LOGGER.log(Level.FINE, msg);
                    }
                }

            }
            msg ="DEBUG SAVE: Bean pronti al salvataggio: " + availabilityBeans.size();
            LOGGER.log(Level.FINE, msg);

            // 3. Chiamiamo il Controller Applicativo

        }
        try {
            ManageShiftsAC manageShiftsAC = new ManageShiftsAC();
            manageShiftsAC.saveAvailabilities(availabilityBeans);

            // Messaggio di successo
            SceneManager.getInstance().showInfoAlert("Salvataggio", "Le tue disponibilità sono state inviate al Boss!");

        } catch (Exception _) {
            SceneManager.getInstance().showErrorAlert("Errore", "Impossibile salvare le disponibilità.");
        }
    }

    public void onPublic() {
        ManageShiftsAC managShiftsAC = new ManageShiftsAC();
        WorkplaceBean wp = SessionContext.getInstance().getLoggedWorkplace();


        try{
            String currentStatus = managShiftsAC.getWeekStatusShifts(wp.getWorkplaceName(),this.currentWeekId);

            if("OPEN".equals(currentStatus)){
                managShiftsAC.updateWeekStatusShifts(wp.getWorkplaceName(), this.currentWeekId,"LOCKED");
                SceneManager.getInstance().showInfoAlert("Pubblicazione", "Turni ufficiali pubblicati.");
            }
            else if("LOCKED".equals(currentStatus)){
                PublishShiftsAC publishAC = new PublishShiftsAC();
                publishAC.publish(wp, this.currentWeekId);
                SceneManager.getInstance().showInfoAlert("Pubblicazione", "Turni ufficiali pubblicati e Boss in attesa di approvazione.");
            }
            buildDynamicTable();
        }catch(BaseException _){
            SceneManager.getInstance().showErrorAlert("Errore tecnico","Impossibile aggiornare lo stato dei turni.");
        }
        //SceneManager.getInstance().showInfoAlert("Implementation problmea", "tasto non implementato");
    }

    public void onWorkersclicked() {
        WorkplaceBean wp = SessionContext.getInstance().getLoggedWorkplace();
        if(wp!=null){
            SceneManager.getInstance().switchScene("Workers.fxml", "Gestione Membri", 900, 600);
        }else{
           LOGGER.info("Seleziona un workplace");
        }
    }

    public void goToHome() {

        SceneManager.getInstance().switchScene("Home.fxml", "Home", 900, 600);

    }

    public void onSettingsclicked() {
        WorkplaceBean wp = SessionContext.getInstance().getLoggedWorkplace();
        if(wp!=null){
            SceneManager.getInstance().switchScene("Settings.fxml", "Gestione Membri", 900, 600);
        }else{

            msg = "Seleziona un workplace";
            LOGGER.log(Level.FINE, msg);
        }
    }

    public void onLogoutClicked() {
        if(SessionContext.getInstance().logoutConfirmation()){
            SessionContext.getInstance().clearPreferences();
            SceneManager.getInstance().switchScene("Login.fxml", "Login", 900, 600);
            msg = "Logout effettuato";
            LOGGER.log(Level.FINE, msg);
        }
    }
    @FXML
    private void handleNextWeek() {
        if(weekOffset<1){
            weekOffset++;
            updateView();
        }else{
            ErrorViewManager.showError(errorlbl,"puoi dare disponibilità solo per la settimana successiva");
        }
    }

    @FXML
    private void handlePrevWeek() {
        if(weekOffset>0){
            weekOffset--;
            updateView();
        }
    }

    private void updateView() {
        this.currentWeekId = ManageShiftsAC.calculateWeekId(weekOffset);
        // Aggiorna la label per far capire all'utente dove si trova
        selectedCellsMap.clear();
        lblWeekDisplay.setText("Settimana: "+ ManageShiftsAC.getWeekRangeString(weekOffset));
        // Ridisegna la tabella (questo metodo ora userà currentWeekId per le chiavi)
        buildDynamicTable();
    }


    private void configureUIByStatus(String status,boolean isOwner) {
        if (isOwner) {
            // Il capo vede il pulsante, ma il testo cambia
            publicShiftsBtn.setVisible(true);
            if (status.equals("OPEN")) {
                publicShiftsBtn.setText("Lock Availability");
            } else if (status.equals("LOCKED")) {
                publicShiftsBtn.setText("Publish Shifts");
            } else {
                publicShiftsBtn.setVisible(false); // Nascondi se già pubblicato
            }
        } else {
            // Il lavoratore non può salvare se non è OPEN
            boolean canSave = status.equals("OPEN");
            saveShiftsBtn.setDisable(!canSave);

            if (status.equals("LOCKED")) {
                instructionLabel.setText("Inserimento chiuso: il Boss sta elaborando i turni.");
            } else if (status.equals("PUBLISHED")) {
                instructionLabel.setText("Turni ufficiali pubblicati.");
            } else {
                instructionLabel.setText("Seleziona le tue disponibilità per la settimana.");
            }
        }
    }
}
