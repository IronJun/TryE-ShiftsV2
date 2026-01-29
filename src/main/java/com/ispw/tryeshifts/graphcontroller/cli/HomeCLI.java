package com.ispw.tryeshifts.graphcontroller.cli;

import com.ispw.tryeshifts.appcontroller.AccessWorkplaceAC;
import com.ispw.tryeshifts.appcontroller.ManageMembersAC;
import com.ispw.tryeshifts.appcontroller.SearchWorkplacesAC;
import com.ispw.tryeshifts.bean.SessionContext;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.excpetion.BaseException;
import com.ispw.tryeshifts.excpetion.MembershipPendingException;
import com.ispw.tryeshifts.excpetion.UserNotMemberException;
import com.ispw.tryeshifts.excpetion.ValidationException;
import com.ispw.tryeshifts.graphcontroller.cli.utilitiesCLI.CLIReader;

import java.util.List;
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
            LOGGER.info("2. Watch your Workplace List\n");
            LOGGER.info("3. See your Working days of the week\n");
            LOGGER.info("4. See your published shifts\n");
            LOGGER.info("5. Manage your Account\n");
            LOGGER.info("0. Quit the console and close the application\n");

            int choice = CLIReader.readInt("Select an option: ");
            switch(choice){
                case 1:
                    showWorkplacesList();
                    break;
                case 2:
                    showMyWorkplaces();
                    break;
                case 3:
                    showMyWorkingDays();
                    break;
                case 4:
                    showMyPublishedShifts();
                    break;
                case 5:
                    manageAccount();
                    break;
                case 0:
                    quit();
                    exit = true;
                    break;
                default:
                    LOGGER.warning("Invalid option!");
            }
        }

    }

    private static void showWorkplacesList(){
        LOGGER.info("---------------------------------------\n");
        LOGGER.info("\n--- SEARCH WORKPLACES ---\n");

        try {
            // 1. Chiamata all'App Controller (usa quello che hai già per JavaFX)
            List<WorkplaceBean> allWorkplaces = SearchWorkplacesAC.getAllWorkplaces();

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
            int choice = CLIReader.readInt("Choice: ");

            if (choice > 0 && choice <= allWorkplaces.size()) {
                // L'utente ha scelto un Workplace esistente
                WorkplaceBean selected = allWorkplaces.get(choice - 1);
                showWorkplaceDetails(selected); // Sotto-metodo per i dettagli
            } else if (choice != 0) {
                LOGGER.warning("Invalid choice!\n");
            }

        } catch (BaseException e) {
            LOGGER.severe("Error fetching workplaces: " + e.getMessage() + "\n");
        }
    }

    private static void showWorkplaceDetails(WorkplaceBean wb) {
        LOGGER.info("\n--- WORKPLACE DETAILS ---\n");
        LOGGER.info("Name: " + wb.getWorkplaceName() + "\n");
        LOGGER.info("Address: " + wb.getAddress() + "\n");

        LOGGER.info("\n1. Ask to Join this Workplace\n");
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
            LOGGER.info("Access Granted to " + accessedWp.getWorkplaceName() + "!\n");
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

    private static void showMyWorkplaces(){

    }
    private static void showMyWorkingDays(){

    }
    private static void showMyPublishedShifts(){

    }
    private static void manageAccount(){

    }
    private static void quit(){

    }

}
