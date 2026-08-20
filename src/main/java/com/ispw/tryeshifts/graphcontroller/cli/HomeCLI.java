package com.ispw.tryeshifts.graphcontroller.cli;

import com.ispw.tryeshifts.appcontroller.*;
import com.ispw.tryeshifts.session.SessionContext;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.excpetion.BaseException;
import com.ispw.tryeshifts.excpetion.MembershipPendingException;
import com.ispw.tryeshifts.excpetion.UserNotMemberException;
import com.ispw.tryeshifts.excpetion.ValidationException;
import com.ispw.tryeshifts.graphcontroller.cli.utilities.CLIReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Logger;

public class HomeCLI {
    private  final Logger logger = Logger.getLogger(HomeCLI.class.getName());
    private final NewWorkplaceCLI newWorkplaceCLI = new NewWorkplaceCLI();
    private final SettingsCLI settingsCLI = new SettingsCLI();
    private final ShiftsCLI shiftsCLI = new ShiftsCLI();
    private final WorkersCLI workersCLI = new WorkersCLI();
    private final NotificationCLI notificationCLI = new NotificationCLI();
    private  String msg;

    public  void start(){
        UserBean user = SessionContext.getInstance().getLoggeduser();
        NotificationAC notification = new NotificationAC();
        if(user == null){
            logger.severe("User not logged in");
            return;
        }
        try {
            boolean exit = false;
            while (!exit) {
                int notifCount = notification.getNotificationNumberforUserEmail(user.getEmail());
                logger.info("\n--- HOME ---\n");
                logger.info("Welcome! " + user.getName() + " " + user.getSurname() + "\n");
                logger.info("---------------------------------------\n");
                logger.info("Insert one of the options: \n");
                logger.info("1. Watch the Workplace List to search and join one\n");
                logger.info("2. Create a new Workplace \n");
                logger.info("3. Watch your Workplace List\n");
                logger.info("4. See your Working days of the week\n");
                logger.info("5. Manage your Account\n");
                logger.info("6. Watch your notification(" + notifCount + ")\n");
                logger.info("Q. Logout\n"); // Nuova opzione
                logger.info("0. To close the application \n");

                String choice = CLIReader.readString("Select an option: ").toUpperCase();
                switch (choice) {
                    case "1":
                        showWorkplacesList(false);
                        break;
                    case "2":
                        newWorkplaceCLI.start();
                        break;
                    case "3":
                        showWorkplacesList(true);
                        break;
                    case "4":
                        showMyWorkingDays();
                        break;
                    case "5":
                        settingsCLI.accountSettings(user);
                        break;
                    case "6":
                        notificationCLI.start();
                        break;
                    case "0":
                        logger.info("Closing application... Goodbye!");
                        System.exit(0);
                        break;
                    case "Q":
                        logout();
                        exit = true;
                        break;
                    default:
                        logger.warning("Invalid option!");
                }
            }
        }catch (BaseException e){
            logger.severe(e.getMessage());
        }
    }

    private  void showWorkplacesList(boolean isPersonal) {
        UserBean loggedUser = SessionContext.getInstance().getLoggeduser();
        if(loggedUser == null){
            logger.severe("User not logged in");
            return;
        }
        try {
            // 1. Carichiamo la lista originale una sola volta
            List<WorkplaceBean> allWorkplaces;
            SearchWorkplacesAC searchWorkplacesAC = new SearchWorkplacesAC();
            if (isPersonal) {
                allWorkplaces = searchWorkplacesAC.getWorkplacesByEmail(loggedUser.getEmail());
            } else {
                allWorkplaces = searchWorkplacesAC.getAllWorkplaces();
            }

            if (allWorkplaces.isEmpty()) {
                logger.info("No workplaces found in the system.\n");
                return;
            }
            workplacePrint(allWorkplaces, isPersonal);

        } catch (BaseException e) {
            logger.severe("Error fetching workplaces: " + e.getMessage() + "\n");
        }
    }

