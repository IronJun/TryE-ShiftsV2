package com.ispw.tryeshifts;

import com.ispw.tryeshifts.appcontroller.LoginAC;
import com.ispw.tryeshifts.bean.SessionContext;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.excpetion.BaseException;
import javafx.application.Application;
import javafx.stage.Stage;

public class JavaFXLauncher extends Application {
    public static void startApp(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) throws Exception {
        SceneManager manager = SceneManager.getInstance();
        manager.setPrimaryStage(primaryStage);

        String savedEmail = SessionContext.getInstance().getSavedEmail();
        if(savedEmail != null){
            try {
                UserBean user = LoginAC.autoLogin(savedEmail);
                SessionContext.getInstance().setLoggeduser(user);
                manager.switchScene("Home.fxml", "E-Shifts - Home", 900, 600);
                return;
            } catch(BaseException e) {
                SessionContext.getInstance().clearPreferences();
            }
        }
        manager.switchScene("SignUp.fxml", "E-Shifts - Sign UP", 850, 550);
    }

}
