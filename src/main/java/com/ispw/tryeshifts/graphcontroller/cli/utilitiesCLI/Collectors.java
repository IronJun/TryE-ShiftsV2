package com.ispw.tryeshifts.graphcontroller.cli.utilitiesCLI;

import java.util.logging.Logger;

public class Collectors {
    private static final Logger LOGGER = Logger.getLogger(Collectors.class.getName());

    public static String collectEmail() {
        while (true) {
            String email = CLIReader.readString("Email: ");
            if (email.contains("@") && email.contains(".")) {
                return email; // Email sintatticamente valida
            }
            LOGGER.warning("Formato email non valido! Riprova.");
        }
    }
    public static String collectName(String kind){
        while(true){
            String name = CLIReader.readString(kind+": ");
            if(name != null && name.matches("[a-zA-ZÀ-ÿ\\\\s'-]+")){
                return name.trim();
            }
            LOGGER.warning("Nome non valido! Riprova.");
        }
    }
    public static String collectPassword(boolean repeat) {
        while (true) {
            String p1 = CLIReader.readString("Password: ");
            if (repeat) {
                String p2 = CLIReader.readString("Conferma Password: ");
                if (p1.equals(p2) && p1.length() >= 8) {
                    return p1;
                }
                LOGGER.warning("Le password non coincidono o sono troppo corte (min 8 car). Riprova.");

            }
            return p1;
        }
    }
}
