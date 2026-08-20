package com.ispw.tryeshifts.graphcontroller.gui.component;

import com.ispw.tryeshifts.graphcontroller.gui.utilities.NotificationService;
import com.ispw.tryeshifts.appcontroller.NotificationAC;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.excpetion.BaseException;
import com.ispw.tryeshifts.graphcontroller.gui.utilities.ErrorViewManager;
import com.ispw.tryeshifts.graphcontroller.gui.utilities.NavPage;
import com.ispw.tryeshifts.graphcontroller.gui.utilities.SceneManager;
import com.ispw.tryeshifts.session.SessionContext;
import com.ispw.tryeshifts.utils.PreferencesManager;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.application.Platform;


public class NavbarGC {
    @FXML private Button btnHome;
    @FXML private Button btnShifts;
    @FXML private Button btnSettings;
    @FXML private Button btnWorkers;
    @FXML private Label lblBadgeCount;
    @FXML private Label errorlbl;


    private static final String STYLE_INACTIVE = "-fx-background-color: transparent; -fx-text-fill: white;";
    private static final String STYLE_ACTIVE = "-fx-background-color: #6A62B3; -fx-text-fill: white; -fx-background-radius: 10;";
    public void initialize() {
        ErrorViewManager.hideError(errorlbl);
    }


    public void setActivePage(NavPage page){
        btnHome.setStyle(STYLE_INACTIVE);
        btnShifts.setStyle(STYLE_INACTIVE);
        btnSettings.setStyle(STYLE_INACTIVE);
        btnWorkers.setStyle(STYLE_INACTIVE);

        if(page!=null) {
            switch (page) {
                case HOME:
                    btnHome.setStyle(STYLE_ACTIVE);
                    break;
                case SHIFTS:
                    btnShifts.setStyle(STYLE_ACTIVE);
                    break;
                case SETTINGS:
                    btnSettings.setStyle(STYLE_ACTIVE);
                    break;
                case WORKERS:
                    btnWorkers.setStyle(STYLE_ACTIVE);
                    break;

            }
        }
    }

    public void onHomeClicked() {
        ErrorViewManager.hideError(errorlbl);
        SceneManager.getInstance().switchScene("Home.fxml","Home", 900,600);
    }

    public void onShiftsClicked() {
        ErrorViewManager.hideError(errorlbl);
        if (SessionContext.getInstance().getLoggedWorkplace() == null) {
            ErrorViewManager.showError(errorlbl,"Per vedere i turni torna alla home e seleziona un workpalce");
        } else {
            SceneManager.getInstance().switchScene("Shifts.fxml", "Shifts", 900, 600);
        }
    }

    public void onSettingsClicked() {
        ErrorViewManager.hideError(errorlbl);
        SceneManager.getInstance().switchScene("Settings.fxml","Settings", 900,600);
    }

    public void onWorkersClicked() {
        ErrorViewManager.hideError(errorlbl);
        if(SessionContext.getInstance().getLoggedWorkplace() == null){
            ErrorViewManager.showError(errorlbl,"Error, select a workplace to se its workers list.");
        }else {
            SceneManager.getInstance().switchScene("Workers.fxml", "Workers", 900, 600);
        }
    }

    public void onLogoutClicked() {
        ErrorViewManager.hideError(errorlbl);
        if(SceneManager.getInstance().logoutConfirmation()){
            PreferencesManager.clearPreferences();
            SessionContext.getInstance().setLoggedWorkplace(null);
            SessionContext.getInstance().setLoggeduser(null);
            SceneManager.getInstance().switchScene("Login.fxml","Login", 900,600);
        }
    }

    public void onNotificationBellClicked(MouseEvent event) throws BaseException {
        ErrorViewManager.hideError(errorlbl);
        if(lblBadgeCount != null){
            lblBadgeCount.setVisible(false);
        }
        UserBean loggedUser = SessionContext.getInstance().getLoggeduser();
        if(loggedUser == null) {
            ErrorViewManager.showError(errorlbl, "Error, logged user is null");
            return;
        }
        NotificationAC notificationAC = new NotificationAC();
        String email = loggedUser.getEmail();
        Node sourceNode = (Node) event.getSource();
        notificationAC.getUserNotificationsAsync(loggedUser.getEmail())
                .thenAcceptAsync(notification -> {
                    NotificationService.showNotificationPopup(sourceNode, notification, () -> {
                        try {
                            notificationAC.markAllAsRead(email);
                        } catch (BaseException e) {
                            ErrorViewManager.showError(errorlbl, e.getMessage());
                        }
                    });
                }, Platform::runLater)
                .exceptionally(ex -> {
                    Platform.runLater(() -> ErrorViewManager.showError(errorlbl, ex.getMessage()));
                    return null;
                });


    }



}
