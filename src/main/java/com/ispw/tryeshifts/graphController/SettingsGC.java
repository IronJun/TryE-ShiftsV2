package com.ispw.tryeshifts.graphController;

import com.ispw.tryeshifts.SceneManager;
import com.ispw.tryeshifts.appController.CreateWorkplaceAC;
import com.ispw.tryeshifts.appController.SettingsAC;
import com.ispw.tryeshifts.bean.SessionContext;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.graphController.utilities.ErrorViewManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;



public class SettingsGC {
    @FXML private TextField nameField,addressField;
    @FXML private FlowPane daysContainer;
    @FXML private ListView<String> shiftsListView;
    @FXML private ComboBox<String> startHourCombo, startMinuteCombo, endHourCombo, endMinuteCombo;
    @FXML private Label errorlabel;
    @FXML private VBox leftPane;
    @FXML private VBox rightPane;
    @FXML private HBox mainHBox;
    @FXML private TextField firstNameField,lastNameField, emailField;
    @FXML private PasswordField newPasswordField,confirmPasswordField;

    private List<CheckBox> dayCheckBoxes = new ArrayList<>();
    private final String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

    @FXML
    public void initialize(){
        ErrorViewManager.setupAutoHide(errorlabel);
        UserBean loggedUser = SessionContext.getInstance().getLoggeduser();
        WorkplaceBean wp = SessionContext.getInstance().getLoggedWorkplace();

        boolean isOwner = loggedUser.getEmail().equals(wp.getOwnerEmail());

        if(!isOwner){
            mainHBox.getChildren().remove(leftPane);
        }else {
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
        String start = startHourCombo.getValue() + ":" + startMinuteCombo.getValue();
        String end = endHourCombo.getValue() + ":" + endMinuteCombo.getValue();
        String fullShift = start + " - " + end;

        // Evita duplicati nella lista
        if (!shiftsListView.getItems().contains(fullShift)) {
            shiftsListView.getItems().add(fullShift);
        } else {
            ErrorViewManager.showError(errorlabel,"turno già esistente");
        }
    }

    @FXML
    private void saveWorkplaceChanges(){
        try{
            List<String> selectedDays = dayCheckBoxes.stream().filter(CheckBox::isSelected).map(CheckBox::getText).toList();
            WorkplaceBean currentWp = SessionContext.getInstance().getLoggedWorkplace();
            String oldName = currentWp.getWorkplaceName();

            WorkplaceBean updatedBean = new WorkplaceBean(nameField.getText(),addressField.getText(),selectedDays,shiftsListView.getItems(),currentWp.getOwnerEmail());

            CreateWorkplaceAC ac = new CreateWorkplaceAC();
            ac.updateWorkplaceAC(updatedBean,oldName);

            SceneManager.getInstance().showInfoAlert("Success","Workplace updated correctly");

        }catch(Exception e){
            SceneManager.getInstance().showErrorAlert("Errore aggiornamento","Impossibile aggiornare il workplace");
        }
    }
    public void onShiftsClicked(ActionEvent actionEvent) {
        WorkplaceBean wp = SessionContext.getInstance().getLoggedWorkplace();
        if(wp!=null){
            SceneManager.getInstance().switchScene("Shifts.fxml", "Gestione Membri", 900, 600);
        }else{
            SceneManager.getInstance().showInfoAlert("Seleziona un workplace", "Per vedere i membri devi prima selezionare un workplace");
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

    public void onLogoutClicked(ActionEvent actionEvent) {
        SessionContext.getInstance().setLoggeduser(null);
        SessionContext.getInstance().setLoggedWorkplace(null);
        SceneManager.getInstance().switchScene("Login.fxml", "Login", 900, 600);
    }

    public void onHomeclicked(ActionEvent actionEvent) {
        SceneManager.getInstance().switchScene("Home.fxml", "Home", 900, 600);
    }

    public void saveProfileChanges(ActionEvent actionEvent) {
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
        }catch(Exception e){
            SceneManager.getInstance().showErrorAlert("Errore aggioranemnto 2","Impossibile aggiornare il profilo");
        }
    }
}
