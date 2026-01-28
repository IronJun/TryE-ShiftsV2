package com.ispw.tryeshifts.graphcontroller.cli;

import com.ispw.tryeshifts.appcontroller.SignupAC;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.excpetion.BaseException;
import com.ispw.tryeshifts.graphcontroller.cli.utilitiesCLI.CLIReader;

import java.util.logging.Logger;

public class SignUpCLI {
    private static final Logger LOGGER = Logger.getLogger(SignUpCLI.class.getName());
    public static void start(){
        boolean success = false;
        LOGGER.info("\n--- REGISTRAZIONE E-SHIFTS ---");

        UserBean user = new UserBean();
        user.setEmail(collectEmail());
        user.setName(collectName("Nome"));
        user.setSurname(collectName("Cognome"));
        user.setPassword(collectPassword());


        try{
            SignupAC.registerUser(user);
            LOGGER.info("Registrazione avvenuta con successo!");
            //new LoginCLI().start();
        }catch(BaseException e){
            LOGGER.severe("ERRORE" + e.getMessage());
        }
    }
    private static String collectEmail() {
        while (true) {
            String email = CLIReader.readString("Email: ");
            if (email.contains("@") && email.contains(".")) {
                return email; // Email sintatticamente valida
            }
            LOGGER.warning("Formato email non valido! Riprova.");
        }
    }
    private static String collectName(String kind){
        while(true){
            String name = CLIReader.readString(kind+": ");
            if(name != null && name.matches("[a-zA-ZÀ-ÿ\\\\s'-]+")){
                return name.trim();
            }
            LOGGER.warning("Nome non valido! Riprova.");
        }
    }
    private static String collectPassword() {
        while (true) {
            String p1 = CLIReader.readString("Password: ");
            String p2 = CLIReader.readString("Conferma Password: ");
            if (p1.equals(p2) && p1.length() >= 8) {
                return p1;
            }
            LOGGER.warning("Le password non coincidono o sono troppo corte (min 8 car). Riprova.");
        }
    }

}
