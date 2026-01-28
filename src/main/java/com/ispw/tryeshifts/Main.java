package com.ispw.tryeshifts;

//import com.ispw.tryeshifts.dao.JDBC;
import com.ispw.tryeshifts.appcontroller.LoginAC;
import com.ispw.tryeshifts.bean.SessionContext;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.excpetion.BaseException;
import com.ispw.tryeshifts.graphcontroller.cli.SignUpCLI;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.Scanner;
import java.util.logging.Logger;

public class Main extends Application {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    public void start(Stage primaryStage) throws Exception
    {
        SceneManager manager = SceneManager.getInstance();
        manager.setPrimaryStage(primaryStage);
        String savedEmail = SessionContext.getInstance().getSavedEmail();
        if(savedEmail != null){
            try{
                UserBean user = LoginAC.autoLogin(savedEmail);
                SessionContext.getInstance().setLoggeduser(user);
                manager.switchScene("Home.fxml","E-Shifts - Home", 900, 600);
                return;
            }catch(BaseException e){
                SessionContext.getInstance().clearPreferences();
            }
        }
        manager.switchScene("SignUp.fxml","E-Shifts - Sign UP", 850,550);
    }
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        LOGGER.info("Seleziona interfaccia: [1] GUI (JavaFX) | [2] CLI (Console)");
        String choice = scanner.nextLine();

        if ("1".equals(choice)) {
            // Avviamo JavaFX tramite una classe dedicata
            JavaFXLauncher.startApp(args);
        } else if ("2".equals(choice)) {
            // Avviamo la CLI
            SignUpCLI.start();
        } else {
            LOGGER.severe("Scelta non valida.");
        }
    }
}
