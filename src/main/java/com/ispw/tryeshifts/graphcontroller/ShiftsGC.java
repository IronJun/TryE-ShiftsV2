package com.ispw.tryeshifts.graphcontroller;

import com.ispw.tryeshifts.SceneManager;
import com.ispw.tryeshifts.appcontroller.ManageShiftsAC;
import com.ispw.tryeshifts.bean.AvailabilityBean;
import com.ispw.tryeshifts.bean.SessionContext;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.excpetion.DAOException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;
import com.ispw.tryeshifts.graphcontroller.utilities.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ShiftsGC {
    private UserBean loggedUser;
    private WorkplaceBean selectedWorkplace;
    private static final Logger LOGGER = Logger.getLogger(ShiftsGC.class.getName());

    @FXML
    private GridPane shiftsGrid;
    @FXML

    private Label workplaceTitleLabel;
    @FXML
    private Button saveShiftsBtn;

    @FXML
    private Button publicShiftsBtn;

    @FXML private Label instructionLabel;

    private Map<String, Boolean> selectedCellsMap = new HashMap<>();

    private final String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

    public void initialize(){
        this.loggedUser = SessionContext.getInstance().getLoggeduser();
        WorkplaceBean info = SessionContext.getInstance().getLoggedWorkplace();
        if (info != null) {
            setSelectedWorkplace(info);
        }
        ShiftsUIStrat strat;
        if(loggedUser.getEmail().equals(selectedWorkplace.getOwnerEmail())){
            strat = new BossShiftsStrat();
        }else{
            strat = new WorkerShiftsStrat();
        }
        strat.customizeUI(instructionLabel, saveShiftsBtn,publicShiftsBtn);
    }


    public void setSelectedWorkplace(WorkplaceBean wp) {
        this.selectedWorkplace = wp;
        this.workplaceTitleLabel.setText(wp.getWorkplaceName());
        // Qui potrai caricare i turni specifici di questo workplace
        buildDynamicTable();
        LOGGER.info("Benvenuto nei turni di: " + wp.getWorkplaceName());
    }



    private void buildDynamicTable() {

        // 1. Reset e Setup Iniziale (come prima)
        try {

            shiftsGrid.getChildren().clear();
            shiftsGrid.getColumnConstraints().clear();
            shiftsGrid.getRowConstraints().clear();

            for (int i = 0; i < 8; i++) {
                ColumnConstraints col = new ColumnConstraints();
                col.setPercentWidth(100.0 / 8.0);
                shiftsGrid.getColumnConstraints().add(col);
            }

            for (int i = 0; i < days.length; i++) {
                Label lbl = new Label(days[i]);
                lbl.setStyle("-fx-font-weight: bold; -fx-padding: 10;");
                shiftsGrid.add(lbl, i + 1, 0);
            }

            // 2. Recupero Dati e Scelta della Factory (Logica GoF)
            ManageShiftsAC manageShiftsAC = new ManageShiftsAC();
            UserBean loggedUser = SessionContext.getInstance().getLoggeduser();
            WorkplaceBean wp = SessionContext.getInstance().getLoggedWorkplace();
            Map<String, List<String>> shifts = manageShiftsAC.getShiftData(loggedUser, wp);
            List<String> activeDays = wp.getSelectedDays();
            boolean isOwner = loggedUser.getEmail().equals(wp.getOwnerEmail());

            // Qui applichiamo l'Abstract Factory
            ShiftCellProvider cellProvider;
            if (isOwner) {
                cellProvider = new OwnerCellFactory();
            } else {
                // Passiamo la mappa delle celle selezionate alla factory del worker
                cellProvider = new WorkerCellFactory(selectedCellsMap);
            }

            // 3. Costruzione dinamica della griglia..
            List<String> timeSlots = wp.getShiftsBean();

            for (int r = 0; r < timeSlots.size(); r++) {
                RowConstraints rowConstraint = new RowConstraints();
                rowConstraint.setMinHeight(80);
                rowConstraint.setPrefHeight(80);
                shiftsGrid.getRowConstraints().add(rowConstraint);

                // Label Orario (Colonna 0)
                Label timeLbl = new Label(timeSlots.get(r));
                timeLbl.setStyle("-fx-font-weight: bold; -fx-padding: 10; -fx-background-color: #E8E6F3;");
                timeLbl.setMaxWidth(Double.MAX_VALUE);
                timeLbl.setMaxHeight(Double.MAX_VALUE);
                timeLbl.setAlignment(Pos.CENTER);
                shiftsGrid.add(timeLbl, 0, r + 1);


                for (int c = 1; c <= 7; c++) {
                    String currentDay = days[c - 1];
                    String timeKey =timeSlots.get(r).replace("","");
                    String cellKey = currentDay + "_" + timeKey;
                    LOGGER.info("DEBUG UI: Cerco in mappa la chiave: [" + cellKey + "]");
                    boolean isDayActive = activeDays.contains(currentDay);
                    List<String> cellContent = shifts.getOrDefault(cellKey, new ArrayList<>());
                    if (!isOwner && cellContent.contains("SELECTED")) {
                        selectedCellsMap.put(cellKey, true);
                    }
                    if (!cellContent.isEmpty()) {
                        LOGGER.info("DEBUG UI: TROVATI DATI PER: " + cellKey + " -> " + cellContent);
                    }
                    // DELEGA ALLA FACTORY: Non c'è più IF/ELSE qui!
                    VBox cell = cellProvider.createCell(cellKey, cellContent, isDayActive);

                    shiftsGrid.add(cell, c, r + 1);
                }
            }
        }catch(DAOException | EntityNotFoundException _){
            SceneManager.getInstance().showErrorAlert("Errore tecnico","Impossibile recuperare i turni");
        }
    }

    @FXML
    public void onSaveAvailability() {
        // 1. Recuperiamo i dati contestuali
        UserBean loggedUser = SessionContext.getInstance().getLoggeduser();
        WorkplaceBean wp = SessionContext.getInstance().getLoggedWorkplace();
        LOGGER.info("DEBUG SAVE: Inizio scansione mappa. Dimensioni mappa: " + selectedCellsMap.size());
        // 2. Creiamo una lista di AvailabilityBean
        List<AvailabilityBean> availabilityBeans = new ArrayList<>();

        // Scorriamo la mappa delle celle selezionate
        for (Map.Entry<String, Boolean> entry : selectedCellsMap.entrySet()) {
            LOGGER.info("DEBUG SAVE: Cella " + entry.getKey() + " stato: " + entry.getValue());
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
                        LOGGER.info("DEBUG SUCCESS: Creato bean per " + day + " dalle " + start + " alle " + end);

                        AvailabilityBean bean = new AvailabilityBean(
                                loggedUser.getEmail(),
                                wp.getWorkplaceName(),
                                day,
                                start,
                                end
                        );
                        availabilityBeans.add(bean);
                    } else {
                        // Se finisci qui, stampa esattamente cosa c'è dentro per capire
                        LOGGER.info("DEBUG FAIL: timeParts ha lunghezza " + timeParts.length + " per la stringa: [" + fullTime + "]");
                    }
                }

            }
            LOGGER.info("DEBUG SAVE: Bean pronti al salvataggio: " + availabilityBeans.size());

            // 3. Chiamiamo il Controller Applicativo
            try {
                ManageShiftsAC manageShiftsAC = new ManageShiftsAC();
                manageShiftsAC.saveAvailabilities(availabilityBeans);

                // Messaggio di successo
                SceneManager.getInstance().showInfoAlert("Salvataggio", "Le tue disponibilità sono state inviate al Boss!");

            } catch (Exception _) {
                SceneManager.getInstance().showErrorAlert("Errore", "Impossibile salvare le disponibilità.");
            }
        }
    }


    public void onPublic(ActionEvent actionEvent) {
        SceneManager.getInstance().showInfoAlert("Implementation problmea", "tasto non implementato");
    }

    public void onWorkersclicked(ActionEvent actionEvent) {
        WorkplaceBean wp = SessionContext.getInstance().getLoggedWorkplace();
        if(wp!=null){
            SceneManager.getInstance().switchScene("Workers.fxml", "Gestione Membri", 900, 600);
        }else{
           LOGGER.info("Seleziona un workplace");
        }
    }
    public void goToHome(ActionEvent actionEvent) {

        SceneManager.getInstance().switchScene("Home.fxml", "Home", 900, 600);

    }

    public void onSettingsclicked(ActionEvent actionEvent) {
        WorkplaceBean wp = SessionContext.getInstance().getLoggedWorkplace();
        if(wp!=null){
            SceneManager.getInstance().switchScene("Settings.fxml", "Gestione Membri", 900, 600);
        }else{
            LOGGER.info("Seleziona un workplace");
        }
    }


    public void onLogoutClicked(ActionEvent actionEvent) {
        this.loggedUser = null;
        SceneManager.getInstance().switchScene("Login.fxml", "Login", 900, 600);
        LOGGER.info("Logout effettuato");
    }
}
