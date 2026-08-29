package com.ispw.tryeshifts.graphcontroller.cli;

import com.ispw.tryeshifts.appcontroller.*;
import com.ispw.tryeshifts.session.SessionContext;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.exception.BaseException;
import com.ispw.tryeshifts.exception.MembershipPendingException;
import com.ispw.tryeshifts.exception.UserNotMemberException;
import com.ispw.tryeshifts.exception.ValidationException;
import com.ispw.tryeshifts.graphcontroller.cli.utilities.CLIReader;
import com.ispw.tryeshifts.utils.PreferencesManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Logger;

public class  HomeCLI {
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
            logger.severe("User not logged in\n");
            return;
        }
        try {
            boolean exit = false;
            while (!exit) {
                int notifCount = notification.getNotificationNumberforUserEmail(user.getEmail());
                CLIReader.println("--- HOME ---");
                CLIReader.println("Welcome! " + user.getName() + " " + user.getSurname() + "");
                CLIReader.println("---------------------------------------");
                CLIReader.println("Insert one of the options: ");
                CLIReader.println("1. Watch the Workplace List to search and join one");
                CLIReader.println("2. Create a new Workplace ");
                CLIReader.println("3. Watch your Workplace List");
                CLIReader.println("4. See your Working days of the week");
                CLIReader.println("5. Manage your Account");
                CLIReader.println("6. Watch your notification(" + notifCount + ")");
                CLIReader.println("Q. Logout"); // Nuova opzione
                CLIReader.println("0. To close the application ");

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
                        CLIReader.println("Closing application... Goodbye!");
                        System.exit(0);
                        break;
                    case "Q":
                        logout();
                        exit = true;
                        break;
                    default:
                        logger.warning("Invalid option!\n");
                }
            }
        }catch (BaseException e){
            logger.severe(e.getMessage()+"\n");
        }
    }

    private  void showWorkplacesList(boolean isPersonal) {
        UserBean loggedUser = SessionContext.getInstance().getLoggeduser();
        if(loggedUser == null){
            logger.severe("User not logged in\n");
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
                CLIReader.println("No workplaces found in the system.");
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
            CLIReader.println("--- WORKPLACE LIST ---");
            for (int i = 0; i < currentList.size(); i++) {
                WorkplaceBean wb = currentList.get(i);
                msg = String.format("%d. %s (%s)", (i + 1), wb.getWorkplaceName(), wb.getAddress())+"";
                CLIReader.println(msg);
            }
            CLIReader.println("0. Back to Home");

            String input = CLIReader.readString("Select a number or type a name to search: ");

            try {
                int choiceInt = Integer.parseInt(input);

                if (choiceInt == 0) return; // Torna effettivamente alla Home

                if (choiceInt > 0 && choiceInt <= currentList.size()) {
                    showWorkplaceDetails(currentList.get(choiceInt - 1), isPersonal);
                    return;
                } else {
                    logger.warning("Invalid number!\n");
                }

            } catch (NumberFormatException _) {
                String query = input.toLowerCase();
                List<WorkplaceBean> filtered = allWorkplaces.stream()
                        .filter(wp -> wp.getWorkplaceName().toLowerCase().contains(query))
                        .toList();

                if (filtered.isEmpty()) {
                    logger.warning("No workplace found matching: " + input +"\n");
                    currentList = allWorkplaces; // Reset per non restare bloccati su una lista vuota
                } else {
                    currentList = filtered; // Aggiorna la visualizzazione al prossimo ciclo
                }
            }
        }
    }
    private  void showWorkplaceDetails(WorkplaceBean wb, boolean isPersonal) {
        if(wb == null){
            logger.warning("Workplace details are null!\n");
            return;
        }
        CLIReader.println("--- WORKPLACE DETAILS ---");
        CLIReader.println("Name: " + wb.getWorkplaceName() + "");
        CLIReader.println("Address: " + wb.getAddress() + "");
        UserBean user = SessionContext.getInstance().getLoggeduser();
        try{
            AccessWorkplaceAC ac = new AccessWorkplaceAC();
            ac.canAccess(user,wb.getWorkplaceName());
            CLIReader.println("1.Access this workplace");
        }catch(UserNotMemberException _){
            CLIReader.println("1. Ask to join this workplace");
        }catch(MembershipPendingException _){
            CLIReader.println("1. Your request has not been accepted yet, see the status");
        }catch (BaseException e){
            logger.warning("Impossible to verify the access to this workplace: " + e.getMessage()+"\n" );
            CLIReader.println("1. Select this workplace");
        }
        CLIReader.println("0. Back to list");
        int action = CLIReader.readInt("Select action: ");
        if (action == 1) {
            handleWorkplaceSelection(wb);
        }
    }
    private  void handleWorkplaceSelection(WorkplaceBean wp){
        if(wp==null){
            logger.warning("Workplace selection empty!\n");
            return;
        }
        UserBean loggedUser = SessionContext.getInstance().getLoggeduser();
        try{
            WorkplaceBean accessedWp = new AccessWorkplaceAC().canAccess(loggedUser,wp.getWorkplaceName());
            SessionContext.getInstance().setLoggedWorkplace(accessedWp);
            boolean exit= false;
            while(!exit) {
                CLIReader.println("---------"+ accessedWp.getWorkplaceName() + "---------");
                CLIReader.println("Choose one of the options:");
                CLIReader.println("1. Give/see the shifts for this workplace");
                CLIReader.println("2. See Active Workers for this workplace");
                if(loggedUser.getEmail().equals(accessedWp.getOwnerEmail())) {
                    CLIReader.println("3. See pending Workers for this workplace");
                    CLIReader.println("4. Manage settings for this workplace");

                }
                CLIReader.println("0. Back to Home");
                int choice = CLIReader.readInt("Select an option: ");
                switch (choice) {
                    case 1:
                        shiftsCLI.shiftsDashboard(accessedWp);
                        break;
                    case 2:
                        workersCLI.activeWorkers(accessedWp);
                        break;
                    case 3:
                        if(loggedUser.getEmail().equals(accessedWp.getOwnerEmail())) {
                            workersCLI.pendingWorkers(accessedWp);
                        }else{
                            logger.warning("Only the owner can see the pendant request!\n");
                        }
                        break;
                    case 4:
                        if(loggedUser.getEmail().equals(accessedWp.getOwnerEmail())) {
                            settingsCLI.workplaceSettings(accessedWp);
                        }else{
                            logger.warning("Only can change the workplace Settings!\n");
                        }
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
            CLIReader.println("Membership pending for " + wp.getWorkplaceName() + "!");
            CLIReader.readString("Press ENTER to continue...");
        }catch (BaseException e){
            logger.severe("Error accessing workplace: " + e.getMessage() + "\n");
        }
    }
    private  void executeJoinRequest(UserBean user, String wpName) {
        try {
            // Supponendo che requestJoin sia in WorkplaceAC o simile
            new ManageMembersAC().requestJoin(user, wpName);
            CLIReader.println("Join request sent successfully! Wait for Boss approval.");
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
            msg = "--- Your Week Shifts (" + ac.getWeekRangeString(offset) + ") ---";
            CLIReader.println(msg);

            if (slots.isEmpty()) {
                CLIReader.println("No Shifts assigned for this week.");
            } else {
                // Intestazione
                StringBuilder header = new StringBuilder(String.format("%-15s", "HOUR"));
                for (String day : days) {
                    header.append(String.format("| %-15s", day.toUpperCase()));
                }
                msg = header.toString() + "" + "-".repeat(header.length()) + "";
                CLIReader.println(msg);

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
                    msg = row.toString() + "";
                    CLIReader.println(msg);
                }
            }

            CLIReader.readString("Press Enter to go back to the home...");

        } catch (BaseException e) {
            logger.severe("Error fetching the calendar: " + e.getMessage()+"\n");
        }
    }
    private  void logout() {
        CLIReader.println("Logging out...");
        // 1. Puliamo il SessionContext
        PreferencesManager.clearPreferences();
        SessionContext.getInstance().setLoggeduser(null);
        SessionContext.getInstance().setLoggedWorkplace(null);
        // 2. Eventuali altri cleanup
    }

}
