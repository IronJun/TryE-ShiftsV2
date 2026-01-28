package com.ispw.tryeshifts.bean;

//classe che funziona da cassetto per il passaggio dei dati tra le view, singleton

import com.ispw.tryeshifts.SceneManager;
import com.ispw.tryeshifts.appcontroller.ManageMembersAC;
import com.ispw.tryeshifts.excpetion.BaseException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;
import com.ispw.tryeshifts.graphcontroller.utilities.ErrorViewManager;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;
import java.util.prefs.Preferences;

public class SessionContext {
    private static SessionContext instance;
    private UserBean loggeduser;
    private WorkplaceBean loggedWorkplace;
    private static final String PREF_EMAIL = "last_logged_email";

    private SessionContext(){}

    public static SessionContext getInstance(){
        if(instance == null) instance = new SessionContext();
        return instance;
    }

    public boolean logoutConfirmation(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText("Sei sicuro di voler effettuare il logout?");
        Optional<ButtonType> result = alert.showAndWait();

        return result.isPresent() && result.get() == ButtonType.OK;

    }
    public void saveUserToPreferences(String email){
        Preferences prefs = Preferences.userNodeForPackage(SessionContext.class);
        prefs.put(PREF_EMAIL, email);
    }
    public String getSavedEmail(){
        Preferences prefs = Preferences.userNodeForPackage(SessionContext.class);
        return prefs.get(PREF_EMAIL, null);
    }
    public void clearPreferences(){
        Preferences prefs = Preferences.userNodeForPackage(SessionContext.class);
        prefs.remove(PREF_EMAIL);
    }
    public UserBean getLoggeduser() {return loggeduser;}
    public void setLoggeduser(UserBean loggeduser) {this.loggeduser = loggeduser;}
    public WorkplaceBean getLoggedWorkplace() {return loggedWorkplace;}
    public void setLoggedWorkplace(WorkplaceBean loggedWorkplace) {this.loggedWorkplace = loggedWorkplace;}
}
