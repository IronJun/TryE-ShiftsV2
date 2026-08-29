package com.ispw.tryeshifts.graphcontroller.cli;

import com.ispw.tryeshifts.appcontroller.LoginAC;
import com.ispw.tryeshifts.appcontroller.SignupAC;
import com.ispw.tryeshifts.session.SessionContext;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.exception.BaseException;
import com.ispw.tryeshifts.graphcontroller.cli.utilities.CLIService;
import com.ispw.tryeshifts.utils.PreferencesManager;

import java.util.logging.Logger;
import java.util.regex.Pattern;


public class SignUpCLI {
    private final Logger logger = Logger.getLogger(SignUpCLI.class.getName());
    private final HomeCLI homeCLI = new HomeCLI() ;
    private final LoginCLI loginCLI = new LoginCLI();
    private final Pattern format = Pattern.compile("[a-zA-Z0-9_-]+");
    public  void start() {
        // 1. L'AUTO-LOGIN va fatto SOLO QUI (una volta all'avvio)
        try {
            String savedEmail = PreferencesManager.getSavedEmail();
            if (savedEmail != null) {
                UserBean user = new LoginAC().autoLogin(savedEmail);
                SessionContext.getInstance().setLoggeduser(user);
                homeCLI.start();
                // Se l'utente fa Logout dalla Home, il codice prosegue sotto nel loop.
            }
        } catch (BaseException e) {
            logger.warning("Auto-login failed: " + e.getMessage()+"\n");
            PreferencesManager.clearPreferences();
        }

        // 2. Loop principale di navigazione
        while (true) {
            preRegistration();
        }
    }
    private  void preRegistration(){
        boolean accVal = false;
        String acc = "";
        while (!accVal) {
            acc = CLIService.readString("Do you already have an account? Y/n (or 0 to exit): ").toUpperCase();
            if (acc.equals("0")) System.exit(0);

            accVal = acc.equals("Y") || acc.equals("N");
            if (!accVal) {
                logger.warning("You can insert just 'y' as yes or 'n' as no.\n");
            }
        }

        if (acc.equals("Y")) {
            try {
                loginCLI.start();
            } catch (BaseException e) {
                logger.severe("ERROR: " + e.getMessage()+"\n");
            }
        } else {
            performRegistration();
        }
    }
    private  void performRegistration() {
        boolean passwordValid = false;
        String pwd = "";
        String pwdRepeat = "";


        try{
            String email = emailRegister();
            String name = nameRegister();
            String surname = nameRegister();
            while (!passwordValid) {
                pwd = CLIService.readString("Password: ");
                pwdRepeat = CLIService.readString("Confirm Password: ");
                passwordValid = pwd.length() >= 6 && pwd.equals(pwdRepeat);
                if (!passwordValid) {
                    if (pwd.length() < 6) {
                        logger.warning("The password must be at least 6 characters\n.");
                    }
                    if (!pwd.equals(pwdRepeat)) {
                        logger.warning("Password doesn't match\n");
                    }
                }
            }
            UserBean user = new UserBean(email, pwd, name, surname, pwdRepeat);
            new SignupAC().registerUser(user);

            CLIService.println("✅ Regsitration completed succesfully now you can move on to the login");

            // Non serve chiamare LoginCLI.start() qui, perché il loop nel metodo start()
            // ricomincerà e chiederà "Do you already have an account?", permettendo all'utente di premere 'Y'.

        } catch (BaseException e) {
            logger.severe("❌ Error during registration: " + e.getMessage() + "\n");
        }
    }
    private  String emailRegister(){
        boolean emailValid = false;
        String email = "";
        while (!emailValid) {
            email = CLIService.readString("Email: ");
            emailValid = email.contains("@") && email.contains(".");
            if (!emailValid) {
                logger.warning("Mail format invalid, retry.\n");
            }
        }
        return email;
    }

    private  String nameRegister(){
        boolean nameValid = false;
        String name = "";
        while (!nameValid) {
            name = CLIService.readString("Name: ");
            nameValid = name != null && format.matcher(name).matches();
            if (!nameValid) {
                logger.warning("Name invalid, retry.\n");
            }
        }
        return name;
    }
}
