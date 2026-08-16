package com.ispw.tryeshifts;

import com.ispw.tryeshifts.appcontroller.LoginAC;
import com.ispw.tryeshifts.graphcontroller.gui.utilities.SceneManager;
import com.ispw.tryeshifts.session.SessionContext;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.excpetion.BaseException;
import com.ispw.tryeshifts.excpetion.InvalidCredentialException;
import com.ispw.tryeshifts.utils.PreferencesManager;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.logging.Logger;

public class JavaFXLauncher extends Application {
    private static final Logger LOGGER = Logger.getLogger(JavaFXLauncher.class.getName());

    public static void startApp(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) throws Exception {
        SceneManager manager = SceneManager.getInstance();
        manager.setPrimaryStage(primaryStage);

        String savedEmail = PreferencesManager.getSavedEmail();
        if(savedEmail != null){
            try {
                UserBean user = new LoginAC().autoLogin(savedEmail);
                SessionContext.getInstance().setLoggeduser(user);
                manager.switchScene("Home.fxml", "E-Shifts - Home", 900, 600);
                return;
            }catch(InvalidCredentialException e) {
                LOGGER.severe("Credential insertion error: " + e.getMessage() +"\n");
            }catch(BaseException e){
                LOGGER.severe("ERRORE: " + e.getMessage() +"\n");
            }
        }
        manager.switchScene("SignUp.fxml", "E-Shifts - Sign UP", 850, 550);
    }

}
