package com.ispw.tryeshifts.graphcontroller.javafx;

import com.ispw.tryeshifts.SceneManager;
import com.ispw.tryeshifts.appcontroller.LoginAC;
import com.ispw.tryeshifts.bean.SessionContext;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.excpetion.BaseException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;
import com.ispw.tryeshifts.excpetion.InvalidCredentialException;
import com.ispw.tryeshifts.graphcontroller.javafx.utilities.ErrorViewManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class LoginGC {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;
    @FXML private CheckBox rememberMeCheckBox;
    private boolean isPasswordVisible = false;
    @FXML private ImageView eyeIcon;
    @FXML private TextField passwordTextField;

    public void initialize(){
        loginButton.setDefaultButton(true);
    }
    @FXML
    public void onLoginClicked() {
        ErrorViewManager.setupAutoHide(errorLabel);

        String email = emailField != null ? emailField.getText().trim() : "";
        String password = passwordField != null ? passwordField.getText().trim() : "";

        try{
            UserBean inputBean = new UserBean(email, password);
            UserBean loggedUser = LoginAC.loginUser(inputBean);

            SessionContext.getInstance().setLoggeduser(loggedUser);

            if(rememberMeCheckBox.isSelected()) SessionContext.getInstance().saveUserToPreferences(email);
            else SessionContext.getInstance().clearPreferences();
            SceneManager.getInstance().switchScene("Home.fxml", "Home", 900, 600);

        } catch (EntityNotFoundException | InvalidCredentialException e) {
            ErrorViewManager.showError(errorLabel, e.getMessage());
        } catch (BaseException _) {
            ErrorViewManager.screenError("Errore Tecnico", "Server non raggiungibile.");
        }
    }


    @FXML
    public void onBackClicked() {
        // Torna alla scena di SignUp
        SceneManager.getInstance().switchScene("SignUp.fxml", "SignUp", 900, 600);
    }

    public void togglePassword() {
        if(isPasswordVisible){
            passwordField.setText(passwordField.getText());
            passwordField.setVisible(true);
            passwordTextField.setVisible(false);

            eyeIcon.setImage(new Image(getClass().getResourceAsStream("/com/ispw/tryeshifts/view/assets/closedEye.png")));
            isPasswordVisible = false;
        }else{
            passwordTextField.setText(passwordField.getText());
            passwordTextField.setVisible(true);
            passwordField.setVisible(false);

            // Cambia l'icona in "occhio aperto"
            eyeIcon.setImage(new Image(getClass().getResourceAsStream("/com/ispw/tryeshifts/view/assets/openedEye.png")));
            isPasswordVisible = true;
        }
    }
}
