package com.ispw.tryeshifts.graphcontroller.cli;

import com.ispw.tryeshifts.appcontroller.SettingsAC;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.excpetion.BaseException;
import com.ispw.tryeshifts.graphcontroller.cli.utilities.CLIReader;
import java.util.logging.Logger;


public class SettingsCLI {
    private final Logger LOGGER = Logger.getLogger(SettingsCLI.class.getName());
    private final NewWorkplaceCLI newWorkplaceCLI = new NewWorkplaceCLI();
    private final SettingsAC ac = new SettingsAC();

    public SettingsCLI(){

    }
    public  void accountSettings(UserBean user){
        boolean back = false;

        while (!back) {
            LOGGER.info("\n--- USER SETTINGS ---");
            LOGGER.info("\nEmail: " + user.getEmail() + " (Non modificabile)");
            LOGGER.info("\n1. NAME: " + user.getName());
            LOGGER.info("\n2. SURNAME: " + user.getSurname());
            LOGGER.info("\n3. Password: ********");
            LOGGER.info("\n0. Torna indietro");

            String choice = CLIReader.readString("\nSeleziona il numero del campo da modificare: ");

            switch (choice) {
                case "1":
                    String newName = CLIReader.readString("Nuovo nome: ");
                    user.setName(newName);
                    updateUser(user);
                    break;
                case "2":
                    String newSurname = CLIReader.readString("Nuovo cognome: ");
                    user.setSurname(newSurname);
                    updateUser(user);
                    break;
                case "3":
                    String newPass = CLIReader.readString("Nuova password: ");
                    user.setPassword(newPass);
                    updateUser(user);
                    break;
                case "0":
                    back = true;
                    break;
                default:
                    LOGGER.warning("Scelta non valida.");
            }
        }
    }

    public  void workplaceSettings(WorkplaceBean wp){
        boolean back = false;
        String oldname = wp.getWorkplaceName();

        while (!back) {


            LOGGER.info("\n--- IMPOSTAZIONI WORKPLACE: " + wp.getWorkplaceName() + " ---");
            LOGGER.info("1. Nome: " + wp.getWorkplaceName());
            LOGGER.info("2. Indirizzo: " + wp.getAddress());
            LOGGER.info("3. Giorni Operativi: " + wp.getSelectedDays());
            LOGGER.info("4. Fasce Orarie: " + wp.getShiftsBean());
            LOGGER.info("0. Torna indietro");

            String choice = CLIReader.readString("\nCosa vuoi modificare?: ");

            switch (choice) {
                case "1":
                    wp.setWorkplaceName(CLIReader.readString("Nuovo nome: "));
                    break;
                case "2":
                    wp.setAddress(CLIReader.readString("Nuovo indirizzo: "));
                    break;
                case "3":
                    wp.setSelectedDays(newWorkplaceCLI.selectOperatingDays());
                    break;
                case "4":
                    wp.setShiftsBean(newWorkplaceCLI.defineTimeSlots());
                    break;
                case "0":
                    back = true;
                    continue; // Salta il salvataggio se vuoi solo uscire
                default:
                    LOGGER.warning("Scelta non valida.");
            }

            try {
                ac.updateWorkplace(wp, oldname);

                oldname = wp.getWorkplaceName();

                LOGGER.info("\n✅ Modifica salvata. Vuoi cambiare altro? (y/n)");
                String answer = CLIReader.readString("").toUpperCase();
                if (!answer.equals("Y")) back = true;

            } catch (BaseException e) {
                LOGGER.severe("❌ Errore durante l'aggiornamento: " + e.getMessage());
            }
        }
    }

    private  void updateUser(UserBean user) {
        try {
            // Usa l'App Controller che gestisce il profilo (es. LoginAC o ProfileAC)
            ac.updateUserProfile(user);
            LOGGER.info("✅ Profilo aggiornato correttamente!");
        } catch (BaseException e) {
            LOGGER.severe("❌ Errore durante l'aggiornamento: " + e.getMessage());
        }
    }
}
