package com.ispw.tryeshifts.graphcontroller.cli;

import com.ispw.tryeshifts.appcontroller.LoginAC;
import com.ispw.tryeshifts.bean.SessionContext;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.excpetion.BaseException;
import com.ispw.tryeshifts.excpetion.InvalidCredentialException;
import com.ispw.tryeshifts.graphcontroller.cli.utilitiesCLI.CLIReader;

import java.util.logging.Logger;


public class LoginCLI{
    private static final Logger LOGGER = Logger.getLogger(LoginCLI.class.getName());

    public LoginCLI(){
        throw new IllegalStateException("Utility class");
    }

    public static void start() throws BaseException {
        LOGGER.info("\n--- LOGIN E-SHIFTS ---\n");
        LOGGER.info("Inserisci le tue credenziali per accedere al sistema.\n");
//        boolean login = false;

        while(true) {
            try {
                LOGGER.info("Press Q to SignUP if you don't have an account at any moment\n");
                String email = CLIReader.readString("Email: ");
                if(email.equalsIgnoreCase("Q")){
                    return;
                }
                String password = CLIReader.readString("Password: ");
                if (password.equalsIgnoreCase("Q")) {
                    return;
                }
                UserBean inputUser = new UserBean(email, password);
                UserBean loggedUser = LoginAC.loginUser(inputUser);

                SessionContext.getInstance().setLoggeduser(loggedUser);

                handleRememberMe(inputUser);

                LOGGER.info("Login completed correctly!! \n");
                HomeCLI.start();
                return;

            } catch (InvalidCredentialException e) {
                LOGGER.severe("ERRORE: " + e.getMessage() +"\n");
            }catch (BaseException e){
                LOGGER.severe("ERRORE: " + e.getMessage() +"\n");
            }
        }
    }

    private static void handleRememberMe(UserBean user){
        boolean memValidation = false;
        while (!memValidation) {
            String mem = CLIReader.readString("Do you want to remember credentials? Y/n: ").toUpperCase();
            if (mem.equals("Y")) {
                SessionContext.getInstance().saveUserToPreferences(user.getEmail());
                memValidation = true;
            } else if (mem.equals("N")) {
                SessionContext.getInstance().clearPreferences();
                memValidation = true;
            } else {
                LOGGER.warning("Insert 'Y' or 'N'\n");
            }
        }
    }

}
