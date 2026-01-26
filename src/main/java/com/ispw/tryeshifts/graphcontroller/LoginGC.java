package com.ispw.tryeshifts.graphcontroller;

import com.ispw.tryeshifts.SceneManager;
import com.ispw.tryeshifts.appcontroller.LoginAC;
import com.ispw.tryeshifts.bean.SessionContext;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.excpetion.BaseException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;
import com.ispw.tryeshifts.excpetion.InvalidCredentialException;
import com.ispw.tryeshifts.graphcontroller.utilities.ErrorViewManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginGC {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;
    @FXML private Button loginButton;

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
            SceneManager.getInstance().switchScene("Home.fxml", "Home", 900, 600);

        } catch (EntityNotFoundException | InvalidCredentialException e) {
            ErrorViewManager.showError(errorLabel, e.getMessage());
        } catch (BaseException _) {
            ErrorViewManager.ScreenError("Errore Tecnico", "Server non raggiungibile.");
        }
    }


    @FXML
    public void onBackClicked() {
        // Torna alla scena di SignUp
        SceneManager.getInstance().switchScene("SignUp.fxml", "SignUp", 900, 600);
    }
}
