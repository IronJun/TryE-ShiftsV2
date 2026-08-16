package com.ispw.tryeshifts.graphcontroller.cli;

import com.ispw.tryeshifts.appcontroller.SettingsAC;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.excpetion.BaseException;
import com.ispw.tryeshifts.graphcontroller.cli.utilities.CLIReader;
import java.util.logging.Logger;


public class SettingsCLI {
    private final Logger logger = Logger.getLogger(SettingsCLI.class.getName());
    private final NewWorkplaceCLI newWorkplaceCLI = new NewWorkplaceCLI();
    private final SettingsAC ac = new SettingsAC();

    public  void accountSettings(UserBean user){
        boolean back = false;

        while (!back) {
            logger.info("\n--- USER SETTINGS ---");
            logger.info("\nEmail: " + user.getEmail() + " (Non modificabile)");
            logger.info("\n1. NAME: " + user.getName());
            logger.info("\n2. SURNAME: " + user.getSurname());
            logger.info("\n3. Password: ********");
            logger.info("\n0. Torna indietro");

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
                    logger.warning("Scelta non valida.");
            }
        }
    }

    public  void workplaceSettings(WorkplaceBean wp){
        boolean back = false;
        String oldname = wp.getWorkplaceName();

        while (!back) {


            logger.info("\n--- IMPOSTAZIONI WORKPLACE: " + wp.getWorkplaceName() + " ---");
            logger.info("1. Nome: " + wp.getWorkplaceName());
            logger.info("2. Indirizzo: " + wp.getAddress());
            logger.info("3. Giorni Operativi: " + wp.getSelectedDays());
            logger.info("4. Fasce Orarie: " + wp.getShiftsBean());
            logger.info("0. Torna indietro");

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
                    logger.warning("Scelta non valida.");
            }

            try {
                ac.updateWorkplace(wp, oldname);

                oldname = wp.getWorkplaceName();

                logger.info("\n✅ Modifica salvata. Vuoi cambiare altro? (y/n)");
                String answer = CLIReader.readString("").toUpperCase();
                if (!answer.equals("Y")) back = true;

            } catch (BaseException e) {
                logger.severe("❌ Errore durante l'aggiornamento: " + e.getMessage());
            }
        }
    }

    private  void updateUser(UserBean user) {
        try {
            // Usa l'App Controller che gestisce il profilo (es. LoginAC o ProfileAC)
            ac.updateUserProfile(user);
            logger.info("✅ Profilo aggiornato correttamente!");
        } catch (BaseException e) {
            logger.severe("❌ Errore durante l'aggiornamento: " + e.getMessage());
        }
    }
}
