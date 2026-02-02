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

        boolean MemValidation = false;
        boolean login = false;

        while(!login) {
            try {
                String quit = CLIReader.readString("Press Q to SignUP if you don't have an account...\n").toUpperCase();
                if(quit.equalsIgnoreCase("Q")) {
                    login = true;
                    break;
                } else {
                    UserBean inputUser = new UserBean(CLIReader.readString("Email: "), CLIReader.readString("Password: "));

                    UserBean loggedUser = LoginAC.loginUser(inputUser);
                    SessionContext.getInstance().setLoggeduser(loggedUser);
                    login = true;
                    while (!MemValidation) {
                        String Mem = CLIReader.readString("Do you want the system to remember your credentials? Y/n : \n  ").toUpperCase();

                        if (Mem.equals("Y")) {
                            SessionContext.getInstance().saveUserToPreferences(inputUser.getEmail());
                            MemValidation = true;
                        } else if (Mem.equals("n")) {
                            SessionContext.getInstance().clearPreferences();
                            MemValidation = true;
                        } else LOGGER.severe("ERROR: You can insert just 'y' as yes or 'n' as no \n");
                    }
                    LOGGER.info("Login completed correctly!! \n");
                    HomeCLI.start();
                }
            } catch (InvalidCredentialException e) {
                LOGGER.severe("ERRORE: " + e.getMessage() +"\n");
            }catch (BaseException e){
                LOGGER.severe("ERRORE: " + e.getMessage() +"\n");
            }
        }
    }

}
