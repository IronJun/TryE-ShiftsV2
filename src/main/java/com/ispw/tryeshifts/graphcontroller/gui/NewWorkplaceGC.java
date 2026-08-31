package com.ispw.tryeshifts.graphcontroller.gui;

import com.ispw.tryeshifts.graphcontroller.gui.utilities.SceneManager;
import com.ispw.tryeshifts.appcontroller.CreateWorkplaceAC;
import com.ispw.tryeshifts.appcontroller.ManageShiftsAC;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.exception.*;
import com.ispw.tryeshifts.graphcontroller.gui.utilities.ErrorViewManager;
import com.ispw.tryeshifts.graphcontroller.gui.utilities.ShiftCellHandling;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;


public class NewWorkplaceGC {
    private static final Logger LOGGER = Logger.getLogger(NewWorkplaceGC.class.getName());
    @FXML private TextField nameField;
    @FXML private TextField addressField;
    @FXML private CheckBox checkMon;
    @FXML private CheckBox checkTue;
    @FXML private CheckBox checkWed;
    @FXML private CheckBox checkThu;
    @FXML private CheckBox checkFri;
    @FXML private CheckBox checkSat;
    @FXML private CheckBox checkSun;
    @FXML private ListView<String> shiftsListView;
    @FXML private ComboBox<String> startHourCombo;
    @FXML private ComboBox<String> startMinuteCombo;
    @FXML private ComboBox<String> endHourCombo;
    @FXML private ComboBox<String> endMinuteCombo;
    @FXML private Label errorLabel;

    private UserBean loggedUser;

    public void initialize() {
        ErrorViewManager.setupAutoHide(errorLabel,nameField,addressField);
        ErrorViewManager.setupAutoHideCombo(errorLabel,startHourCombo,startMinuteCombo,endHourCombo,endMinuteCombo);
        for(int i= 0;i<24;i++){
            String h = String.format("%02d",i);
            startHourCombo.getItems().add(h);
            endHourCombo.getItems().add(h);
        }
        for(int i=0;i<60;i+=5){
            String m = String.format("%02d",i);
            startMinuteCombo.getItems().add(m);
            endMinuteCombo.getItems().add(m);
        }
        startHourCombo.getSelectionModel().selectFirst();
        startMinuteCombo.getSelectionModel().selectFirst();
        endHourCombo.getSelectionModel().select(1);
        endMinuteCombo.getSelectionModel().select(0);

        shiftsListView.setCellFactory(listView -> new ShiftCellHandling());
    }
    public void setLoggedUser(UserBean user) {
        this.loggedUser = user;
    }
    @FXML
    public void onSave() {
        if (loggedUser == null) {
            LOGGER.warning("ERROR: not received the logged user!");
            return;
        }

        String email = loggedUser.getEmail();
        // 1. Validazione base
        if (nameField.getText().isEmpty() || addressField.getText().isEmpty()) {
            ErrorViewManager.showError(errorLabel,"Name and address are mandatory!");
            return;
        }
        List<String> days = getSelectedDays();
        if (days.isEmpty()) {
            ErrorViewManager.showError(errorLabel,"You have to select at least one day of the week!");
            return;
        }
        List<String> shifts = new ArrayList<>(shiftsListView.getItems());
        if(shifts.isEmpty()){
            ErrorViewManager.showError(errorLabel,"you have to add at least one shift!");
            return;
        }
        // 2. Creazione del Bean (trasporto dati verso la logica applicativa)
        try {
            WorkplaceBean wpBean = new WorkplaceBean(nameField.getText(), addressField.getText(), days, shifts, email);
            new CreateWorkplaceAC().createWorkplace(wpBean);
            SceneManager.getInstance().showInfoAlert("Success", "Workplace created con correctly!");
            closeWindow();
        }catch(DuplicateEntityException | EntityNotFoundException e){
            SceneManager.getInstance().showErrorAlert("Workplace creation went wrong: ",e.getMessage());
        } catch(BaseException e) {
            SceneManager.getInstance().showErrorAlert("Technical error occurred: ", e.getMessage());
        }
    }
    @FXML
    public void onCancel() {
        closeWindow();
    }
    private void closeWindow() {
        // Recupera lo Stage (finestra) corrente tramite un nodo qualsiasi e lo chiude
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();

    }
    private List<String> getSelectedDays() {
        List<String> days = new ArrayList<>();
        if (checkMon.isSelected()) days.add("Mon");
        if (checkTue.isSelected()) days.add("Tue");
        if (checkWed.isSelected()) days.add("Wed");
        if (checkThu.isSelected()) days.add("Thu");
        if (checkFri.isSelected()) days.add("Fri");
        if (checkSat.isSelected()) days.add("Sat");
        if (checkSun.isSelected()) days.add("Sun");
        return days;
    }

    public void addShiftToList() {
        String startH = startHourCombo.getValue();
        String startM = startMinuteCombo.getValue();
        String endH = endHourCombo.getValue();
        String endM = endMinuteCombo.getValue();
        try{
            String formattedShift = new ManageShiftsAC().addShiftstoWorkaplce(startM,startH,endM,endH,shiftsListView.getItems());
            shiftsListView.getItems().add(formattedShift);
            Collections.sort(shiftsListView.getItems());
        }catch(IllegalArgumentException | BaseException e){
            ErrorViewManager.showError(errorLabel,e.getMessage());
        }
    }
    public TextField getNameField() {
        return nameField; // Ritorna il campo del nome per permettere alla Home di trovare la finestra
    }



}