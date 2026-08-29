package com.ispw.tryeshifts.graphcontroller.gui;

import com.ispw.tryeshifts.graphcontroller.gui.component.NavbarGC;
import com.ispw.tryeshifts.graphcontroller.gui.utilities.NavPage;
import com.ispw.tryeshifts.graphcontroller.gui.utilities.SceneManager;
import com.ispw.tryeshifts.appcontroller.*;
import com.ispw.tryeshifts.session.SessionContext;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.exception.*;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Window;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;


public class HomeGC {
    private static final Logger LOGGER = Logger.getLogger(HomeGC.class.getName());
    private static final String TECHNICAL_ERROR = "Technical error: ";
    private UserBean loggedUser;
    @FXML private ListView<WorkplaceBean> workplaceListView;
    @FXML private GridPane shiftsGrid; // Corrisponde a fx:id="shiftsGrid"
    @FXML private VBox vboxWorkplaceLegend;
    @FXML private TextField searchField;
    private final Map<String, String> workplaceColors = new HashMap<>();
    @FXML private Label lblWeekDisplay;
    @FXML private NavbarGC navbarController;
    private int weekOffset = 0;
    private String currentWeekId;

    private final AccessWorkplaceAC accessWorkplaceAC = new AccessWorkplaceAC();
    private final ManageShiftsAC manageShiftsAC = new ManageShiftsAC();
    private final SearchWorkplacesAC searchWorkplacesAC = new SearchWorkplacesAC();


