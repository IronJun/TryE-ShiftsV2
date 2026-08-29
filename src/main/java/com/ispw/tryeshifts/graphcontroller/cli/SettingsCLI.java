package com.ispw.tryeshifts.graphcontroller.cli;

import com.ispw.tryeshifts.appcontroller.SettingsAC;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.exception.BaseException;
import com.ispw.tryeshifts.graphcontroller.cli.utilities.CLIService;
import java.util.logging.Logger;


public class SettingsCLI {
    private final Logger logger = Logger.getLogger(SettingsCLI.class.getName());
    private final NewWorkplaceCLI newWorkplaceCLI = new NewWorkplaceCLI();
    private final SettingsAC ac = new SettingsAC();

    public  void accountSettings(UserBean user){
        boolean back = false;

        while (!back) {
            CLIService.println("--- USER SETTINGS ---");
            CLIService.println("Email: " + user.getEmail() + " (Non modificabile)");
            CLIService.println("1. NAME: " + user.getName());
            CLIService.println("2. SURNAME: " + user.getSurname());
            CLIService.println("3. Password: ********");
            CLIService.println("0. Torna indietro");

            String choice = CLIService.readString("Seleziona il numero del campo da modificare: ");

            switch (choice) {
                case "1":
                    String newName = CLIService.readString("Nuovo nome: ");
                    user.setName(newName);
                    updateUser(user);
                    break;
                case "2":
                    String newSurname = CLIService.readString("Nuovo cognome: ");
                    user.setSurname(newSurname);
                    updateUser(user);
                    break;
                case "3":
                    String newPass = CLIService.readString("Nuova password: ");
                    user.setPassword(newPass);
                    updateUser(user);
                    break;
                case "0":
                    back = true;
                    break;
                default:
                    logger.warning("Scelta non valida.\n");
            }
        }
    }

    public  void workplaceSettings(WorkplaceBean wp){
        boolean back = false;
        String oldname = wp.getWorkplaceName();

        while (!back) {


            CLIService.println("--- IMPOSTAZIONI WORKPLACE: " + wp.getWorkplaceName() + " ---");
            CLIService.println("1. Nome: " + wp.getWorkplaceName());
            CLIService.println("2. Indirizzo: " + wp.getAddress());
            CLIService.println("3. Giorni Operativi: " + wp.getSelectedDays());
            CLIService.println("4. Fasce Orarie: " + wp.getShiftsBean());
            CLIService.println("0. Torna indietro");

            String choice = CLIService.readString("Cosa vuoi modificare?: ");

            switch (choice) {
                case "1":
                    wp.setWorkplaceName(CLIService.readString("Nuovo nome: "));
                    break;
                case "2":
                    wp.setAddress(CLIService.readString("Nuovo indirizzo: "));
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
                    logger.warning("Scelta non valida.\n");
            }

            try {
                ac.updateWorkplace(wp, oldname);

                oldname = wp.getWorkplaceName();

                CLIService.println("✅ Modifica salvata. Vuoi cambiare altro? (y/n)");
                String answer = CLIService.readString("").toUpperCase();
                if (!answer.equals("Y")) back = true;

            } catch (BaseException e) {
                logger.severe("❌ Errore durante l'aggiornamento: " + e.getMessage()+"\n");
            }
        }
    }

    private  void updateUser(UserBean user) {
        try {
            // Usa l'App Controller che gestisce il profilo (es. LoginAC o ProfileAC)
            ac.updateUserProfile(user);
            CLIService.println("✅ Profilo aggiornato correttamente!");
        } catch (BaseException e) {
            logger.severe("❌ Errore durante l'aggiornamento: " + e.getMessage()+"\n");
        }
    }
}
