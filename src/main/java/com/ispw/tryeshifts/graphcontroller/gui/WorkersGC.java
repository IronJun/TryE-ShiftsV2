package com.ispw.tryeshifts.graphcontroller.gui;

import com.ispw.tryeshifts.graphcontroller.gui.component.NavbarGC;
import com.ispw.tryeshifts.graphcontroller.gui.utilities.NavPage;
import com.ispw.tryeshifts.graphcontroller.gui.utilities.SceneManager;
import com.ispw.tryeshifts.appcontroller.ManageMembersAC;
import com.ispw.tryeshifts.session.SessionContext;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.excpetion.BaseException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;
import com.ispw.tryeshifts.graphcontroller.gui.utilities.ErrorViewManager;
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
    @FXML private Label errorlbl;
    @FXML private NavbarGC navbarController;
    private WorkplaceBean currentWorkplace;
    private final ManageMembersAC ac = new ManageMembersAC();


    public void initialize(){
        this.currentWorkplace = SessionContext.getInstance().getLoggedWorkplace();
        if(this.currentWorkplace == null) {
            ErrorViewManager.showError(errorlbl, "Error, Wokrplace Selected is null");
            return;
        }
        if(navbarController != null){
            navbarController.setActivePage(NavPage.WORKERS);
        }else{
            ErrorViewManager.showError(errorlbl,"Error: No active Navbar");
        }
        UserBean loggedUser = SessionContext.getInstance().getLoggeduser();
        if(loggedUser == null){
            ErrorViewManager.showError(errorlbl,"loggeduser is null\n");
            return;
        }
        String emailUser = loggedUser.getEmail();
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
            String wpName = currentWorkplace.getWorkplaceName();
            //gestione membri attivi

            List<UserBean> activeMembers = ac.getActiveMembers(wpName);
            activeWorkersList.getItems().setAll(activeMembers);

            List<UserBean> pending = ac.getPendingRequests(wpName);
            pendingWorkersList.getItems().setAll(pending);

            setupCells();
        }catch(EntityNotFoundException _){
            ErrorViewManager.screenError("Errore","Impossibile trovare il workplace");
        }catch(BaseException _){
            ErrorViewManager.screenError("Errore tecnico","Impossibile recuperare i membri");
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
                    setText(item.getEmail() + " (" + item.getRole() + ")" + "        " + item.getName() +" "+ item.getSurname());
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

                    Label label = new Label(item.getEmail() + "      "+ item.getName() +" "+ item.getSurname());
                    Button btnAcc = new Button("V");
                    btnAcc.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                    Button btnRej = new Button("X");
                    btnRej.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

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
            WorkplaceBean currentWp = SessionContext.getInstance().getLoggedWorkplace();

            if(currentWp == null){
                ErrorViewManager.showError(errorlbl,"Workplace is null\n");
                return;
            }

            String wpName = currentWp.getWorkplaceName();
            // Chiamiamo l'applicativo per aggiornare il DB
            ac.acceptWorker(userEmail, wpName, accept);
            SceneManager.getInstance().showInfoAlert("Success","Correctly updated the DB");
            loadLists();
        }catch(EntityNotFoundException _){
            ErrorViewManager.screenError("Errore","Impossibile trovare il workplace");
        }catch(BaseException _){
            ErrorViewManager.screenError("Errore tecnico","Impossibile aggiornare il DB");
        }

    }


}