    public void initialize() {
        this.loggedUser = SessionContext.getInstance().getLoggeduser();
        // Aggiungiamo un listener: ogni volta che il testo cambia, cerchiamo
        if(navbarController != null){
            navbarController.setActivePage(NavPage.HOME);
        }
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            handleSearch(newValue);
        });

        setupWorkplaceListView();
        this.weekOffset = 0;
        this.currentWeekId = manageShiftsAC.calculateWeekId(weekOffset);
        if(lblWeekDisplay!=null){
            lblWeekDisplay.setText("Week: "+ manageShiftsAC.getWeekRangeString(weekOffset));
        }
        if (this.loggedUser != null) {
            refreshWorkplaceList();
            handleSearch("");
            currentWeekId = manageShiftsAC.calculateWeekId(weekOffset);
            buildHomeTable(shiftsGrid, this.loggedUser.getEmail(), currentWeekId);
        }
    }

    public void showJoinConfirmation(String workplaceName){

        boolean response = SceneManager.getInstance().showConfirmationAlert("Access Request",
                "Do you want to access: "+workplaceName+"?",
                "Sending the request, the owner will decide your fate");
            if(response){
                try{
                    new ManageMembersAC().requestJoin(this.loggedUser,workplaceName);
                    SceneManager.getInstance().showInfoAlert("Success","Correctly sent the request");
                }catch(EntityNotFoundException _){
                    SceneManager.getInstance().showErrorAlert("Error Workplace","Could not find:  "+workplaceName);
                }catch(BaseException e){
                    SceneManager.getInstance().showErrorAlert(TECHNICAL_ERROR,e.getMessage());
                }
            }
    }

    public void handleWorkplaceSelection(String workplaceName) {
        try {
            WorkplaceBean wpBean = accessWorkplaceAC.canAccess(this.loggedUser, workplaceName);
            SessionContext.getInstance().setLoggedWorkplace(wpBean);
            SceneManager.getInstance().switchScene("Shifts.fxml", "Turni", 900, 600);
        }catch (UserNotMemberException _) {
            showJoinConfirmation(workplaceName);
        }catch (MembershipPendingException _){
            SceneManager.getInstance().showErrorAlert("Pendant request","You have already sent a request to join: "+workplaceName);
        }catch(EntityNotFoundException _){
            SceneManager.getInstance().showErrorAlert("Workplace Error","Couldn't find the workplace: "+workplaceName);
        }catch(BaseException e){
            SceneManager.getInstance().showErrorAlert(TECHNICAL_ERROR, e.getMessage());
        }
    }

    public void handleSearch(String query) {
        try {
            List<WorkplaceBean> result;
            if (query == null || query.isEmpty()) {
                result = searchWorkplacesAC.getAllWorkplaces();
            } else {
                result = searchWorkplacesAC.searchByName(query);
            }
            String msg = "Risultati trovati: " + result.size();
            LOGGER.info(msg); // DEBUG

            workplaceListView.getItems().clear();
            for (WorkplaceBean wp : result) {
                msg = "Aggiungo: " + wp.getWorkplaceName();
                LOGGER.info(msg); // DEBUG
                workplaceListView.getItems().add(wp);
            }
        }catch(DataFetchException _){
            SceneManager.getInstance().showErrorAlert("Errore di Connessione",
                    "Non è stato possibile recuperare i dati. Riprova più tardi.");
            workplaceListView.getItems().clear();
        }catch(BaseException e){
            SceneManager.getInstance().showErrorAlert(TECHNICAL_ERROR, e.getMessage());
            workplaceListView.getItems().clear();
        }
    }

    public void buildHomeTable(GridPane grid, String userEmail, String weekId){
        grid.getChildren().clear();
        grid.getColumnConstraints().clear();
        grid.getRowConstraints().clear();
        try {
            Map<String, Object> data = manageShiftsAC.getHomeScheduleData(userEmail, weekId);
            Map<String, String> assignments = (Map<String, String>) data.get("assignments");
            TreeSet<String> slots = (TreeSet<String>) data.get("slots");

            String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

            ColumnConstraints timeCol = new ColumnConstraints();
            timeCol.setHgrow(Priority.NEVER);
            timeCol.setPrefWidth(100);
            grid.getColumnConstraints().add(timeCol);
            for (int i = 0; i < days.length; i++) {
                ColumnConstraints dayCol = new ColumnConstraints();
                dayCol.setHgrow(Priority.ALWAYS); // Questa riga permette l'espansione
                dayCol.setPercentWidth(100.0 / (days.length + 1)); // Distribuzione uniforme
                dayCol.setHalignment(HPos.CENTER);
                grid.getColumnConstraints().add(dayCol);
            }

            for (int i = 0; i < days.length; i++) {
                Label lblDay = new Label(days[i]);
                lblDay.setStyle("-fx-font-weight: bold; -fx-text-fill: #4B4488;");
                lblDay.setMaxWidth(Double.MAX_VALUE);
                lblDay.setMaxHeight(Double.MAX_VALUE); // Aggiungi questo
                lblDay.setAlignment(Pos.CENTER);

                grid.add(lblDay, i + 1, 0);
                // Forza l'allineamento della cella nella griglia
                GridPane.setValignment(lblDay, VPos.CENTER);
            }
            int rowIndex = 1;
            for (String slot : slots) {
                // Configurazione riga: permette l'espansione verticale se vuoi
                RowConstraints row = new RowConstraints();
                row.setVgrow(Priority.ALWAYS);
                grid.getRowConstraints().add(row);

                Label lblSlot = new Label(slot);
                lblSlot.setAlignment(Pos.CENTER);
                grid.add(lblSlot, 0, rowIndex);

                for (int col = 0; col < days.length; col++) {
                    String cellKey = days[col] + "_" + slot;
                    StackPane cell = new StackPane();

                    // RIMOSSO prefSize fisso: ora usiamo maxWidth/Height per farla crescere
                    cell.setMaxWidth(Double.MAX_VALUE);
                    cell.setMaxHeight(Double.MAX_VALUE);
                    GridPane.setMargin(cell, new Insets(2, 2, 2, 2));
                    if (assignments.containsKey(cellKey)) {
                        String wpName = assignments.get(cellKey);
                        cell.setStyle("-fx-background-color: " + getColorForWorkplace(wpName) + "; -fx-background-radius: 5;");
                    } else {
                        cell.setStyle("-fx-border-color: #eeeeee; -fx-border-width: 0.5;");
                    }
                    grid.add(cell, col + 1, rowIndex);
                    GridPane.setHgrow(cell, Priority.ALWAYS);
                    GridPane.setVgrow(cell, Priority.ALWAYS);
                }
                rowIndex++;
            }
        }catch(BaseException e){
            LOGGER.log(Level.SEVERE, "Errore durante la generazione della tabella Home", e);

            Label errorLabel = new Label("Impossibile caricare i turni: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-style: italic;");
            grid.add(errorLabel, 0, 0, 8, 1);
        }
    }

    public String getColorForWorkplace(String wpName) {
        // Se non abbiamo ancora assegnato un colore a questo locale, ne scegliamo uno dalla lista
        if (!workplaceColors.containsKey(wpName)) {
            String[] palette = {"#4CAF50", "#2196F3", "#9C27B0", "#FF9800", "#E91E63", "#795548"};
            int index = workplaceColors.size() % palette.length;
            workplaceColors.put(wpName, palette[index]);
        }
        return workplaceColors.get(wpName);
    }

    private void refreshWorkplaceList() {
        vboxWorkplaceLegend.getChildren().clear();
        try {
            List<WorkplaceBean> workplaces = searchWorkplacesAC.getWorkplacesByEmail(this.loggedUser.getEmail());
            if (workplaces == null || workplaces.isEmpty()) {
                vboxWorkplaceLegend.getChildren().add(new Label("No workplaces joined yet."));
                return;
            }
            for (WorkplaceBean wp : workplaces) {
                //Contenitore Principale
                VBox card = new VBox(5);
                card.setPadding(new Insets(10));
                card.setStyle("-fx-background-color: white; -fx-border-color: #dddddd; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;");


                //Efetto hover
                card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #cccccc; -fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;"));
                card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dddddd; -fx-border-radius: 8; -fx-background-radius: 8; "));
                card.setOnMouseClicked(e -> handleWorkplaceSelection(wp.getWorkplaceName()));

                //HBox name and color
                HBox topBox = new HBox(10);
                topBox.setAlignment(Pos.CENTER_LEFT);
                Rectangle rect = new Rectangle(12,12);
                rect.setArcHeight(4); rect.setArcWidth(4);
                rect.setFill(Color.web(getColorForWorkplace(wp.getWorkplaceName())));

                Label nameLabel1 = new Label(wp.getWorkplaceName());
                nameLabel1.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                topBox.getChildren().addAll(rect, nameLabel1);

                //HBox address e giorni
                HBox bottomBox = new HBox();
                bottomBox.setAlignment(Pos.BOTTOM_LEFT);

                Label addressLabel1 = new Label(wp.getAddress());
                addressLabel1.setStyle("-fx-font-size: 11px;-fx-text-fill: #666666");


                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                List<String> activeDayStr = wp.getSelectedDays();
                String theOneString = (activeDayStr!=null && !activeDayStr.isEmpty()) ? String.join(", ",activeDayStr) : "No days for this workplace";
                Label daysLabel = new Label("Days: "+theOneString);
                daysLabel.setStyle("-fx-font-size: 11px; -fx-font-style: italic; -fx-text-fill: #888888");

                bottomBox.getChildren().addAll(addressLabel1,spacer, daysLabel);

                card.getChildren().addAll(topBox, bottomBox);
                vboxWorkplaceLegend.getChildren().add(card);
            }
        } catch (BaseException e) {
            SceneManager.getInstance().showErrorAlert(TECHNICAL_ERROR,"Error during the Creation of the Workplace List");
            LOGGER.severe(e.getMessage());
        }

    }

    public void newWpClicked(){
        if (this.loggedUser == null) {
            LOGGER.warning("ERRORE: Impossibile aprire il popup, loggedUser è null!");
            return;
        }

        NewWorkplaceGC popupController = (NewWorkplaceGC) SceneManager.getInstance()
                .showModalDialog("NewWorkplace.fxml", "Configura Nuovo Workplace");


        // 2. Passiamo l'utente loggato al controller del popup
        if (popupController != null) {
            popupController.setLoggedUser(this.loggedUser);

            // RECUPERA LA FINESTRA (Window/Stage) E AGGIUNGI IL LISTENER
            // Usiamo lo shortcut tramite il controller
            Window window = popupController.getNameField().getScene().getWindow();

            window.setOnHiding(e -> {
                searchField.clear();
                //refreshWorkplaceList();
                refreshAllData();
                //handleSearch(""); //  pulisce la ricerca per mostrare il nuovo item
            });

        } else {
            LOGGER.severe("Errore nel caricamento del popup");
        }

    }


    public void refreshAllData() {
        if (this.loggedUser != null) {
            // Aggiorna i tuoi workplace (quelli con i colori)
            refreshWorkplaceList();

            // Aggiorna la ricerca globale (per vedere il nuovo workplace appena creato)
            handleSearch(searchField.getText());

            // Aggiorna la tabella dei turni
            buildHomeTable(shiftsGrid, this.loggedUser.getEmail(), manageShiftsAC.calculateWeekId(weekOffset));
        }
    }

    @FXML
    private void handleNextWeek(){
        weekOffset++;
        updateUI();
    }

    @FXML
    private void handlePrevWeek(){
        weekOffset--;
        updateUI();
    }

    private void updateUI() {
        this.currentWeekId = manageShiftsAC.calculateWeekId(weekOffset);
        lblWeekDisplay.setText(manageShiftsAC.getWeekRangeString(weekOffset));
        buildHomeTable(shiftsGrid, this.loggedUser.getEmail(), this.currentWeekId);
    }

    private void setupWorkplaceListView() {
        workplaceListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                handleWorkplaceSelection(newVal.getWorkplaceName());
            }
        });

        workplaceListView.setCellFactory(param -> new WorkplaceListCell());
    }

    private static class WorkplaceListCell extends ListCell<WorkplaceBean> {
        @Override
        protected void updateItem(WorkplaceBean wp, boolean empty) {
            super.updateItem(wp, empty);
            if (empty || wp == null) {
                setText(null);
                setGraphic(null);
            }else{
                VBox card = new VBox(5);
                card.setPadding(new Insets(5));

                Label nameLabel = new Label(wp.getWorkplaceName());
                nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                HBox bottomBox = new HBox();
                bottomBox.setAlignment(Pos.BOTTOM_LEFT);

                Label addressLabel = new Label(wp.getAddress() != null ? wp.getAddress() : "Nessun indirizzo");
                addressLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                List<String> activeDayStr = wp.getSelectedDays();
                String theOneString = (activeDayStr!=null && !activeDayStr.isEmpty()) ? String.join(", ",activeDayStr) : "No days for this workplace";
                Label daysLabel = new Label("Days: "+theOneString);
                daysLabel.setStyle("-fx-font-size: 11px; -fx-font-style: italic; -fx-text-fill: #888888");

                bottomBox.getChildren().addAll(addressLabel, spacer, daysLabel);
                card.getChildren().addAll(nameLabel, bottomBox);

                setGraphic(card);
            }
        }
    }



}
