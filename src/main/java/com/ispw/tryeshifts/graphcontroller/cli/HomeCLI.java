package com.ispw.tryeshifts.graphcontroller.cli;

import com.ispw.tryeshifts.appcontroller.*;
import com.ispw.tryeshifts.bean.SessionContext;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.excpetion.BaseException;
import com.ispw.tryeshifts.excpetion.MembershipPendingException;
import com.ispw.tryeshifts.excpetion.UserNotMemberException;
import com.ispw.tryeshifts.excpetion.ValidationException;
import com.ispw.tryeshifts.graphcontroller.cli.utilitiesCLI.CLIReader;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Logger;

public class HomeCLI {
    private static final Logger LOGGER = Logger.getLogger(HomeCLI.class.getName());

    private HomeCLI(){}

    public static void start(){
        UserBean user = SessionContext.getInstance().getLoggeduser();
        boolean exit = false;
        while(!exit){
            LOGGER.info("\n--- HOME ---\n");
            LOGGER.info("Welcome! "+user.getName()+" "+user.getSurname()+"\n");
            LOGGER.info("---------------------------------------\n");
            LOGGER.info("Insert one of the options: \n");
            LOGGER.info("1. Watch the Workplace List to search and join one\n");
            LOGGER.info("2. Create a new Workplace \n");
            LOGGER.info("3. Watch your Workplace List\n");
            LOGGER.info("4. See your Working days of the week\n");
            LOGGER.info("5. See your published shifts\n");
            LOGGER.info("5. Manage your Account\n");
            LOGGER.info("Q. Logout\n"); // Nuova opzione
            LOGGER.info("0. To close the application \n");

            String choice = CLIReader.readString("Select an option: ").toUpperCase();
            switch(choice){
                case "1":
                    showWorkplacesList(false);
                    break;
                case "2":
                    NewWorkplaceCLI.start();
                    break;
                case "3":
                    showWorkplacesList(true);
                    break;
                case "4":
                    showMyWorkingDays();
                    break;
                case "5":
                    SettingsCLI.accountSettings(user);
                    break;
                case "0":
                    LOGGER.info("Closing application... Goodbye!");
                    System.exit(0);
                    break;
                case "Q":
                    logout();
                    exit = true;
                    break;
                default:
                    LOGGER.warning("Invalid option!");
            }
        }

    }

