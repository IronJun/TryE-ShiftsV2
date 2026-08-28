package com.ispw.tryeshifts.graphcontroller.gui;

import com.ispw.tryeshifts.appcontroller.utils.WeekStatusCalc;
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
import com.ispw.tryeshifts.exception.BaseException;
import com.ispw.tryeshifts.utils.KeyGenerator;
import com.ispw.tryeshifts.graphcontroller.gui.utilities.*;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.util.*;
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
    @FXML private Label countdownLabel;
    private final Map<String, Boolean> selectedCellsMap = new HashMap<>();
    private final String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    private final String[] daysShown = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    private Timeline timeline;
    ManageShiftsAC manageAC = new ManageShiftsAC();
    PublishShiftsAC pubAc = new PublishShiftsAC();


    public void initialize()  {
        ErrorViewManager.setupAutoHide(errorlbl);
        if(navbarController != null){
            navbarController.setActivePage(NavPage.SHIFTS);
        }
        this.loggeduser = SessionContext.getInstance().getLoggeduser();
        WorkplaceBean info = SessionContext.getInstance().getLoggedWorkplace();
        this.weekOffset = 1;
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
            String shiftsMode = "Manual ";
            lblMode.setText("Shifts handling mode: "+ shiftsMode);
            buildDynamicTable();
            setupStateTimer();
        }catch(BaseException e){
            logger.log(Level.SEVERE, "Errore durante l'inizializzazione della UI", e);
        }

    }

    private void setupStateTimer(){
        try{
            WorkplaceBean wp = SessionContext.getInstance().getLoggedWorkplace();
            if(wp == null || this.currentWeekId == null){
                ErrorViewManager.showError(errorlbl,"no workplace passed or week id null");
            }
            String weekCurrentStatus = manageAC.getWeekStatusShifts(wp.getWorkplaceName(), currentWeekId);

            WeekStatusCalc calc = new WeekStatusCalc();

            LocalDateTime deadline = calc.getNextDeadLine(this.currentWeekId, weekCurrentStatus);
            if(deadline != null){
                String actionName ="OPEN".equals(weekCurrentStatus) ? "Until Lock " : "Until Publication";
                startCountDownTimer(deadline,actionName);
            }else{
                if (timeline != null) timeline.stop();
                countdownLabel.setText("official shifts published");
                }
            }catch(BaseException e){
                ErrorViewManager.showError(errorlbl,e.getMessage());
            } catch (Exception e) {
                countdownLabel.setText("Error during status upload");
        }
    }

    private void startCountDownTimer(LocalDateTime deadline, String actionName){
        if(timeline != null)timeline.stop();

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event ->{
            LocalDateTime now = LocalDateTime.now();


            if(now.isAfter(deadline)|| now.isEqual(deadline)){
                countdownLabel.setText("Time expired for: " + actionName +"!");
                timeline.stop();
            }else{
                java.time.Duration diff = java.time.Duration.between(now,deadline);

                long days = diff.toDays();
                long hours = diff.toHoursPart();
                long minutes = diff.toMinutesPart();
                long seconds = diff.toSecondsPart();
                countdownLabel.setText(String.format("Time left %s : %02dgg %02dh %02dm %02ds", actionName, days, hours, minutes, seconds));
            }
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    public void setSelectedWorkplace(WorkplaceBean wp) {
        this.selectedWorkplace = wp;
        this.workplaceTitleLabel.setText(wp.getWorkplaceName());
        // Qui potrai caricare i turni specifici di questo workplace
        buildDynamicTable();

    }

    private void buildDynamicTable() {
        try {
            resetAndSetupGrid();
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
            boolean isLocked = ctx.status.equals(LOCKED_STATUS);
            return new OwnerCellFactory(isLocked,this::handleRemoveWorker);
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
        ManageShiftsAC manageAC = new ManageShiftsAC();
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

        } catch (BaseException e) {
            SceneManager.getInstance().showErrorAlert("Errore", e.getMessage());
        }
    }

    private void savePartShifts(UserBean loggedUser, WorkplaceBean wp, List<AvailabilityBean> availabilityBeans, String day, String fullTime,String weekIdForDb) {
        String[] timeParts = fullTime.split("-");
        if (timeParts.length >= 2) {
            String start = timeParts[0].trim();
            String end = timeParts[1].trim(); // "00:00-01:00"


            AvailabilityBean bean = new AvailabilityBean(
                    loggedUser.getEmail(),
                    wp.getWorkplaceName(),
                    day,
                    start,
                    end,
                    weekIdForDb
            );
            availabilityBeans.add(bean);
        }
    }

    public void onPublic() {
        WorkplaceBean wp = SessionContext.getInstance().getLoggedWorkplace();

        if(wp == null){
            ErrorViewManager.showError(errorlbl,"workplace is null\n");
            return;
        }
        try{
            String resultMSG = pubAc.handlePublishAction(wp, this.currentWeekId);
            SceneManager.getInstance().showInfoAlert("Shifts Operation", resultMSG);
            buildDynamicTable();
        }catch(BaseException e){
            SceneManager.getInstance().showErrorAlert(TECHNICAL_ERROR,e.getMessage());
        }
    }

    @FXML
    private void handleNextWeek() {
        if(weekOffset<2){
            weekOffset++;
            updateView();
        }else{
            ErrorViewManager.showError(errorlbl,"You can give availability until next week at the latest");
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
        ManageShiftsAC manageAC = new ManageShiftsAC();
        this.currentWeekId = manageAC.calculateWeekId(weekOffset);
        // Aggiorna la label per far capire all'utente dove si trova
        selectedCellsMap.clear();
        lblWeekDisplay.setText("Week: "+ manageAC.getWeekRangeString(weekOffset));
        // Ridisegna la tabella (questo metodo ora userà currentWeekId per le chiavi)
        buildDynamicTable();
    }

    private void handleRemoveWorker(String cellKey,String email){
        try{
            String[] parts = cellKey.split("_");
            if(parts.length >= 4){
                String day = parts[2];
                String fullTime = parts[3];
                ManageShiftsAC manageAC = new ManageShiftsAC();
                manageAC.removeWorkerFromShift(email,selectedWorkplace.getWorkplaceName(),currentWeekId,day,fullTime);
                buildDynamicTable();
            }
        }catch(BaseException e){
            ErrorViewManager.showError(errorlbl,e.getMessage());
        }
    }

}
