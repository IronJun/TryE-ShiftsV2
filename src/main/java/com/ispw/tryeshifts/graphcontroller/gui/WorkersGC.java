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
//        // Cella per i membri attivi: mostra "Email (Ruolo)"
//        activeWorkersList.setCellFactory(lv -> new ListCell<UserBean>() {
//            @Override
//            protected void updateItem(UserBean item, boolean empty) {
//                super.updateItem(item, empty);
//                if (empty || item == null) {
//                    setText(null);
//                } else {
//                    setText(item.getEmail() + " (" + item.getRole() + ")" + "        " + item.getName() +" "+ item.getSurname());
//                }
//            }
//        });
//
//        // Cella per i pendenti: mostra "Email" + Bottoni
//        pendingWorkersList.setCellFactory(lv -> new ListCell<UserBean>() {
//            @Override
//            protected void updateItem(UserBean item, boolean empty) {
////                super.updateItem(item, empty);
////                if (empty || item == null) {
////                    setGraphic(null);
////                } else {
////                    HBox container = new HBox(10);
////                    container.setAlignment(Pos.CENTER_LEFT);
////
////                    Label label = new Label(item.getEmail() + "      "+ item.getName() +" "+ item.getSurname());
////                    Button btnAcc = new Button("V");
////                    btnAcc.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
////                    Button btnRej = new Button("X");
////                    btnRej.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
////
////                    btnAcc.setOnAction(e -> handleResponse(item.getEmail(), true));
////                    btnRej.setOnAction(e -> handleResponse(item.getEmail(), false));
////
////                    container.getChildren().addAll(label, btnAcc, btnRej);
////                    setGraphic(container);
////                }
////            }
////        });
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
        }catch(EntityNotFoundException _){
            ErrorViewManager.screenError("Errore","Impossibile trovare il workplace");
        }catch(BaseException _){
            ErrorViewManager.screenError("Errore tecnico","Impossibile aggiornare il DB");
        }

    }

    private class ActiveWorkerCell extends ListCell<UserBean> {
        @Override
        protected void updateItem(UserBean item, boolean empty) {
            super.updateItem(item, empty);
            if(empty || item == null){
                setText(null);
                setGraphic(null);
                return;
            }
            setGraphic(createActiveWorkerCard(item));
        }
    }

    private class PendingWorkerCell extends ListCell<UserBean> {
        @Override
        protected void updateItem(UserBean item, boolean empty) {
            super.updateItem(item, empty);
            if(empty || item == null){
                setText(null);
                setGraphic(null);
                return;
            }
            setGraphic(createPendingWorkerCard(item));
        }
    }

    private VBox createActiveWorkerCard(UserBean user) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: white; -fx-border-color: #dddddd; -fx-border-radius: 8; -fx-background-radius: 8;");

        HBox topBox = new HBox();
        topBox.setAlignment(Pos.CENTER_LEFT);


        // Composzione del nome in modo sicuro contro i null
        String fullName = (user.getName() != null ? user.getName() : "") + " " +
                (user.getSurname() != null ? user.getSurname() : "");
        Label nameLabel = new Label(fullName.trim().isEmpty() ? "Utente" : fullName);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333333;");


        Region spacer = new Region();
        HBox.setHgrow(spacer, ALWAYS);

        // Badge per il ruolo (stile etichetta azzurra)
        String roleText = (user.getRole() != null) ? user.getRole() : "Worker";
        Label roleLabel = new Label(roleText);
        roleLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: white; " +
                "-fx-background-color: #3498db; -fx-padding: 3 8 3 8; -fx-background-radius: 10;");
        topBox.getChildren().addAll(nameLabel, spacer, roleLabel);


        // Email in basso a sinistra
        Label emailLabel = new Label(user.getEmail());
        emailLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        card.getChildren().addAll(topBox, emailLabel);
        return card;
    }
    private VBox createPendingWorkerCard(UserBean user) {
        VBox card = new VBox(5);
        card.setPadding(new javafx.geometry.Insets(10));
        card.setStyle("-fx-background-color: white; -fx-border-color: #dddddd; -fx-border-radius: 8; -fx-background-radius: 8;");
        HBox topBox = new HBox(10);
        topBox.setAlignment(Pos.CENTER_LEFT);
        // Nome
        String fullName = (user.getName() != null ? user.getName() : "") + " " +
                (user.getSurname() != null ? user.getSurname() : "");
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
        btnAcc.setOnAction(e -> handleResponse(user.getEmail(), true));
        btnRej.setOnAction(e -> handleResponse(user.getEmail(), false));
        topBox.getChildren().addAll(nameLabel, spacer, btnAcc, btnRej);
        // Email in basso a sinistra
        Label emailLabel = new Label(user.getEmail());
        emailLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        card.getChildren().addAll(topBox, emailLabel);
        return card;
    }

}
