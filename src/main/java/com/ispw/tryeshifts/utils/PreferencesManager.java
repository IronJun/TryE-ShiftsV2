package com.ispw.tryeshifts.utils;

import com.ispw.tryeshifts.session.SessionContext;

import java.util.prefs.Preferences;

public class PreferencesManager {
    private static final String PREF_EMAIL = "last_logged_email";

    private PreferencesManager(){
        throw new IllegalStateException("utility clss ");
    }

    public static void saveUserToPreferences(String email){
        Preferences prefs = Preferences.userNodeForPackage(SessionContext.class);
        prefs.put(PREF_EMAIL, email);
    }

    public static String getSavedEmail(){
        Preferences prefs = Preferences.userNodeForPackage(SessionContext.class);
        return prefs.get(PREF_EMAIL, null);
    }

    public static void clearPreferences(){
        Preferences prefs = Preferences.userNodeForPackage(SessionContext.class);
        prefs.remove(PREF_EMAIL);
    }
}
