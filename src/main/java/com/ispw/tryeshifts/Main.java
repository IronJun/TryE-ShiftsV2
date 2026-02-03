package com.ispw.tryeshifts;

//import com.ispw.tryeshifts.dao.JDBC;
import com.ispw.tryeshifts.appcontroller.LoginAC;
import com.ispw.tryeshifts.bean.SessionContext;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.excpetion.BaseException;
import com.ispw.tryeshifts.graphcontroller.cli.SignUpCLI;
import com.ispw.tryeshifts.graphcontroller.cli.utilitiesCLI.CLIReader;
import com.ispw.tryeshifts.graphcontroller.cli.utilitiesCLI.Configurator;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.Scanner;
import java.util.logging.*;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        // 1. Configurazione globale (Log, DB, etc.)
        Configurator.configureLogger();
        // 2. Scelta dell'interfaccia
        LOGGER.info("Seleziona interfaccia: [1] GUI | [2] CLI");
        int choice = CLIReader.readInt(" >");

        if (choice == 1) {
            // Avvia JavaFX (che internamente gestisce il proprio loop)
            Application.launch(JavaFXLauncher.class, args);
        } else if (choice == 2) {
            // Avvia la CLI
            SignUpCLI.start();
        } else {
            LOGGER.severe("Scelta non valida. Uscita...");
        }
    }
}
//
//    public void start(Stage primaryStage) throws Exception {
//        SceneManager manager = SceneManager.getInstance();
//        manager.setPrimaryStage(primaryStage);
//        String savedEmail = SessionContext.getInstance().getSavedEmail();
//        if(savedEmail != null){
//            try{
//                UserBean user = LoginAC.autoLogin(savedEmail);
//                SessionContext.getInstance().setLoggeduser(user);
//                manager.switchScene("Home.fxml","E-Shifts - Home", 900, 600);
//                return;
//            }catch(BaseException e){
//                SessionContext.getInstance().clearPreferences();
//            }
//        }
//        manager.switchScene("SignUp.fxml","E-Shifts - Sign UP", 850,550);
//    }
//
//    public static void main(String[] args) {
//        Scanner scanner = new Scanner(System.in);
//        configureCLIOutput();
//        LOGGER.info("Seleziona interfaccia: [1] GUI (JavaFX) | [2] CLI (Console)");
//        String choice = scanner.nextLine();
//
//        if ("1".equals(choice)) {
//            // Avviamo JavaFX tramite una classe dedicata
//            JavaFXLauncher.startApp(args);
//        } else if ("2".equals(choice)) {
//            // Avviamo la CLI
//            SignUpCLI.start();
//        } else {
//            LOGGER.severe("Scelta non valida.");
//        }
//    }
//
//    public static void configureCLIOutput() {
//        Logger rootLogger = Logger.getLogger("");
//        // Puliamo tutti gli handler esistenti
//        for (Handler handler : rootLogger.getHandlers()) {
//            rootLogger.removeHandler(handler);
//        }
//
//        // Creiamo un handler che scrive su System.out
//        StreamHandler stdoutHandler = new StreamHandler(System.out, new Formatter() {
//            @Override
//            public String format(LogRecord record) {
//                // RESTITUISCE SOLO IL MESSAGGIO.
//                // Niente data, niente ora, niente "vai a capo" automatico.
//                return record.getMessage();
//            }
//        }) {
//            @Override
//            public synchronized void publish(LogRecord record) {
//                super.publish(record);
//                flush(); // Fondamentale per vedere l'output prima dell'input
//            }
//        };
//
//        rootLogger.addHandler(stdoutHandler);
//    }

