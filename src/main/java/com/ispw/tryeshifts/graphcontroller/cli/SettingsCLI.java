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
            CLIService.println("Email: " + user.getEmail() + " (Not editable)");
            CLIService.println("1. NAME: " + user.getName());
            CLIService.println("2. SURNAME: " + user.getSurname());
            CLIService.println("3. Password: ********");
            CLIService.println("0. to go back");

            String choice = CLIService.readString("Select the field you want to update: ");

            switch (choice) {
                case "1":
                    String newName = CLIService.readString("New name: ");
                    user.setName(newName);
                    updateUser(user);
                    break;
                case "2":
                    String newSurname = CLIService.readString("New surname: ");
                    user.setSurname(newSurname);
                    updateUser(user);
                    break;
                case "3":
                    String newPass = CLIService.readString("New password: ");
                    user.setPassword(newPass);
                    updateUser(user);
                    break;
                case "0":
                    back = true;
                    break;
                default:
                    logger.warning("Invalid choice.\n");
            }
        }
    }

    public  void workplaceSettings(WorkplaceBean wp){
        boolean back = false;
        String oldname = wp.getWorkplaceName();

        while (!back) {


            CLIService.println("---  WORKPLACE SETTINGS: " + wp.getWorkplaceName() + " ---");
            CLIService.println("1. Name: " + wp.getWorkplaceName());
            CLIService.println("2. Address: " + wp.getAddress());
            CLIService.println("3. Working days: " + wp.getSelectedDays());
            CLIService.println("4. Shifts: " + wp.getShiftsBean());
            CLIService.println("0. To go back");

            String choice = CLIService.readString("Select the field you want to update: ");

            switch (choice) {
                case "1":
                    wp.setWorkplaceName(CLIService.readString("New name: "));
                    break;
                case "2":
                    wp.setAddress(CLIService.readString("New address: "));
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
                    logger.warning("Invalid choice.\n");
            }

            try {
                ac.updateWorkplace(wp, oldname);

                oldname = wp.getWorkplaceName();

                CLIService.println("✅ Update saved. Are you willing to change something else? (y/n)");
                String answer = CLIService.readString("").toUpperCase();
                if (!answer.equals("Y")) back = true;

            } catch (BaseException e) {
                logger.severe("❌ Error during the update: " + e.getMessage()+"\n");
            }
        }
    }

    private  void updateUser(UserBean user) {
        try {
            // Usa l'App Controller che gestisce il profilo (es. LoginAC o ProfileAC)
            ac.updateUserProfile(user);
            CLIService.println("✅ Profile update correctly!");
        } catch (BaseException e) {
            logger.severe("❌ Error during the update: " + e.getMessage()+"\n");
        }
    }
}
