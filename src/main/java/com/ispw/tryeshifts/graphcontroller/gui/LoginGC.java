package com.ispw.tryeshifts.graphcontroller.gui;

import com.ispw.tryeshifts.graphcontroller.gui.utilities.SceneManager;
import com.ispw.tryeshifts.appcontroller.LoginAC;
import com.ispw.tryeshifts.session.SessionContext;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.exception.BaseException;
import com.ispw.tryeshifts.exception.EntityNotFoundException;
import com.ispw.tryeshifts.exception.InvalidCredentialException;
import com.ispw.tryeshifts.graphcontroller.gui.utilities.ErrorViewManager;
import com.ispw.tryeshifts.utils.PreferencesManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class LoginGC {

    public Button appleLogButton;
    public Button faceBookLogButton;
    public Button googleLogButton;
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
        ErrorViewManager.setupAutoHide(errorLabel);
    }
    @FXML
    public void onLoginClicked() {
        String email = emailField != null ? emailField.getText().trim() : "";
        String password = isPasswordVisible?passwordTextField.getText() : passwordField.getText();

        try{
            UserBean inputBean = new UserBean(email, password);
            UserBean loggedUser = new LoginAC().loginUser(inputBean);

            SessionContext.getInstance().setLoggeduser(loggedUser);

            if(rememberMeCheckBox.isSelected()) PreferencesManager.saveUserToPreferences(email);
            else PreferencesManager.clearPreferences();
            SceneManager.getInstance().switchScene("Home.fxml", "Home", 900, 600);

        } catch (EntityNotFoundException | InvalidCredentialException e) {
            ErrorViewManager.showError(errorLabel, e.getMessage());
        } catch (BaseException e) {
            SceneManager.getInstance().showErrorAlert("Technical error", e.getMessage());
        }
    }
    @FXML
    public void onBackClicked() {
        // Torna alla scena di SignUp
        SceneManager.getInstance().switchScene("SignUp.fxml", "SignUp", 900, 600);
    }

    public void togglePassword() {
        if(isPasswordVisible){
            passwordField.setText(passwordTextField.getText());
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

    public void metaLog() {
        SceneManager.getInstance().showInfoAlert("Error", "access by third part software is not yet implemented");
    }
}