    private  void workplacePrint(List<WorkplaceBean> allWorkplaces, boolean isPersonal) {
        List<WorkplaceBean> currentList = new ArrayList<>(allWorkplaces);

        while (true) {
            logger.info("\n--- ELENCO WORKPLACE ---\n");
            for (int i = 0; i < currentList.size(); i++) {
                WorkplaceBean wb = currentList.get(i);
                msg = String.format("%d. %s (%s)", (i + 1), wb.getWorkplaceName(), wb.getAddress())+"\n";
                logger.info(msg);
            }
            logger.info("0. Back to Home\n");

            String input = CLIReader.readString("Select a number or type a name to search: ");

            try {
                int choiceInt = Integer.parseInt(input);

                if (choiceInt == 0) return; // Torna effettivamente alla Home

                if (choiceInt > 0 && choiceInt <= currentList.size()) {
                    showWorkplaceDetails(currentList.get(choiceInt - 1), isPersonal);
                    return;
                } else {
                    logger.warning("Invalid number!");
                }

            } catch (NumberFormatException _) {
                String query = input.toLowerCase();
                List<WorkplaceBean> filtered = allWorkplaces.stream()
                        .filter(wp -> wp.getWorkplaceName().toLowerCase().contains(query))
                        .toList();

                if (filtered.isEmpty()) {
                    logger.warning("No workplace found matching: " + input);
                    currentList = allWorkplaces; // Reset per non restare bloccati su una lista vuota
                } else {
                    currentList = filtered; // Aggiorna la visualizzazione al prossimo ciclo
                }
            }
        }
    }
    private  void showWorkplaceDetails(WorkplaceBean wb, boolean isPersonal) {
        if(wb == null){
            logger.warning("Workplace details are null!");
            return;
        }
        logger.info("\n--- WORKPLACE DETAILS ---\n");
        logger.info("Name: " + wb.getWorkplaceName() + "\n");
        logger.info("Address: " + wb.getAddress() + "\n");

        if(isPersonal) logger.info("\n1. Access this workplace\n");
        else logger.info("\n1.Ask to join this Workplace \n");
        logger.info("0. Back to list");
        int action = CLIReader.readInt("\nSelect action: ");
        if (action == 1) {
            handleWorkplaceSelection(wb);
        }
    }
    private  void handleWorkplaceSelection(WorkplaceBean wp){
        if(wp==null){
            logger.warning("Workplace selection empty!");
            return;
        }
        UserBean loggedUser = SessionContext.getInstance().getLoggeduser();
        try{
            WorkplaceBean accessedWp = new AccessWorkplaceAC().canAccess(loggedUser,wp.getWorkplaceName());
            SessionContext.getInstance().setLoggedWorkplace(accessedWp);
            boolean exit= false;
            while(!exit) {
                logger.info("---------"+ accessedWp.getWorkplaceName() + "---------\n");
                logger.info("Choose one of the options:\n");
                logger.info("1. Give/see the shifts for this workplace\n");
                logger.info("2. See Active Workers for this workplace\n");
                if(loggedUser.getEmail().equals(accessedWp.getOwnerEmail())) {
                    logger.info("3. See pending Workers for this workplace\n");
                    logger.info("4. Manage settings for this workplace\n");

                }
                logger.info("0. Back to Home\n");
                int choice = CLIReader.readInt("Select an option: ");
                switch (choice) {
                    case 1:
                        shiftsCLI.shiftsDashboard(accessedWp);
                        break;
                    case 2:
                        workersCLI.activeWorkers(accessedWp);
                        break;
                    case 3:
                        workersCLI.pendingWorkers(accessedWp);
                        break;
                    case 4:
                        settingsCLI.workplaceSettings(accessedWp);
                        break;
                    case 0:
                        exit = true;
                        break;
                    default:
                        logger.warning("Invalid option!\n");
                }
            }
        }catch(UserNotMemberException _){
            executeJoinRequest(loggedUser, wp.getWorkplaceName());
        }catch (MembershipPendingException _){
            logger.info("Membership pending for " + wp.getWorkplaceName() + "!\n");
            CLIReader.readString("Press ENTER to continue...");
        }catch (BaseException e){
            logger.severe("Error accessing workplace: " + e.getMessage() + "\n");
        }
    }
    private  void executeJoinRequest(UserBean user, String wpName) {
        try {
            // Supponendo che requestJoin sia in WorkplaceAC o simile
            new ManageMembersAC().requestJoin(user, wpName);
            logger.info("Join request sent successfully! Wait for Boss approval.\n");
        } catch (ValidationException e) {
            logger.warning("Information: " + e.getMessage() + "\n");
        } catch (BaseException e) {
            logger.severe("Error during join request: " + e.getMessage() + "\n");
        }
    }
    private  void showMyWorkingDays() {
        ManageShiftsAC ac= new  ManageShiftsAC();
        UserBean user = SessionContext.getInstance().getLoggeduser();
        if(user == null){
            logger.severe("User is null!\n");
            return;
        }
        int offset = 0; // Settimana corrente
        String weekId = ac.calculateWeekId(offset);
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

        try {
            // Chiamata al tuo metodo dell'Applicativo
            // Supponendo sia in PublishShiftsAC
            Map<String, Object> data = ac.getHomeScheduleData(user.getEmail(), weekId);

            // Estraiamo i dati dalla mappa "Object"
            Map<String, String> assignments = (Map<String, String>) data.get("assignments");
            TreeSet<String> slots = (TreeSet<String>) data.get("slots");
            msg = "\n--- IL TUO CALENDARIO SETTIMANALE (" + ac.getWeekRangeString(offset) + ") ---";
            logger.info(msg);

            if (slots.isEmpty()) {
                logger.info("\nNessun turno assegnato per questa settimana.");
            } else {
                // Intestazione
                StringBuilder header = new StringBuilder(String.format("%-15s", "ORA"));
                for (String day : days) {
                    header.append(String.format("| %-15s", day.toUpperCase()));
                }
                msg = header.toString() + "\n" + "-".repeat(header.length()) + "\n";
                logger.info(msg);

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
                    msg = row.toString() + "\n";
                    logger.info(msg);
                }
            }

            CLIReader.readString("\nPremi INVIO per tornare alla Home...");

        } catch (BaseException e) {
            logger.severe("Errore nel recupero del calendario: " + e.getMessage());
        }
    }
    private  void logout() {
        logger.info("Logging out...\n");
        // 1. Puliamo il SessionContext
        SessionContext.getInstance().setLoggeduser(null);
        SessionContext.getInstance().setLoggedWorkplace(null);
        // 2. Eventuali altri cleanup
    }

}
