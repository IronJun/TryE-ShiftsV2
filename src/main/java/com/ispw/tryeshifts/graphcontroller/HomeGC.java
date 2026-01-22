package com.ispw.tryeshifts.graphcontroller;

import com.ispw.tryeshifts.SceneManager;
import com.ispw.tryeshifts.appcontroller.*;
import com.ispw.tryeshifts.bean.SessionContext;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.entity.Workplace;
import com.ispw.tryeshifts.excpetion.DAOException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;
import com.ispw.tryeshifts.excpetion.MembershipPendingException;
import com.ispw.tryeshifts.excpetion.UserNotMemberException;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Logger;


public class HomeGC {
    private static final Logger LOGGER = Logger.getLogger(HomeGC.class.getName());
    private UserBean loggedUser;
    @FXML private ListView<String> workplaceListView;
    @FXML private GridPane shiftsGrid; // Corrisponde a fx:id="shiftsGrid"
    @FXML private VBox vboxWorkplaceLegend;
    @FXML private TextField searchField;
    @FXML private ListView<String> ownedWorkplaceList;
    private final Map<String, String> workplaceColors = new HashMap<>();

    public void initialize() throws DAOException {

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

        if (this.loggedUser != null) {
            refreshWorkplaceList();
            handleSearch("");
            String currentWeekId = getCurrentWeekId();
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
            SceneManager.getInstance().showInfoAlert("richiesta pendente","hai già inviato una richiesta di accesso al workplace "+workplaceName+". Attendi la sua conferma");
        }catch (EntityNotFoundException | DAOException _){
            SceneManager.getInstance().showErrorAlert("Errore Workplace","Impossibile trovare il workplace "+workplaceName);
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
                    SceneManager.getInstance().showErrorAlert("Errore Workplace 2","Impossibile trovare il workplace "+workplaceName);
                }catch(DAOException _){
                    SceneManager.getInstance().showErrorAlert("Errore tecnico","Impossibile inviare la richiesta");
                }

            }
        });
    }

    public void handleWorkplaceSelection(String workplaceName) {
        try {
            WorkplaceBean wpBean = AccessWorkplaceAC.canAccess(this.loggedUser, workplaceName);
            SessionContext.getInstance().setLoggedWorkplace(wpBean);
            SceneManager.getInstance().switchScene("Shifts.fxml", "Turni", 900, 600);
        } catch (Exception e) {
            if(e.getMessage().equals("Non sei membro di questo workplace")){
                LOGGER.info("vuoi inviare richiesta?");
            } // ecc...
        }

    }

    public void handleSearch(String query) {
        SearchWorkplacesAC searchWorkplacesAC = new SearchWorkplacesAC();
        try {
            List<WorkplaceBean> result;
            if (query == null || query.isEmpty()) {
                result = searchWorkplacesAC.getAllWorkplaces();
            } else {
                result = searchWorkplacesAC.searchByName(query);
            }

            System.out.println("Risultati trovati: " + result.size()); // DEBUG

            workplaceListView.getItems().clear();
            for (WorkplaceBean wp : result) {
                System.out.println("Aggiungo: " + wp.getWorkplaceName()); // DEBUG
                workplaceListView.getItems().add(wp.getWorkplaceName());
            }
        }catch(DAOException e){
            e.printStackTrace(); // Molto meglio per il debug rispetto a _
            SceneManager.getInstance().showErrorAlert("Errore tecnico","Impossibile eseguire la ricerca");
            workplaceListView.getItems().clear();
        }
    }

    public void buildHomeTable(GridPane grid, String userEmail, String weekId) throws DAOException {
        grid.getChildren().clear();
        Map<String, Object> data = ManageShiftsAC.getHomeScheduleData(userEmail, weekId);

        Map<String, String> assignments = (Map<String, String>) data.get("assignments");
        TreeSet<String> slots = (TreeSet<String>) data.get("slots");

        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        System.out.println("DEBUG - Assignments caricati: " + assignments.keySet());
        for (int i = 0; i < days.length; i++) {
            Label lblDay = new Label(days[i]);
            lblDay.setStyle("-fx-font-weight: bold; -fx-text-fill: #4B4488;");
            lblDay.setMaxWidth(Double.MAX_VALUE);
            lblDay.setAlignment(Pos.CENTER);

            // Aggiunge alla colonna i+1, riga 0
            grid.add(lblDay, i + 1, 0);
        }
        int rowIndex = 1;
        for(String slot : slots){
            grid.add(new Label(slot),0 , rowIndex);
            for (int col = 0; col < days.length; col++) {
                String cellKey = days[col] + "_" + slot;
                System.out.println("TROVATO: " + cellKey);
                StackPane cell = new StackPane();
                cell.setPrefSize(80, 40);

                if (assignments.containsKey(cellKey)) {
                    String wpName = assignments.get(cellKey);
                    cell.setStyle("-fx-background-color: " + getColorForWorkplace(wpName) + ";");
                } else {
                    cell.setStyle("-fx-border-color: #eeeeee;");
                }
                grid.add(cell, col + 1, rowIndex);
            }
            rowIndex++;
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
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void newWpClicked() throws DAOException {
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
        this.loggedUser = null;
        SceneManager.getInstance().switchScene("Login.fxml", "Login", 900, 600);
        LOGGER.info("Logout effettuato");
    }

    public void onShiftsclicked() {
        WorkplaceBean wp = SessionContext.getInstance().getLoggedWorkplace();
        if (wp != null) {
            handleWorkplaceSelection(wp.getWorkplaceName());
        } else {
            LOGGER.info("Seleziona un workplace dai tuoi per vedere i turni");
        }
    }

    public void onWorkersclicked() {
        WorkplaceBean wp = SessionContext.getInstance().getLoggedWorkplace();
        if(wp!=null){
            SceneManager.getInstance().switchScene("Workers.fxml", "Gestione Membri", 900, 600);
        }else{
            SceneManager.getInstance().showInfoAlert("Seleziona un workplace", "Per vedere i membri devi prima selezionare un workplace");
        }
    }

    public void onSettingsclicked() {
        WorkplaceBean wp = SessionContext.getInstance().getLoggedWorkplace();
        if(wp!=null){
            SceneManager.getInstance().switchScene("Settings.fxml", "Gestione Membri", 900, 600);
        }else{
            SceneManager.getInstance().showInfoAlert("Seleziona un workplace", "Per vedere i membri devi prima selezionare un workplace");
        }
    }

    public void refreshAllData() throws DAOException {
        if (this.loggedUser != null) {
            // Aggiorna i tuoi workplace (quelli con i colori)
            refreshWorkplaceList();

            // Aggiorna la ricerca globale (per vedere il nuovo workplace appena creato)
            handleSearch(searchField.getText());

            // Aggiorna la tabella dei turni
            buildHomeTable(shiftsGrid, this.loggedUser.getEmail(), getCurrentWeekId());
        }
    }

}
