package com.ispw.tryeshifts.graphcontroller.cli;

import com.ispw.tryeshifts.appcontroller.LoginAC;
import com.ispw.tryeshifts.appcontroller.SignupAC;
import com.ispw.tryeshifts.bean.SessionContext;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.excpetion.BaseException;
import com.ispw.tryeshifts.graphcontroller.cli.utilitiesCLI.CLIReader;

import java.util.logging.Logger;


public class SignUpCLI {


    private static final Logger LOGGER = Logger.getLogger(SignUpCLI.class.getName());

    public static void start() {
        // 1. L'AUTO-LOGIN va fatto SOLO QUI (una volta all'avvio)
        try {
            String savedEmail = SessionContext.getInstance().getSavedEmail();
            if (savedEmail != null) {
                UserBean user = LoginAC.autoLogin(savedEmail);
                SessionContext.getInstance().setLoggeduser(user);
                HomeCLI.start();
                // Se l'utente fa Logout dalla Home, il codice prosegue sotto nel loop.
            }
        } catch (BaseException e) {
            LOGGER.warning("Auto-login failed: " + e.getMessage());
            SessionContext.getInstance().clearPreferences();
        }

        // 2. Loop principale di navigazione
        while (true) {
            boolean accVal = false;
            String acc = "";

            LOGGER.info("--- Welcome to E-Shifts ---\n");

            // RIMOSSO IL BLOCCO AUTO-LOGIN DA QUI DENTRO!

            while (!accVal) {
                acc = CLIReader.readString("Do you already have an account? Y/n (or 0 to exit): ").toUpperCase();
                if (acc.equals("0")) System.exit(0);

                accVal = acc.equals("Y") || acc.equals("N");
                if (!accVal) {
                    LOGGER.warning("You can insert just 'y' as yes or 'n' as no.\n");
                }
            }

            if (acc.equals("Y")) {
                try {
                    LoginCLI.start();
                } catch (BaseException e) {
                    LOGGER.severe("ERRORE: " + e.getMessage());
                }
            } else {
                performRegistration();
            }
        }
    }

    private static void performRegistration() {
        boolean EmailValid = false;
        boolean NameValid = false;
        boolean SurnameValid = false;
        boolean PasswordValid = false;
        String email = "";
        String name = "";
        String surname = "";
        String pwd = "";
        String pwdRepeat = "";
        while (!EmailValid) {
            email = CLIReader.readString("Email: ");
            EmailValid = email.contains("@") && email.contains(".");
            if (!EmailValid) {
                LOGGER.warning("Formato email non valido! Riprova.\n");
            }
        }
        while (!NameValid) {
            name = CLIReader.readString("Name: ");
            NameValid = name != null && name.matches("[a-zA-ZÀ-ÿ\\\\s'-]+");
            if (!NameValid) {
                LOGGER.warning("Nome non valido! Riprova.\n");
            }
        }
        while (!SurnameValid) {
            surname = CLIReader.readString("Surname: ");
            SurnameValid = surname != null && surname.matches("[a-zA-ZÀ-ÿ\\\\s'-]+");
            if (!SurnameValid) {
                LOGGER.warning("Cognome non valido! Riprova.\n");
            }
        }
        while (!PasswordValid) {
            pwd = CLIReader.readString("Password: ");
            pwdRepeat = CLIReader.readString("Conferma Password: ");
            PasswordValid = pwd.length() >= 6 && pwd.equals(pwdRepeat);
            if (!PasswordValid) {
                if (pwd.length() < 6) {
                    LOGGER.warning("La password deve essere di almeno 6 caratteri! Riprova.\n");
                }
                if (!pwd.equals(pwdRepeat)) {
                    LOGGER.warning("Le password non coincidono! Riprova.\n");
                }
            }
        }
        try{
            UserBean user = new UserBean(email, pwd, name, surname, pwdRepeat);

            // 2. Chiamiamo l'App Controller per la persistenza
            SignupAC.registerUser(user);

            LOGGER.info("\n✅ Registrazione avvenuta con successo! Ora puoi effettuare il login.\n");

            // Non serve chiamare LoginCLI.start() qui, perché il loop nel metodo start()
            // ricomincerà e chiederà "Do you already have an account?", permettendo all'utente di premere 'Y'.

        } catch (BaseException e) {
            LOGGER.severe("❌ Errore durante la registrazione: " + e.getMessage() + "\n");
        }
    }
}