    private static void showWorkplacesList(boolean isPersonal){
        LOGGER.info("---------------------------------------\n");
        LOGGER.info("\n--- SEARCH WORKPLACES ---\n");
        UserBean loggedUser = SessionContext.getInstance().getLoggeduser();
        List<WorkplaceBean> allWorkplaces = null;
        try {
            // 1. Chiamata all'App Controller (usa quello che hai già per JavaFX)
            if(isPersonal){
                allWorkplaces = SearchWorkplacesAC.getWorkplaceByUser(loggedUser.getEmail());

            }else {
                allWorkplaces = SearchWorkplacesAC.getAllWorkplaces();
            }

            if (allWorkplaces.isEmpty()) {
                LOGGER.info("No workplaces found in the system.\n");
                CLIReader.readString("Press ENTER to return to Home...");
                return;
            }

            // 2. STAMPA DELLA LISTA NUMERATA
            LOGGER.info("Select a workplace to see details or join:\n");
            for (int i = 0; i < allWorkplaces.size(); i++) {
                WorkplaceBean wb = allWorkplaces.get(i);
                // Esempio: 1. Ospedale San Raffaele (Roma)
                LOGGER.info(String.format("%d. %s (%s)\n", (i + 1), wb.getWorkplaceName(), wb.getAddress()));
            }
            LOGGER.info("0. Back to Home\n");

            // 3. GESTIONE DELLA SCELTA
            boolean validChoice = false;
            while (!validChoice) {
                int choice = CLIReader.readInt("Choice (0 to go back): ");

                if (choice == 0) {
                    return; // Torna alla Home
                }

                if (choice > 0 && choice <= allWorkplaces.size()) {
                    WorkplaceBean selected = allWorkplaces.get(choice - 1);
                    showWorkplaceDetails(selected, isPersonal);
                    validChoice = true; // Usciamo dal loop dopo l'azione
                } else {
                    LOGGER.warning("Invalid choice! Please select a number between 0 and " + allWorkplaces.size() + "\n");
                }
            }

        } catch (BaseException e) {
            LOGGER.severe("Error fetching workplaces: " + e.getMessage() + "\n");
        }
    }
    private static void showWorkplaceDetails(WorkplaceBean wb, boolean isPersonal) {
        LOGGER.info("\n--- WORKPLACE DETAILS ---\n");
        LOGGER.info("Name: " + wb.getWorkplaceName() + "\n");
        LOGGER.info("Address: " + wb.getAddress() + "\n");

        if(isPersonal)LOGGER.info("\n1. Access this workplace\n");
        else LOGGER.info("\n1.Ask to join this Workplace \n");
        LOGGER.info("0. Back to list");
        int action = CLIReader.readInt("\nSelect action: ");
        if (action == 1) {
            handleWorkplaceSelection(wb);
        }
    }
    private static void handleWorkplaceSelection(WorkplaceBean wp){
        UserBean loggedUser = SessionContext.getInstance().getLoggeduser();
        try{
            WorkplaceBean accessedWp = AccessWorkplaceAC.canAccess(loggedUser,wp.getWorkplaceName());
            SessionContext.getInstance().setLoggedWorkplace(accessedWp);
            boolean exit= false;
            while(!exit) {
                LOGGER.info("---------"+ accessedWp.getWorkplaceName() + "---------\n");
                LOGGER.info("Choose one of the options:\n");
                LOGGER.info("1. Give/see the shifts for this workplace\n");
                LOGGER.info("2. See Active Workers for this workplace\n");
                if(loggedUser.getEmail().equals(accessedWp.getOwnerEmail())) {
                    LOGGER.info("3. See pending Workers for this workplace\n");
                    LOGGER.info("4. Manage settings for this workplace\n");

                }
                LOGGER.info("0. Back to Home\n");
                int choice = CLIReader.readInt("Select an option: ");
                switch (choice) {
                    case 1:
                        ShiftsCLI.shiftsDashboard(accessedWp);
                        break;
                    case 2:
                        WorkersCLI.activeWorkers(accessedWp);
                        break;
                    case 3:
                        WorkersCLI.pendingWorkers(accessedWp);
                        break;
                    case 4:
                        SettingsCLI.workplaceSettings(accessedWp);
                        break;
                    case 0:
                        exit = true;
                        break;
                    default:
                        LOGGER.warning("Invalid option!\n");
                }
            }
        }catch(UserNotMemberException e){
            executeJoinRequest(loggedUser, wp.getWorkplaceName());
        }catch (MembershipPendingException e){
            LOGGER.info("Membership pending for " + wp.getWorkplaceName() + "!\n");
            CLIReader.readString("Press ENTER to continue...");
        }catch (BaseException e){
            LOGGER.severe("Error accessing workplace: " + e.getMessage() + "\n");
        }
    }
    private static void executeJoinRequest(UserBean user, String wpName) {
        try {
            // Supponendo che requestJoin sia in WorkplaceAC o simile
            ManageMembersAC.requestJoin(user, wpName);
            LOGGER.info("Join request sent successfully! Wait for Boss approval.\n");
        } catch (ValidationException e) {
            LOGGER.warning("Information: " + e.getMessage() + "\n");
        } catch (BaseException e) {
            LOGGER.severe("Error during join request: " + e.getMessage() + "\n");
        }
    }
    private static void showMyWorkingDays() {
        UserBean user = SessionContext.getInstance().getLoggeduser();
        int offset = 0; // Settimana corrente
        String weekId = ManageShiftsAC.calculateWeekId(offset);
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

        try {
            // Chiamata al tuo metodo dell'Applicativo
            // Supponendo sia in PublishShiftsAC
            ManageShiftsAC ac = new ManageShiftsAC();
            Map<String, Object> data = ac.getHomeScheduleData(user.getEmail(), weekId);

            // Estraiamo i dati dalla mappa "Object"
            Map<String, String> assignments = (Map<String, String>) data.get("assignments");
            TreeSet<String> slots = (TreeSet<String>) data.get("slots");

            LOGGER.info("\n--- IL TUO CALENDARIO SETTIMANALE (" + ManageShiftsAC.getWeekRangeString(offset) + ") ---");

            if (slots.isEmpty()) {
                LOGGER.info("\nNessun turno assegnato per questa settimana.");
            } else {
                // Intestazione
                StringBuilder header = new StringBuilder(String.format("%-15s", "ORA"));
                for (String day : days) {
                    header.append(String.format("| %-15s", day.toUpperCase()));
                }
                LOGGER.info(header.toString() + "\n" + "-".repeat(header.length()) + "\n");

                // Ciclo sulle fasce orarie trovate dal tuo TreeSet
                for (String slot : slots) {
                    StringBuilder row = new StringBuilder(String.format("%-15s", slot));
                    for (String day : days) {
                        // Costruiamo la chiave come fa il tuo AC: "Day_HH:mm-HH:mm"
                        String searchKey = day + "_" + slot;
                        String wpName = assignments.getOrDefault(searchKey, "-");

                        // Tronchiamo il nome se è troppo lungo per la colonna
                        if (wpName.length() > 15) wpName = wpName.substring(0, 12) + "...";

                        row.append(String.format("| %-15s", wpName));
                    }
                    LOGGER.info(row.toString() + "\n");
                }
            }

            CLIReader.readString("\nPremi INVIO per tornare alla Home...");

        } catch (BaseException e) {
            LOGGER.severe("Errore nel recupero del calendario: " + e.getMessage());
        }
    }
    private static void logout() {
        LOGGER.info("Logging out...\n");
        // 1. Puliamo il SessionContext
        SessionContext.getInstance().setLoggeduser(null);
        SessionContext.getInstance().setLoggedWorkplace(null);
        // 2. Eventuali altri cleanup
    }

}
