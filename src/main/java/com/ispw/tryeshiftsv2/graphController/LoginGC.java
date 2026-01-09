package com.ispw.tryeshiftsv2.graphController;

import com.ispw.tryeshiftsv2.SceneManager;
import com.ispw.tryeshiftsv2.appController.LoginAC;
import com.ispw.tryeshiftsv2.bean.SessionContext;
import com.ispw.tryeshiftsv2.bean.UserBean;
import com.ispw.tryeshiftsv2.excpetion.DAOException;
import com.ispw.tryeshiftsv2.excpetion.InvalidCredentialException;
import com.ispw.tryeshiftsv2.excpetion.UserNotFoundException;
import javafx.event.ActionEvent;
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
    //private LoginAC loginAC = new LoginAC();
    @FXML
    public void onLoginClicked(ActionEvent event) {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        String email = emailField != null ? emailField.getText().trim() : "";
        String password = passwordField != null ? passwordField.getText().trim() : "";

        try{
            UserBean inputBean = new UserBean(email, password);
            UserBean loggedUser = LoginAC.loginUser(inputBean);

            SessionContext.getInstance().setLoggeduser(loggedUser);
            SceneManager.getInstance().switchScene("Home.fxml", "Home", 900, 600);

        } catch (UserNotFoundException e) {
            // Possiamo suggerire la registrazione
            //SceneManager.getInstance().showErrorAlert("Login Fallito", e.getMessage());
            errorLabel.setText(e.getMessage());
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        } catch (InvalidCredentialException e) {
            // Errore classico di password
            //SceneManager.getInstance().showErrorAlert("Errore Password", e.getMessage());
            errorLabel.setText(e.getMessage());
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        } catch (DAOException e) {
            // Errore di connessione al database
            SceneManager.getInstance().showErrorAlert("Errore Tecnico", "Server non raggiungibile.");
        }
    }


    @FXML
    public void onBackClicked(ActionEvent event) {
        // Torna alla scena di SignUp
        SceneManager.getInstance().switchScene("SignUp.fxml", "SignUp", 900, 600);
    }
}
