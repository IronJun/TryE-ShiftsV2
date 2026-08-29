package com.ispw.tryeshifts.graphcontroller.cli;

import com.ispw.tryeshifts.appcontroller.LoginAC;
import com.ispw.tryeshifts.session.SessionContext;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.exception.BaseException;
import com.ispw.tryeshifts.exception.InvalidCredentialException;
import com.ispw.tryeshifts.graphcontroller.cli.utilities.CLIReader;
import com.ispw.tryeshifts.utils.PreferencesManager;

import java.util.logging.Logger;


public class LoginCLI{
    private  final Logger logger = Logger.getLogger(LoginCLI.class.getName());
    private final HomeCLI homeCLI = new HomeCLI();


    public  void start() throws BaseException {
        CLIReader.println("--- LOGIN E-SHIFTS ---");
        CLIReader.println("Insert your credential to get in the App.");

        while(true) {
            try {
                CLIReader.println("Press Q to SignUP if you don't have an account at any moment");
                String email = CLIReader.readString("Email: ");
                if(email.equalsIgnoreCase("Q")){
                    return;
                }
                String password = CLIReader.readPassword("Password: ");
                if (password.equalsIgnoreCase("Q")) {
                    return;
                }
                UserBean inputUser = new UserBean(email, password);
                UserBean loggedUser = new LoginAC().loginUser(inputUser);

                SessionContext.getInstance().setLoggeduser(loggedUser);

                handleRememberMe(inputUser);

                CLIReader.println("Login completed correctly!! ");
                homeCLI.start();
                return;

            } catch (InvalidCredentialException e) {
                logger.severe("ERROR: " + e.getMessage() +"\n");
            }catch (BaseException e){
                logger.severe("ERROR: " + e.getMessage() +"\n");
            }
        }
    }

    private  void handleRememberMe(UserBean user){
        boolean memValidation = false;
        while (!memValidation) {
            String mem = CLIReader.readString("Do you want to remember credentials? Y/n: ").toUpperCase();
            if (mem.equals("Y")) {
                PreferencesManager.saveUserToPreferences(user.getEmail());
                memValidation = true;
            } else if (mem.equals("N")) {
                PreferencesManager.clearPreferences();
                memValidation = true;
            } else {
                logger.warning("Insert 'Y' or 'N'\n");
            }
        }
    }

}
