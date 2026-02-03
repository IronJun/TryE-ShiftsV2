package com.ispw.tryeshifts.graphcontroller.javafx;

import com.ispw.tryeshifts.SceneManager;
import com.ispw.tryeshifts.appcontroller.*;
import com.ispw.tryeshifts.bean.SessionContext;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.entity.Workplace;
import com.ispw.tryeshifts.excpetion.*;
import com.ispw.tryeshifts.graphcontroller.javafx.utilities.ErrorViewManager;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;


public class HomeGC {
    private static final Logger LOGGER = Logger.getLogger(HomeGC.class.getName());
    private static final String TECNICAL_ERROR = "Technical error: ";
    private UserBean loggedUser;
    @FXML private ListView<String> workplaceListView;
    @FXML private GridPane shiftsGrid; // Corrisponde a fx:id="shiftsGrid"
    @FXML private VBox vboxWorkplaceLegend;
    @FXML private TextField searchField;
    @FXML private Label errorlbl;
    private final Map<String, String> workplaceColors = new HashMap<>();
    @FXML Label lblWeekDisplay;
    @FXML private Button btnNextWeek;
    @FXML private Button btnPrevWeek;
    private int weekOffset = 0;
    private String currentWeekId;
    private String msg="";

    public void initialize() {
        ErrorViewManager.setupAutoHide(errorlbl);
        this.loggedUser = SessionContext.getInstance().getLoggeduser();
        // Aggiungiamo un listener: ogni volta che il testo cambia, cerchiamo
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            handleSearch(newValue);
        });

        workplaceListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                handleGlobalSearchSelection(newVal);
            }
        });
        this.weekOffset = 0;
        this.currentWeekId = ManageShiftsAC.calculateWeekId(weekOffset);
        if(lblWeekDisplay!=null){
            lblWeekDisplay.setText("Settimana: "+ ManageShiftsAC.getWeekRangeString(weekOffset));
        }
        if (this.loggedUser != null) {
            refreshWorkplaceList();
            handleSearch("");
            currentWeekId = getCurrentWeekId();
            buildHomeTable(shiftsGrid, this.loggedUser.getEmail(), currentWeekId);
        }
    }

    private String getCurrentWeekId() {
        java.time.LocalDate now = java.time.LocalDate.now();
        java.time.temporal.TemporalField woy = java.time.temporal.WeekFields.of(java.util.Locale.getDefault()).weekOfWeekBasedYear();
        int weekNumber = now.get(woy);
        return now.getYear() + "_" + String.format("%02d", weekNumber);
    }

    public void handleGlobalSearchSelection(String workplaceName) {
        try{
            WorkplaceBean fullWp = AccessWorkplaceAC.canAccess(this.loggedUser, workplaceName);
            SessionContext.getInstance().setLoggedWorkplace(fullWp);
            SceneManager.getInstance().switchScene("Shifts.fxml", "Turni", 900, 600);
        }catch(UserNotMemberException _) {
            showJoinConfirmation(workplaceName);
        }catch(MembershipPendingException _){
            ErrorViewManager.ScreenError("richiesta pendente","hai già inviato una richiesta di accesso al workplace "+workplaceName+". Attendi la sua conferma");
        }catch (EntityNotFoundException _){
            ErrorViewManager.ScreenError("Errore Workplace","Impossibile trovare il workplace "+workplaceName);
        }catch (BaseException _){
            ErrorViewManager.ScreenError("unkown error","ask the programmer");
        }
    }

    public void showJoinConfirmation(String workplaceName){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Richiesta di accesso");
        alert.setHeaderText("Vuoi inviare una richiesta di accesso al workplace "+workplaceName+"?");
        alert.setContentText("Inviando la richiesta, dovrai attendere l'owner che accetti");

        alert.showAndWait().ifPresent(response -> {
            if(response == ButtonType.OK){
                try{
                    ManageMembersAC ac = new ManageMembersAC();
                    ac.requestJoin(this.loggedUser,workplaceName);
                    SceneManager.getInstance().showInfoAlert("Success","Correctly sent the request");
                }catch(EntityNotFoundException _){
                    ErrorViewManager.ScreenError("Errore Workplace 2","Impossibile trovare il workplace "+workplaceName);
                }catch(BaseException _){
                    ErrorViewManager.ScreenError(TECNICAL_ERROR,"Impossibile inviare la richiesta");
                }

            }
        });
    }

    public void handleWorkplaceSelection(String workplaceName) {
        try {
            WorkplaceBean wpBean = AccessWorkplaceAC.canAccess(this.loggedUser, workplaceName);
            SessionContext.getInstance().setLoggedWorkplace(wpBean);
            SceneManager.getInstance().switchScene("Shifts.fxml", "Turni", 900, 600);
        }catch (UserNotMemberException _) {
            LOGGER.info("L'utente non è membro. Mostro popup di iscrizione per: " + workplaceName);
            showJoinConfirmation(workplaceName);
        }catch (MembershipPendingException e){
            ErrorViewManager.ScreenError("Richiesta Pendente", e.getMessage());
        }catch(EntityNotFoundException _){
            ErrorViewManager.ScreenError("Errore Workplace", "il workpalce selezionato non esiste");
        }catch(BaseException e){
            ErrorViewManager.ScreenError(TECNICAL_ERROR, e.getMessage());
        }
    }

    public void handleSearch(String query) {
        try {
            List<WorkplaceBean> result;
            if (query == null || query.isEmpty()) {
                result = SearchWorkplacesAC.getAllWorkplaces();
            } else {
                result = SearchWorkplacesAC.searchByName(query);
            }
            msg = "Risultati trovati: " + result.size();
            LOGGER.info(msg); // DEBUG

            workplaceListView.getItems().clear();
            for (WorkplaceBean wp : result) {
                msg = "Aggiungo: " + wp.getWorkplaceName();
                LOGGER.info(msg); // DEBUG
                workplaceListView.getItems().add(wp.getWorkplaceName());
            }
        }catch(DataFetchException e){
            LOGGER.log(Level.SEVERE, "Errore durante la ricerca", e);
            ErrorViewManager.ScreenError("Errore di Connessione",
                    "Non è stato possibile recuperare i dati. Riprova più tardi.");
            workplaceListView.getItems().clear();
        }catch(BaseException e){
            ErrorViewManager.ScreenError(TECNICAL_ERROR, e.getMessage());
            workplaceListView.getItems().clear();
        }
    }

    public void buildHomeTable(GridPane grid, String userEmail, String weekId){
        grid.getChildren().clear();
        grid.getColumnConstraints().clear();
        grid.getRowConstraints().clear();
        try {
            Map<String, Object> data = ManageShiftsAC.getHomeScheduleData(userEmail, weekId);
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
            List<Workplace> workplaces = ManageShiftsAC.getUserWorkplaces(this.loggedUser.getEmail());
            if (workplaces == null || workplaces.isEmpty()) {
                vboxWorkplaceLegend.getChildren().add(new Label("No workplaces joined yet."));
                return;
            }
            for (Workplace wp : workplaces) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(5));
                row.setStyle("-fx-cursor: hand; -fx-background-radius: 5;");

                // Effetto hover per far capire che è cliccabile
                row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #f0f0f0; -fx-cursor: hand;"));
                row.setOnMouseExited(e -> row.setStyle("-fx-background-color: transparent;"));

                // Azione al click (sostituisce il vecchio listener della ListView)
                row.setOnMouseClicked(e -> handleWorkplaceSelection(wp.getName()));

                // Quadratino colorato
                Rectangle rect = new Rectangle(15, 15);
                rect.setArcHeight(5); rect.setArcWidth(5);
                rect.setFill(Color.web(getColorForWorkplace(wp.getName())));

                Label name = new Label(wp.getName());

                row.getChildren().addAll(rect, name);
                vboxWorkplaceLegend.getChildren().add(row);
            }
        } catch (BaseException _) {
            ErrorViewManager.ScreenError(TECNICAL_ERROR,"Impossibile recuperare i workplace");
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
            LOGGER.info("utente passato al popup: " + this.loggedUser.getEmail());
            popupController.getNameField().getScene().getWindow().setOnHiding(e -> {
                LOGGER.info("DEBUG HOME: Popup in chiusura, eseguo il refresh...");
                refreshWorkplaceList();
            });
        } else {
            LOGGER.info("Errore nel caricamento del popup");
        }

        refreshAllData();

    }

    public void onLogoutClicked() {
        if(SessionContext.getInstance().logoutConfirmation()){
            SessionContext.getInstance().clearPreferences();
            SceneManager.getInstance().showInfoAlert("Logout", "");
            SceneManager.getInstance().switchScene("Login.fxml", "Login", 900, 600);
            LOGGER.info("Logout effettuato");
        }

    }

    public void onShiftsclicked() {
        WorkplaceBean wp = SessionContext.getInstance().getLoggedWorkplace();
        if (wp != null) {
            handleWorkplaceSelection(wp.getWorkplaceName());
        } else {
            ErrorViewManager.showError(errorlbl,"Select a workplace to see its shifts, or create a new one");
        }
    }

    public void onWorkersclicked() {
        WorkplaceBean wp = SessionContext.getInstance().getLoggedWorkplace();
        if(wp!=null){
            SceneManager.getInstance().switchScene("Workers.fxml", "Gestione Membri", 900, 600);
        }else{
            ErrorViewManager.showError(errorlbl,"Select a workplace to see its workers");
        }
    }

    public void onSettingsclicked() {
        SceneManager.getInstance().switchScene("Settings.fxml", "Gestione Membri", 900, 600);
    }

    public void refreshAllData() {
        if (this.loggedUser != null) {
            // Aggiorna i tuoi workplace (quelli con i colori)
            refreshWorkplaceList();

            // Aggiorna la ricerca globale (per vedere il nuovo workplace appena creato)
            handleSearch(searchField.getText());

            // Aggiorna la tabella dei turni
            buildHomeTable(shiftsGrid, this.loggedUser.getEmail(), getCurrentWeekId());
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
        this.currentWeekId = ManageShiftsAC.calculateWeekId(weekOffset);
        lblWeekDisplay.setText(ManageShiftsAC.getWeekRangeString(weekOffset));
        buildHomeTable(shiftsGrid, this.loggedUser.getEmail(), this.currentWeekId);
    }
}
