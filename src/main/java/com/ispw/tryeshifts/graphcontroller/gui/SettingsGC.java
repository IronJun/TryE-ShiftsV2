package com.ispw.tryeshifts.graphcontroller.gui;

import com.ispw.tryeshifts.graphcontroller.gui.component.NavbarGC;
import com.ispw.tryeshifts.graphcontroller.gui.utilities.NavPage;
import com.ispw.tryeshifts.graphcontroller.gui.utilities.SceneManager;
import com.ispw.tryeshifts.appcontroller.ManageShiftsAC;
import com.ispw.tryeshifts.appcontroller.SettingsAC;
import com.ispw.tryeshifts.session.SessionContext;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.exception.BaseException;
import com.ispw.tryeshifts.exception.InvalidCredentialException;
import com.ispw.tryeshifts.graphcontroller.gui.utilities.ErrorViewManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



public class SettingsGC {
    @FXML private TextField nameField;
    @FXML private TextField addressField;
    @FXML private FlowPane daysContainer;
    @FXML private ListView<String> shiftsListView;
    @FXML private ComboBox<String> startHourCombo;
    @FXML private ComboBox<String> startMinuteCombo;
    @FXML private ComboBox<String> endHourCombo;
    @FXML private ComboBox<String> endMinuteCombo;
    @FXML private Label errorlabel;
    @FXML private Label errorlbl2;
    @FXML private VBox leftPane;
    @FXML private VBox rightPane;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private VBox overlayPane;
    @FXML private Label overlayMessage;
    @FXML private NavbarGC navbarController;
    private final List<CheckBox> dayCheckBoxes = new ArrayList<>();
    private final String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

    @FXML
    public void initialize(){
        ErrorViewManager.setupAutoHide(errorlbl2);
        ErrorViewManager.setupAutoHide(errorlabel);
        if(navbarController != null){
            navbarController.setActivePage(NavPage.SETTINGS);
        }
        UserBean loggedUser = SessionContext.getInstance().getLoggeduser();
        WorkplaceBean wp = SessionContext.getInstance().getLoggedWorkplace();
        if(loggedUser == null){
            ErrorViewManager.showError(overlayMessage, "Workplace or User is null!\n");
            return;
        }
        shiftsListView.setCellFactory(lv -> new ShiftCellHandling());
        if (wp==null) {
            showLeftPaneRestriction("No Workplace Selected");
        } else if (!loggedUser.getEmail().equals(wp.getOwnerEmail())) {
            showLeftPaneRestriction("You are not the owner of the workplace");
        } else {
            hideLeftPaneRestriction();
            initworkplaceSettings(wp);
            initTimeCombos();
        }
        initAccountSett(loggedUser);
    }
    private void initAccountSett(UserBean user){
        firstNameField.setText(user.getName());
        lastNameField.setText(user.getSurname());
        emailField.setText(user.getEmail());
    }
    private void initworkplaceSettings(WorkplaceBean wp){
        if (wp == null) return;
        nameField.setText(wp.getWorkplaceName());
        addressField.setText(wp.getAddress());
        for (String day : days) {
            CheckBox checkBox = new CheckBox(day);
            if (wp.getSelectedDays().contains(day)) checkBox.setSelected(true);
            daysContainer.getChildren().add(checkBox);
            dayCheckBoxes.add(checkBox);
        }

        shiftsListView.getItems().addAll(wp.getShiftsBean());
    }
    private void initTimeCombos(){
        for (int i = 0; i < 24; i++) {
            String hour = String.format("%02d", i);
            startHourCombo.getItems().add(hour);
            endHourCombo.getItems().add(hour);
        }

        // Popola Minuti (00, 15, 30, 45)
        String[] minutes = {"00", "15", "30", "45"};
        startMinuteCombo.getItems().addAll(minutes);
        endMinuteCombo.getItems().addAll(minutes);

        // Imposta valori di default per evitare null pointer al primo click
        startHourCombo.getSelectionModel().selectFirst();
        startMinuteCombo.getSelectionModel().selectFirst();
        endHourCombo.getSelectionModel().selectFirst();
        endMinuteCombo.getSelectionModel().selectFirst();
    }
    @FXML
    private void addShift(){
        String startH = startHourCombo.getValue();
        String startM = startMinuteCombo.getValue();
        String endH = endHourCombo.getValue();
        String endM = endMinuteCombo.getValue();
        try{
            String formattedShift =new ManageShiftsAC().addShiftstoWorkaplce(startM,startH,endM,endH,shiftsListView.getItems());
            shiftsListView.getItems().add(formattedShift);
            Collections.sort(shiftsListView.getItems());
        }catch (InvalidCredentialException _){
            ErrorViewManager.showError(errorlabel,"Invalid shift time!");
        } catch (BaseException e) {
            ErrorViewManager.showError(errorlabel,e.getMessage());
        }
    }


