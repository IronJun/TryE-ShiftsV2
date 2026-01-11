package com.ispw.tryeshifts.graphController;

import com.ispw.tryeshifts.SceneManager;
import com.ispw.tryeshifts.appController.SignupAC;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.excpetion.DAOException;
import com.ispw.tryeshifts.excpetion.InvalidDataException;
import com.ispw.tryeshifts.excpetion.UserAlreadyExistsException;
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

    public void initialize(){
        signUpButton.setDefaultButton(true);
    }
    @FXML
    public void onSignUpclicked(ActionEvent event){
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // Validate all required fields
        String email = emailField != null ? emailField.getText().trim() : "";
        String name = nameField != null ? nameField.getText().trim() : "";
        String surname = surnameField != null ? surnameField.getText().trim() : "";
        String pwd = passwordField != null ? passwordField.getText().trim() : "";
        String repeat = repeatPasswordField != null ? repeatPasswordField.getText().trim() : "";

        try {
            UserBean bean = new UserBean(email, pwd, name, surname,repeat);
            SignupAC.registerUser(bean);
            //SceneManager.getInstance().showInfoAlert("registrazione avvenuta con successo", "puoi effettuare il login");
            SceneManager.getInstance().switchScene("Login.fxml", "Login", 900, 600);
        }catch(InvalidDataException e){
            errorLabel.setText(e.getMessage());
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
            //SceneManager.getInstance().showErrorAlert("Errore Registrazione",e.getMessage());
        }catch(UserAlreadyExistsException e){
            //SceneManager.getInstance().showErrorAlert("Account già esistente",e.getMessage());
            errorLabel.setText(e.getMessage());
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }catch(DAOException e){
            SceneManager.getInstance().showErrorAlert("Errore tecnico","Impossibile registrare l'utente");
        }

    }

    @FXML
    public void onBackClicked(ActionEvent event){
        System.out.println("Back clicked");
    }

    @FXML
    public void onLoginClicked(ActionEvent event, UserBean user){
        System.out.println("Login clicked");
    }
}
