package com.ispw.tryeshiftsv2.graphController;

import com.ispw.tryeshiftsv2.SceneManager;
import com.ispw.tryeshiftsv2.appController.AccessWorkplaceAC;
import com.ispw.tryeshiftsv2.appController.ManageMembersAC;
import com.ispw.tryeshiftsv2.appController.SearchWorkplacesAC;
import com.ispw.tryeshiftsv2.appController.getOwnedWorkplaceAC;
import com.ispw.tryeshiftsv2.bean.SessionContext;
import com.ispw.tryeshiftsv2.bean.UserBean;
import com.ispw.tryeshiftsv2.bean.WorkplaceBean;
import com.ispw.tryeshiftsv2.excpetion.DAOException;
import com.ispw.tryeshiftsv2.excpetion.EntityNotFoundException;
import com.ispw.tryeshiftsv2.excpetion.MembershipPendingException;
import com.ispw.tryeshiftsv2.excpetion.UserNotMemberException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.util.List;


public class HomeGC {
    private UserBean loggedUser;

    @FXML
    private ListView<String> workplaceListView;

    @FXML
    private TextField searchField;

    @FXML
    private ListView<String> ownedWorkplaceList;

    public void initialize() {

        this.loggedUser = SessionContext.getInstance().getLoggeduser();

        // Aggiungiamo un listener: ogni volta che il testo cambia, cerchiamo
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            handleSearch(newValue);
        });
        ownedWorkplaceList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                handleWorkplaceSelection(newValue);
            }
        });

        workplaceListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                handleGlobalSearchSelection(newVal);
            }
        });

        if (this.loggedUser != null) {
            refreshWorkplaceList();
        }
    }

    public void handleGlobalSearchSelection(String workplaceName) {
        try{
            WorkplaceBean fullWp = AccessWorkplaceAC.canAccess(this.loggedUser, workplaceName);
            SessionContext.getInstance().setLoggedWorkplace(fullWp);
            SceneManager.getInstance().switchScene("Shifts.fxml", "Turni", 900, 600);
        }catch(UserNotMemberException e) {
            showJoinConfirmation(workplaceName);
        }catch(MembershipPendingException e){
            SceneManager.getInstance().showInfoAlert("richiesta pendente","hai già inviato una richiesta di accesso al workplace "+workplaceName+". Attendi la sua conferma");
        }catch (EntityNotFoundException | DAOException e){
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
                }catch(EntityNotFoundException e){
                    SceneManager.getInstance().showErrorAlert("Errore Workplace 2","Impossibile trovare il workplace "+workplaceName);
                }catch(DAOException e){
                    SceneManager.getInstance().showErrorAlert("Errore tecnico","Impossibile inviare la richiesta");
                }

            }
        });
    }


    public void handleWorkplaceSelection(String workplaceName) {
        try{
            //AccessWorkplaceAC ac = new AccessWorkplaceAC();
            WorkplaceBean wpBean = AccessWorkplaceAC.canAccess(this.loggedUser, workplaceName);
            SessionContext.getInstance().setLoggedWorkplace(wpBean);
            SceneManager.getInstance().switchScene("Shifts.fxml", "Turni", 900, 600);
        }catch (Exception e){
            if(e.getMessage().equals("Non sei membro di questo workplace")){
                System.out.println("vuoi inviare richiesta?");
            }else if(e.getMessage().equals("Non sei ancora stato accettato da questo workplace")){
                System.out.println("richiesta già inviata");
            }else{
                System.out.println("ERRORE: "+e.getMessage());
            }
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

            workplaceListView.getItems().clear();
            for (WorkplaceBean wp : result) {
                workplaceListView.getItems().add(wp.getWorkplaceName());
            }
        }catch(DAOException e){
            SceneManager.getInstance().showErrorAlert("Errore tecnico","Impossibile eseguire la ricerca");
            workplaceListView.getItems().clear();
        }
    }

    public void setLoggedUser(UserBean user) {
        this.loggedUser = user;
        if (ownedWorkplaceList != null) {
            refreshWorkplaceList();
        }
    }

    private void refreshWorkplaceList() {
        if (this.loggedUser == null) {
            System.out.println("DEBUG HOME: Impossibile fare refresh, loggedUser è NULL");
            return;
        }
        try{
            SearchWorkplacesAC searchAC = new SearchWorkplacesAC();
            List<WorkplaceBean> allWorkplaces = searchAC.getAllWorkplaces();
            //System.out.println("DEBUG HOME: Trovati "+myWorkplaces.size()+" workplace per "+this.loggedUser.getEmail());

            workplaceListView.getItems().clear();
            for (WorkplaceBean wp : allWorkplaces) {
                workplaceListView.getItems().add(wp.getWorkplaceName());
            }
            getOwnedWorkplaceAC ac = new getOwnedWorkplaceAC();
            List<WorkplaceBean> myWorkplaces = ac.getForUser(this.loggedUser);
            ownedWorkplaceList.getItems().clear();
            for (WorkplaceBean wp : myWorkplaces) {
                ownedWorkplaceList.getItems().add(wp.getWorkplaceName());
            }
        }catch(DAOException e){
            SceneManager.getInstance().showErrorAlert("Errore Workplace 3","Impossibile recuperare i workplace del loggedUser");
        }
    }

    public void newWpClicked(ActionEvent event) {

        if (this.loggedUser == null) {
            System.err.println("ERRORE: Impossibile aprire il popup, loggedUser è null!");
            return;
        }

        NewWorkplaceGC popupController = (NewWorkplaceGC) SceneManager.getInstance()
                .showModalDialog("NewWorkplace.fxml", "Configura Nuovo Workplace");


        // 2. Passiamo l'utente loggato al controller del popup
        if (popupController != null) {
            popupController.setLoggedUser(this.loggedUser);
            System.out.println("utente passato al popup: " + this.loggedUser.getEmail());
            popupController.getNameField().getScene().getWindow().setOnHiding(e -> {
                System.out.println("DEBUG HOME: Popup in chiusura, eseguo il refresh...");
                refreshWorkplaceList();
            });
        } else {
            System.err.println("Errore nel caricamento del popup");
        }

        refreshWorkplaceList();

    }

    public void onLogoutClicked(ActionEvent event) {
        this.loggedUser = null;
        SceneManager.getInstance().switchScene("Login.fxml", "Login", 900, 600);
        System.out.println("Logout effettuato");
    }

    public void onShiftsclicked(ActionEvent actionEvent) {
        WorkplaceBean wp = SessionContext.getInstance().getLoggedWorkplace();
        if (wp != null) {
            handleWorkplaceSelection(wp.getWorkplaceName());
        } else {
            System.out.println("Seleziona un workplace dai tuoi per vedere i turni");
        }
    }

    public void onWorkersclicked(ActionEvent actionEvent) {
        WorkplaceBean wp = SessionContext.getInstance().getLoggedWorkplace();
        if(wp!=null){
            SceneManager.getInstance().switchScene("Workers.fxml", "Gestione Membri", 900, 600);
        }else{
            SceneManager.getInstance().showInfoAlert("Seleziona un workplace", "Per vedere i membri devi prima selezionare un workplace");
        }
    }
    public void onSettingsclicked(ActionEvent actionEvent) {
        WorkplaceBean wp = SessionContext.getInstance().getLoggedWorkplace();
        if(wp!=null){
            SceneManager.getInstance().switchScene("Settings.fxml", "Gestione Membri", 900, 600);
        }else{
            SceneManager.getInstance().showInfoAlert("Seleziona un workplace", "Per vedere i membri devi prima selezionare un workplace");
        }
    }

}