    @FXML
    private void saveWorkplaceChanges(){
        try{
            List<String> selectedDays = dayCheckBoxes.stream().filter(CheckBox::isSelected).map(CheckBox::getText).toList();
            WorkplaceBean currentWp = SessionContext.getInstance().getLoggedWorkplace();
            if(currentWp == null){
                ErrorViewManager.showError(errorlabel,"No Workplace Selected!\n");
                return;
            }
            String oldName = currentWp.getWorkplaceName();

            WorkplaceBean updatedBean = new WorkplaceBean(nameField.getText(),addressField.getText(),selectedDays,shiftsListView.getItems(),currentWp.getOwnerEmail());

            SettingsAC ac = new SettingsAC();
            ac.updateWorkplace(updatedBean,oldName);

            SceneManager.getInstance().showInfoAlert("Success","Workplace updated correctly");

        }catch(BaseException e){
            SceneManager.getInstance().showErrorAlert("Errore aggiornamento",e.getMessage());
        }
    }

    public void saveProfileChanges() {
        try{
            String newPwd = newPasswordField.getText();
            String confirmPwd = confirmPasswordField.getText();

            if (!newPwd.isEmpty()) {
                if (!newPwd.equals(confirmPwd)) {
                    SceneManager.getInstance().showErrorAlert("Errore Pswd", "Le password non coincidono!");
                    return;
                }
                if (newPwd.length() < 6) {
                    SceneManager.getInstance().showErrorAlert("Errore Pswd 2", "La password deve essere di almeno 6 caratteri.");
                    return;
                }
            }

            UserBean updatedUser = SessionContext.getInstance().getLoggeduser();
            if(updatedUser == null){
                ErrorViewManager.showError(errorlbl2,"User not found");
                return;
            }
            updatedUser.setName(firstNameField.getText());
            updatedUser.setSurname(lastNameField.getText());

            if(!newPwd.isEmpty()){
                updatedUser.setPassword(newPwd);
            }

            SettingsAC ac = new SettingsAC();
            ac.updateUserProfile(updatedUser);

            SceneManager.getInstance().showInfoAlert("Success","Profile updated correctly");

            newPasswordField.clear();
            confirmPasswordField.clear();
        }catch(BaseException e){
            SceneManager.getInstance().showErrorAlert("Errore aggioranemnto 2",e.getMessage());
        }
    }
    private void showLeftPaneRestriction(String message) {
        overlayPane.setVisible(true);
        overlayPane.setManaged(true);

        overlayMessage.setVisible(true);
        overlayMessage.setManaged(true);
        overlayMessage.setText(message);

        BoxBlur boxBlur = new BoxBlur(10, 10, 3);
        leftPane.setEffect(boxBlur);
        leftPane.setDisable(true);
        overlayPane.setMouseTransparent(false);
    }

    private void hideLeftPaneRestriction() {
        overlayPane.setVisible(false);
        overlayPane.setManaged(false);

        overlayMessage.setVisible(false);
        overlayMessage.setManaged(false);

        leftPane.setEffect(null);
        leftPane.setDisable(false);
        overlayPane.setMouseTransparent(true);
    }

}
