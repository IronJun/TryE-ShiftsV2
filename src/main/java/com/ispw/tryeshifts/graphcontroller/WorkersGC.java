package com.ispw.tryeshifts.graphcontroller;

import com.ispw.tryeshifts.SceneManager;
import com.ispw.tryeshifts.appcontroller.ManageMembersAC;
import com.ispw.tryeshifts.bean.SessionContext;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.excpetion.DAOException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;

import java.util.List;

public class WorkersGC {
    @FXML private ListView<UserBean> activeWorkersList;
    @FXML private ListView<UserBean> pendingWorkersList;
    @FXML private Label titleLabel;
    @FXML private Label titlePending;
    private WorkplaceBean currentWorkplace;

    public void initialize(){
        this.currentWorkplace = SessionContext.getInstance().getLoggedWorkplace();
        String emailUser = SessionContext.getInstance().getLoggeduser().getEmail();

        boolean isOwner = currentWorkplace.getOwnerEmail().equals(emailUser);
        titlePending.setVisible(isOwner);
        titlePending.setManaged(isOwner);
        pendingWorkersList.setVisible(isOwner);
        pendingWorkersList.setManaged(isOwner);
        activeWorkersList.setVisible(true);

        if(currentWorkplace != null){
            titleLabel.setText("Lavoratori: "+ currentWorkplace.getWorkplaceName());
            loadLists();
        }
    }

    private void loadLists(){
        try{
            ManageMembersAC ac = new ManageMembersAC();
            String wpName = currentWorkplace.getWorkplaceName();
            //gestione membri attivi

            List<UserBean> activeMembers = ac.getActiveMembers(wpName);
            activeWorkersList.getItems().setAll(activeMembers);

            List<UserBean> pending = ac.getPendingRequests(wpName);
            pendingWorkersList.getItems().setAll(pending);

            setupCells();
        }catch(EntityNotFoundException _){
            SceneManager.getInstance().showErrorAlert("Errore","Impossibile trovare il workplace");
        }catch(DAOException _){
            SceneManager.getInstance().showErrorAlert("Errore tecnico","Impossibile recuperare i membri");
        }
    }



    private void setupCells() {
        // Cella per i membri attivi: mostra "Email (Ruolo)"
        activeWorkersList.setCellFactory(lv -> new ListCell<UserBean>() {
            @Override
            protected void updateItem(UserBean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getEmail() + " (" + item.getRole() + ")");
                }
            }
        });

        // Cella per i pendenti: mostra "Email" + Bottoni
        pendingWorkersList.setCellFactory(lv -> new ListCell<UserBean>() {
            @Override
            protected void updateItem(UserBean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    HBox container = new HBox(10);
                    container.setAlignment(Pos.CENTER_LEFT);

                    Label label = new Label(item.getEmail());
                    Button btnAcc = new Button("V");
                    Button btnRej = new Button("X");

                    btnAcc.setOnAction(e -> handleResponse(item.getEmail(), true));
                    btnRej.setOnAction(e -> handleResponse(item.getEmail(), false));

                    container.getChildren().addAll(label, btnAcc, btnRej);
                    setGraphic(container);
                }
            }
        });


    }

    private void handleResponse(String userEmail, boolean accept) {
        try{
            ManageMembersAC ac = new ManageMembersAC();
            String currentWp = SessionContext.getInstance().getLoggedWorkplace().getWorkplaceName();
            // Chiamiamo l'applicativo per aggiornare il DB
            ac.acceptWorker(userEmail, currentWp, accept);
            SceneManager.getInstance().showInfoAlert("Success","Correctly updated the DB");
            loadLists();
        }catch(EntityNotFoundException _){
            SceneManager.getInstance().showErrorAlert("Errore","Impossibile trovare il workplace");
        }catch(DAOException _){
            SceneManager.getInstance().showErrorAlert("Errore tecnico","Impossibile aggiornare il DB");
        }

    }

    public void goToHome() {
    SceneManager.getInstance().switchScene("Home.fxml", "Home", 900, 600);
    }

    public void onLogoutClicked() {
        SessionContext.getInstance().setLoggeduser(null);
        SessionContext.getInstance().setLoggedWorkplace(null);
        SceneManager.getInstance().switchScene("Login.fxml", "Login", 400, 500);
    }
    public void goToShifts(){
        SceneManager.getInstance().switchScene("Shifts.fxml", "Turni", 900, 600);
    }

    public void onSettingclicked() {
        SceneManager.getInstance().switchScene("Settings.fxml", "Gestione Membri", 900, 600);
    }
}
