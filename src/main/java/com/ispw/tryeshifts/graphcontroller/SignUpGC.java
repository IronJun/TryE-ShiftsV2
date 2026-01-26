package com.ispw.tryeshifts.graphcontroller;

import com.ispw.tryeshifts.SceneManager;
import com.ispw.tryeshifts.appcontroller.SignupAC;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.excpetion.*;
import com.ispw.tryeshifts.graphcontroller.utilities.ErrorViewManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class SignUpGC {

    @FXML private TextField emailField;
    @FXML private TextField nameField;
    @FXML private TextField surnameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField repeatPasswordField;
    @FXML private Label errorLabel;
    @FXML private Button signUpButton;

    public void initialize() {
        signUpButton.setDefaultButton(true);
    }

    @FXML
    public void onSignUpclicked(ActionEvent event) {
        ErrorViewManager.setupAutoHide(errorLabel);

        // Validate all required fields
        String email = emailField != null ? emailField.getText().trim() : "";
        String name = nameField != null ? nameField.getText().trim() : "";
        String surname = surnameField != null ? surnameField.getText().trim() : "";
        String pwd = passwordField != null ? passwordField.getText().trim() : "";
        String repeat = repeatPasswordField != null ? repeatPasswordField.getText().trim() : "";

        try {
            UserBean bean = new UserBean(email, pwd, name, surname, repeat);
            SignupAC.registerUser(bean);
            SceneManager.getInstance().switchScene("Login.fxml", "Login", 900, 600);
        } catch (ValidationException e) {
            ErrorViewManager.showError(errorLabel, "campi non validi");
        } catch (DuplicateEntityException e) {
            ErrorViewManager.showError(errorLabel, "questa mail è già in uso");
        } catch (BaseException e){
            ErrorViewManager.ScreenError("System Error",e.getMessage());
        }

    }

    public void goTologin(){
        SceneManager.getInstance().switchScene("Login.fxml", "Login", 900, 600);
    }

}
