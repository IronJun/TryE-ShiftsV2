package com.ispw.tryeshifts.graphcontroller.gui;

import com.ispw.tryeshifts.appcontroller.NotificationAC;
import com.ispw.tryeshifts.graphcontroller.gui.component.NavbarGC;
import com.ispw.tryeshifts.graphcontroller.gui.utilities.NavPage;
import com.ispw.tryeshifts.graphcontroller.gui.utilities.SceneManager;
import com.ispw.tryeshifts.appcontroller.ManageMembersAC;
import com.ispw.tryeshifts.session.SessionContext;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.exception.BaseException;
import com.ispw.tryeshifts.exception.EntityNotFoundException;
import com.ispw.tryeshifts.graphcontroller.gui.utilities.ErrorViewManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

import static javafx.scene.layout.Priority.ALWAYS;

public class WorkersGC {
    @FXML private ListView<UserBean> activeWorkersList;
    @FXML private ListView<UserBean> pendingWorkersList;
    @FXML private Label titleLabel;
    @FXML private Label titlePending;
    @FXML private Label errorlbl;
    @FXML private NavbarGC navbarController;
    private WorkplaceBean currentWorkplace;
    private final ManageMembersAC ac = new ManageMembersAC();
    private final NotificationAC notificationAC = new NotificationAC();

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
        }catch(EntityNotFoundException e){
            SceneManager.getInstance().showErrorAlert("Errore",e.getMessage());
        }catch(BaseException e){
            SceneManager.getInstance().showErrorAlert("Errore Tecnico",e.getMessage());
        }
    }

    private void setupCells() {
        activeWorkersList.setCellFactory(lv -> new ActiveWorkerCell());
        pendingWorkersList.setCellFactory(lv -> new PendingWorkerCell());

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
        }catch(EntityNotFoundException e){
            SceneManager.getInstance().showErrorAlert("Errore",e.getMessage());
        }catch(BaseException e){
            SceneManager.getInstance().showErrorAlert("Errore tecnico",e.getMessage());
        }

    }

    private class ActiveWorkerCell extends ListCell<UserBean> {
        @Override
        protected void updateItem(UserBean item, boolean empty) {
            super.updateItem(item, empty);
            if(empty || item == null){
                setText(null);
                setGraphic(null);
            }else{
                VBox card = new VBox(5);
                card.setPadding(new Insets(10));
                card.setStyle("-fx-background-color: white; -fx-border-color: #dddddd; -fx-border-radius: 8; -fx-background-radius: 8;");

                HBox topBox = new HBox();
                topBox.setAlignment(Pos.CENTER_LEFT);


                // Composzione del nome in modo sicuro contro i null
                String fullName = (item.getName() != null ? item.getName() : "") + " " +
                        (item.getSurname() != null ? item.getSurname() : "");
                Label nameLabel = new Label(fullName.trim().isEmpty() ? "Utente" : fullName);
                nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333333;");


                Region spacer = new Region();
                HBox.setHgrow(spacer, ALWAYS);

                // Badge per il ruolo (stile etichetta azzurra)
                String roleText = (item.getRole() != null) ? item.getRole() : "Worker";
                Label roleLabel = new Label(roleText);
                roleLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: white; " +
                        "-fx-background-color: #3498db; -fx-padding: 3 8 3 8; -fx-background-radius: 10;");
                topBox.getChildren().addAll(nameLabel, spacer, roleLabel);


                // Email in basso a sinistra
                Label emailLabel = new Label(item.getEmail());
                emailLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
                card.getChildren().addAll(topBox, emailLabel);
                setGraphic(card);
            }
        }
    }

    private class PendingWorkerCell extends ListCell<UserBean> {
        @Override
        protected void updateItem(UserBean item, boolean empty) {
            super.updateItem(item, empty);
            if(empty || item == null){
                setText(null);
                setGraphic(null);
            }else {

                VBox card = new VBox(5);
                card.setPadding(new javafx.geometry.Insets(10));
                card.setStyle("-fx-background-color: white; -fx-border-color: #dddddd; -fx-border-radius: 8; -fx-background-radius: 8;");
                HBox topBox = new HBox(10);
                topBox.setAlignment(Pos.CENTER_LEFT);
                // Nome
                String fullName = (item.getName() != null ? item.getName() : "") + " " +
                        (item.getSurname() != null ? item.getSurname() : "");
                Label nameLabel = new Label(fullName.trim().isEmpty() ? "Utente" : fullName);
                nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333333;");
                Region spacer = new Region();
                HBox.setHgrow(spacer, ALWAYS);
                // Pulsanti posizionati sulla destra
                Button btnAcc = new Button("V");
                btnAcc.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-cursor: hand; -fx-background-radius: 5; -fx-min-width: 30px;");

                Button btnRej = new Button("X");
                btnRej.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-cursor: hand; -fx-background-radius: 5; -fx-min-width: 30px;");
                btnAcc.setOnAction(e -> handleResponse(item.getEmail(), true));
                btnRej.setOnAction(e -> handleResponse(item.getEmail(), false));
                topBox.getChildren().addAll(nameLabel, spacer, btnAcc, btnRej);
                // Email in basso a sinistra
                Label emailLabel = new Label(item.getEmail());
                emailLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
                card.getChildren().addAll(topBox, emailLabel);
                setGraphic(card);
            }
        }
    }


}
