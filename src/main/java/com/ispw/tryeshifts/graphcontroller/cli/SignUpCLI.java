package com.ispw.tryeshifts.graphcontroller.cli;

import com.ispw.tryeshifts.appcontroller.SignupAC;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.excpetion.BaseException;
import com.ispw.tryeshifts.graphcontroller.cli.utilitiesCLI.CLIReader;
import com.ispw.tryeshifts.graphcontroller.cli.utilitiesCLI.Collectors;

import java.util.logging.Logger;


public class SignUpCLI {
    private static final Logger LOGGER = Logger.getLogger(SignUpCLI.class.getName());
    public static void start()  {
        boolean EmailValid = false;
        boolean NameValid = false;
        boolean SurnameValid = false;
        boolean PasswordValid = false;
        boolean AccVal = false;
        String acc = "";
        String email = "";
        String name = "";
        String surname = "";
        String pwd = "";
        String pwdRepeat = "";

        LOGGER.info("--- Welcome in E-Shifts ---\n");
        while(!AccVal){
            acc = CLIReader.readString("Do you already have an account? Y/n : ");
            AccVal = acc.equals("Y") || acc.equals("n");
            if(!AccVal){ LOGGER.warning("you can insert just 'y' as yes or 'n' as no \n");}
        }
        if(acc.equals("Y")){
            try{
                LoginCLI.start();

            }catch(BaseException e){
                LOGGER.severe("ERRORE" + e.getMessage());
            }
        }else {
            LOGGER.info("\n--- REGISTRAZIONE E-SHIFTS ---\n");


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

            UserBean user = new UserBean(email, pwd, name, surname, pwdRepeat);
            try {
                SignupAC.registerUser(user);
                LOGGER.info("Registrazione avvenuta con successo!\n");
                LoginCLI.start();
            } catch (BaseException e) {
                LOGGER.severe("ERRORE" + e.getMessage());
            }
        }
    }
}
