package com.ispw.tryeshifts;


import com.ispw.tryeshifts.graphcontroller.cli.SignUpCLI;
import com.ispw.tryeshifts.graphcontroller.cli.utilities.CLIService;
import com.ispw.tryeshifts.graphcontroller.cli.utilities.Configurator;
import javafx.application.Application;

import java.util.logging.*;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        // 1. Configurazione globale (Log, DB, etc.)
        Configurator.configureLogger();
        // 2. Scelta dell'interfaccia
        LOGGER.info("Seleziona interfaccia: [1] GUI | [2] CLI");
        int choice = CLIService.readInt(" >");
        switch(choice) {
            case 1:
                Application.launch(JavaFXLauncher.class, args);
                break;
            case 2:
                SignUpCLI signup = new SignUpCLI();
                signup.start();
                break;
            default:
                LOGGER.severe("Scelta non valida. Uscita...");
        }
    }
}

