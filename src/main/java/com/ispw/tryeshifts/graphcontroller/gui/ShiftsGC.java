package com.ispw.tryeshifts.graphcontroller.gui;

import com.ispw.tryeshifts.appcontroller.ManageMembersAC;
import com.ispw.tryeshifts.appcontroller.NotificationAC;
import com.ispw.tryeshifts.graphcontroller.gui.component.NavbarGC;
import com.ispw.tryeshifts.graphcontroller.gui.utilities.SceneManager;
import com.ispw.tryeshifts.appcontroller.ManageShiftsAC;
import com.ispw.tryeshifts.appcontroller.PublishShiftsAC;
import com.ispw.tryeshifts.bean.AvailabilityBean;
import com.ispw.tryeshifts.graphcontroller.gui.utilities.factorycalendar.OwnerCellFactory;
import com.ispw.tryeshifts.graphcontroller.gui.utilities.factorycalendar.PublishedCellFactory;
import com.ispw.tryeshifts.graphcontroller.gui.utilities.factorycalendar.ShiftCellProvider;
import com.ispw.tryeshifts.graphcontroller.gui.utilities.factorycalendar.WorkerCellFactory;
import com.ispw.tryeshifts.graphcontroller.gui.utilities.stratgui.BossShiftsStrat;
import com.ispw.tryeshifts.graphcontroller.gui.utilities.stratgui.ShiftsUIStrat;
import com.ispw.tryeshifts.graphcontroller.gui.utilities.stratgui.WorkerShiftsStrat;
import com.ispw.tryeshifts.session.SessionContext;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.excpetion.BaseException;
import com.ispw.tryeshifts.graphcontroller.KeyGenerator;
import com.ispw.tryeshifts.graphcontroller.gui.utilities.*;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ShiftsGC {
    private UserBean loggeduser;
    private WorkplaceBean selectedWorkplace;
    private final Logger logger = Logger.getLogger(ShiftsGC.class.getName());
    private String msg;
    private int weekOffset = 0;
    private String currentWeekId;
    private static final String LOCKED_STATUS = "LOCKED";
    private static final String PUBLISHED_STATUS = "PUBLISHED";
    private static final String TECHNICAL_ERROR = "Technical Error";
    @FXML private GridPane shiftsGrid;
    @FXML private Label workplaceTitleLabel;
    @FXML private Button saveShiftsBtn;
    @FXML private Button publicShiftsBtn;
    @FXML private Label instructionLabel;
    @FXML private Label lblMode;
    @FXML private Label lblWeekDisplay;
    @FXML private Label errorlbl;
    @FXML private NavbarGC navbarController;
    private final Map<String, Boolean> selectedCellsMap = new HashMap<>();
    private final String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    private final String[] daysShown = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    private String shiftsMode= "Forzata" ;
    private final ManageShiftsAC manageAC = new ManageShiftsAC();
    private final PublishShiftsAC pubAc = new PublishShiftsAC();

    public void initialize()  {
        ErrorViewManager.setupAutoHide(errorlbl);
        if(navbarController != null){
            navbarController.setActivePage(NavPage.SHIFTS);
        }
        this.loggeduser = SessionContext.getInstance().getLoggeduser();
        WorkplaceBean info = SessionContext.getInstance().getLoggedWorkplace();
        this.weekOffset = 0;
        this.currentWeekId = manageAC.calculateWeekId(weekOffset);
        if(lblWeekDisplay!=null){
            lblWeekDisplay.setText("Settimana: "+ manageAC.getWeekRangeString(weekOffset));
        }

        if (info != null) {
            this.selectedWorkplace = info;
            setSelectedWorkplace(info);
        }
        try{
            TableContext context = fetchTableContext(); // Recuperiamo i dati freschi

            ShiftsUIStrat strat;
            if(loggeduser.getEmail().equals(selectedWorkplace.getOwnerEmail())){
                strat = new BossShiftsStrat();
            }else{
                strat = new WorkerShiftsStrat();
            }
            strat.customizeUI(instructionLabel, saveShiftsBtn,publicShiftsBtn,context.status());
            lblMode.setText("Modalità pubblicazione turni: "+ shiftsMode);
            buildDynamicTable();
        }catch(BaseException e){
            logger.log(Level.SEVERE, "Errore durante l'inizializzazione della UI", e);
        }

    }

    public void setSelectedWorkplace(WorkplaceBean wp) {
        this.selectedWorkplace = wp;
        this.workplaceTitleLabel.setText(wp.getWorkplaceName());
        // Qui potrai caricare i turni specifici di questo workplace
        buildDynamicTable();
        msg = "DEBUG UI: Workplace selezionato: " + wp.getWorkplaceName();
        logger.log(Level.FINE,msg);
    }

    private void buildDynamicTable() {
        try {
            // 1. Setup Strutturale della Grid
            resetAndSetupGrid();

            // 2. Recupero Dati e Context
            TableContext context = fetchTableContext();
            ShiftsUIStrat strat = (loggeduser.getEmail().equals(selectedWorkplace.getOwnerEmail()))
                    ? new BossShiftsStrat()
                    : new WorkerShiftsStrat();

            strat.customizeUI(instructionLabel, saveShiftsBtn, publicShiftsBtn, context.status());
            // 3. Selezione del Provider (Factory)
            ShiftCellProvider cellProvider = selectCellProvider(context);

            // 4. Popolamento Griglia
            populateGrid(context, cellProvider);

        } catch (BaseException e) {
            msg = TECHNICAL_ERROR + ": " + e.getMessage();
            logger.severe(msg);
        }
    }

    private void resetAndSetupGrid() {
        shiftsGrid.setGridLinesVisible(false);
        shiftsGrid.setGridLinesVisible(true);
        shiftsGrid.getChildren().clear();
        shiftsGrid.getColumnConstraints().clear();
        shiftsGrid.getRowConstraints().clear();

        // Setup Colonne
        for (int i = 0; i < 8; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100.0 / 8.0);
            shiftsGrid.getColumnConstraints().add(col);
        }

        // Setup Header Row
        RowConstraints headerRow = new RowConstraints();
        headerRow.setMinHeight(40);
        headerRow.setPrefHeight(40);
        headerRow.setVgrow(Priority.NEVER);
        shiftsGrid.getRowConstraints().add(headerRow);

        addTableHeaders();
    }
    private ShiftCellProvider selectCellProvider(TableContext ctx) {
        if (ctx.status.equals(PUBLISHED_STATUS)) {
            return new PublishedCellFactory(ctx.assignments);
        }
        if (ctx.isOwner) {
            return new OwnerCellFactory();
        }

        boolean isLocked = ctx.status.equals(LOCKED_STATUS);
        return new WorkerCellFactory(selectedCellsMap, isLocked);
    }
    private void populateGrid(TableContext ctx, ShiftCellProvider provider) {
        List<String> timeSlots = ctx.wp.getShiftsBean();

        for (int r = 0; r < timeSlots.size(); r++) {
            addTimeSlotLabel(timeSlots.get(r), r + 1);

            for (int c = 1; c <= 7; c++) {
                String currentDay = days[c - 1];
                String cellKey = buildCellKey(currentDay, timeSlots.get(r));

                boolean isDayActive = ctx.wp.getSelectedDays().contains(currentDay);
                List<String> cellContent = ctx.shifts.getOrDefault(cellKey, new ArrayList<>());

                updateLocalSelectionMap(cellKey, cellContent, ctx.isOwner);

                VBox cell = provider.createCell(cellKey, cellContent, isDayActive);
                shiftsGrid.add(cell, c, r + 1);
            }
        }
    }



    private record TableContext(
            String status,
            boolean isOwner,
            Map<String, List<String>> shifts,
            Map<String, List<String>> assignments,
            UserBean loggedUser,
            WorkplaceBean wp
    ) {}
    private TableContext fetchTableContext() throws BaseException {
        UserBean user = SessionContext.getInstance().getLoggeduser();
        WorkplaceBean wp = SessionContext.getInstance().getLoggedWorkplace();

        if(user == null || wp == null) {
            ErrorViewManager.showError(errorlbl,"user or workplace is null!");
            return null;
        }
        return new TableContext(
                manageAC.getWeekStatusShifts(wp.getWorkplaceName(), currentWeekId),
                user.getEmail().equals(wp.getOwnerEmail()),
                manageAC.getShiftData(user, wp,currentWeekId),
                pubAc.getAssignmentsForWeek(wp, currentWeekId),
                user,
                wp);
    }
    private void addTableHeaders() {
        // Header ORA
        Label timeHeader = new Label("ORA");
        timeHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #4B4488;");
        shiftsGrid.add(timeHeader, 0, 0);

        // Header GIORNI
        for (int i = 0; i < daysShown.length; i++) {
            Label lbl = new Label(daysShown[i].toUpperCase());
            lbl.setStyle("-fx-font-weight: bold; " +
                    "-fx-text-fill: #4B4488; " +
                    "-fx-background-color: #E8E6F3; " +
                    "-fx-padding: 10; " +
                    "-fx-border-color: #8379B5; " +
                    "-fx-border-width: 0 0 1 0;");
            lbl.setMaxWidth(Double.MAX_VALUE);
            shiftsGrid.add(lbl, i + 1, 0);
        }
    }
    private void addTimeSlotLabel(String slotText, int rowIndex) {
        RowConstraints rowConstraint = new RowConstraints();
        rowConstraint.setMinHeight(80);
        rowConstraint.setPrefHeight(80);
        rowConstraint.setVgrow(Priority.ALWAYS);
        shiftsGrid.getRowConstraints().add(rowConstraint);

        Label timeLbl = new Label(slotText);
        timeLbl.setStyle("-fx-font-weight: bold; " +
                "-fx-text-fill: #4B4488; " +
                "-fx-background-color: #E8E6F3; " +
                "-fx-padding: 10; " +
                "-fx-border-color: #8379B5; " +
                "-fx-border-width: 0 1 1 0;");
        timeLbl.setMaxWidth(Double.MAX_VALUE);
        timeLbl.setMaxHeight(Double.MAX_VALUE);
        timeLbl.setAlignment(Pos.CENTER);
        shiftsGrid.add(timeLbl, 0, rowIndex);
    }
    private String buildCellKey(String day, String slot) {
        return KeyGenerator.buildShiftKey(this.currentWeekId, day, slot);
    }
    private void updateLocalSelectionMap(String cellKey, List<String> cellContent,  boolean isOwner) {
        if (!isOwner) {
            boolean isSelected = cellContent.contains("SELECTED");
            selectedCellsMap.put(cellKey, isSelected);

            // SE QUESTO NON STAMPA, LA CHIAVE cellKey NON MATCHATA CON ctx.shifts
            if (isSelected) {
                msg ="!!! MATCH TROVATO per chiave: " + cellKey;
                logger.info(msg);
            }
        }
    }

    @FXML
    public void onSaveAvailability() {
        // 1. Recuperiamo i dati contestuali
        UserBean loggedUser = SessionContext.getInstance().getLoggeduser();
        WorkplaceBean wp = SessionContext.getInstance().getLoggedWorkplace();
        msg = "DEBUG SAVE: Inizio scansione mappa. Dimensioni mappa: " + selectedCellsMap.size();
        logger.log(Level.FINE, msg);
        // 2. Creiamo una lista di AvailabilityBean
        List<AvailabilityBean> availabilityBeans = new ArrayList<>();

        // Scorriamo la mappa delle celle selezionate
        for (Map.Entry<String, Boolean> entry : selectedCellsMap.entrySet()) {
            msg = "DEBUG SAVE: Cella " + entry.getKey() + " stato: " + entry.getValue();
            logger.log(Level.FINE, msg);
            if (Boolean.TRUE.equals(entry.getValue())) { // Se la cella è selezionata (true)
                String key = entry.getKey(); // "Mon_18:30"
                String[] parts = key.split("_");
                if(parts.length >= 4){
                    String year = parts[0];
                    String week = parts[1];
                    String day = parts[2];      // INDICE 2 per il GIORNO
                    String fullTime = parts[3];
                    String weekIdForDb = year + "_" + week;
                    savePartShifts(loggedUser, wp, availabilityBeans, day ,fullTime,weekIdForDb);

                }
            }
            msg ="DEBUG SAVE: Bean pronti al salvataggio: " + availabilityBeans.size();
            logger.log(Level.FINE, msg);

            // 3. Chiamiamo il Controller Applicativo

        }
        try {
            manageAC.saveAvailabilities(availabilityBeans);

            // Messaggio di successo
            SceneManager.getInstance().showInfoAlert("Salvataggio", "Le tue disponibilità sono state inviate al Boss!");

        } catch (Exception _) {
            SceneManager.getInstance().showErrorAlert("Errore", "Impossibile salvare le disponibilità.");
        }
    }

    private void savePartShifts(UserBean loggedUser, WorkplaceBean wp, List<AvailabilityBean> availabilityBeans, String day, String fullTime,String weekIdForDb) {
        String[] timeParts = fullTime.split("-");
        if (timeParts.length >= 2) {
            String start = timeParts[0].trim();
            String end = timeParts[1].trim(); // "00:00-01:00"

            // LOG DI CONTROLLO FINALE
            msg = "DEBUG SUCCESS: Creato bean per " + day + " dalle " + start + " alle " + end;
            logger.log(Level.FINE, msg);
            AvailabilityBean bean = new AvailabilityBean(
                    loggedUser.getEmail(),
                    wp.getWorkplaceName(),
                    day,
                    start,
                    end,
                    weekIdForDb
            );
            availabilityBeans.add(bean);
        } else {
            msg = "DEBUG FAIL: timeParts ha lunghezza " + timeParts.length + " per la stringa: [" + fullTime + "]";
            logger.log(Level.FINE, msg);
        }
    }

    public void onPublic() {
        WorkplaceBean wp = SessionContext.getInstance().getLoggedWorkplace();

        if(wp == null){
            ErrorViewManager.showError(errorlbl,"workplace is null\n");
            return;
        }
        ManageShiftsAC manageAC = new ManageShiftsAC();
        try{
            String currentStatus = manageAC.getWeekStatusShifts(wp.getWorkplaceName(),this.currentWeekId);

            if("OPEN".equals(currentStatus)){
                manageAC.updateWeekStatusShifts(wp.getWorkplaceName(), this.currentWeekId,LOCKED_STATUS);
                SceneManager.getInstance().showInfoAlert("Locking", "The Shifts are now locked");
            }
            else if(LOCKED_STATUS.equals(currentStatus)){
                PublishShiftsAC pubAC = new PublishShiftsAC();
                pubAC.publish(wp, this.currentWeekId);
                ManageMembersAC manageMembersAC = new ManageMembersAC();
                List<UserBean> workers;
                workers = manageMembersAC.getActiveMembers(wp.getWorkplaceName());
                String message = " Shifts of "+wp.getWorkplaceName()+" has been successfully published.";
                String type = "SHIFTS";
                NotificationAC notificationAC = new NotificationAC();
                List<CompletableFuture<Void>> futures = new ArrayList<>();

                for(UserBean worker : workers ) {
                    futures.add(notificationAC.sendNotificationsAsync(worker.getEmail(), message, type));
                }
                SceneManager.getInstance().showInfoAlert("Pubblicazione", "Turni ufficiali pubblicati e Boss in attesa di approvazione.");
            }
            buildDynamicTable();
        }catch(BaseException _){
            SceneManager.getInstance().showErrorAlert(TECHNICAL_ERROR,"Impossibile aggiornare lo stato dei turni.");
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
        this.currentWeekId = manageAC.calculateWeekId(weekOffset);
        // Aggiorna la label per far capire all'utente dove si trova
        selectedCellsMap.clear();
        lblWeekDisplay.setText("Settimana: "+ manageAC.getWeekRangeString(weekOffset));
        // Ridisegna la tabella (questo metodo ora userà currentWeekId per le chiavi)
        buildDynamicTable();
    }

}
