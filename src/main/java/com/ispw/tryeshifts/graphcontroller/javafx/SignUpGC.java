package com.ispw.tryeshifts.graphcontroller.javafx;

import com.ispw.tryeshifts.SceneManager;
import com.ispw.tryeshifts.appcontroller.SignupAC;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.excpetion.*;
import com.ispw.tryeshifts.graphcontroller.javafx.utilities.ErrorViewManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class SignUpGC {

    @FXML private TextField emailField;
    @FXML private TextField nameField;
    @FXML private TextField surnameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField repeatPasswordField;
    @FXML private Label errorLabel;
    @FXML private Button signUpButton;
    @FXML private ImageView eyeIcon;
    @FXML private ImageView eyeIcon2;
    @FXML private TextField passwordTextField;
    @FXML private TextField repeatPasswordTextField;
    private boolean isPasswordVisible = false;
    private boolean isRepeatPasswordVisibile = false;



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
        } catch (IncompleteDataException | DuplicateEntityException | InvalidCredentialException e) {
            ErrorViewManager.showError(errorLabel, e.getMessage());
        }catch(ValidationException | SecuriryException e){
            ErrorViewManager.showError(errorLabel, e.getMessage());
        } catch (BaseException e){
            ErrorViewManager.ScreenError("System Error",e.getMessage());
        }

    }

    public void goTologin(){
        SceneManager.getInstance().switchScene("Login.fxml", "Login", 900, 600);
    }

    public void togglePassword() {
      isPasswordVisible = toggleGeneric(isPasswordVisible, passwordField, passwordTextField, eyeIcon);
    }

    public void toggleRepeatPassword() {
        isRepeatPasswordVisibile = toggleGeneric(isRepeatPasswordVisibile, repeatPasswordField, repeatPasswordTextField, eyeIcon2);
    }

    private boolean toggleGeneric(boolean currentVisibility, PasswordField pf, TextField tf, ImageView icon){
        if(currentVisibility){
            pf.setText(pf.getText());
            pf.setVisible(true);
            tf.setVisible(false);
            icon.setImage(new Image(getClass().getResourceAsStream("/com/ispw/tryeshifts/view/assets/closedEye.png")));
            return false;
        }else{
            tf.setText(pf.getText());
            tf.setVisible(true);
            pf.setVisible(false);
            icon.setImage(new Image(getClass().getResourceAsStream("/com/ispw/tryeshifts/view/assets/openedEye.png")));
            return true;
        }
    }
}
