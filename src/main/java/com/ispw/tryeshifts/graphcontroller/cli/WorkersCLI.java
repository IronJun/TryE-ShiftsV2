package com.ispw.tryeshifts.graphcontroller.cli;

import com.ispw.tryeshifts.appcontroller.ManageMembersAC;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.excpetion.BaseException;
import com.ispw.tryeshifts.graphcontroller.cli.utilities.CLIReader;

import java.util.List;
import java.util.logging.Logger;

public class WorkersCLI {
    private static final Logger LOGGER = Logger.getLogger(WorkersCLI.class.getName());
    private static String msg;
    private WorkersCLI(){
        throw new IllegalStateException("Utility class");
    }
    public static void activeWorkers(WorkplaceBean wp){
        try {
            LOGGER.info("\n--- ACTIVE MEMBERS OF : " + wp.getWorkplaceName() + " ---");
            ManageMembersAC ac = new ManageMembersAC();
            List<UserBean> active = ac.getActiveMembers(wp.getWorkplaceName());
            if(active.isEmpty()){
                LOGGER.info("No active members yet.");
            }else{
                msg = String.format("%n %-20s | %-20s | %-30s", "NOME", "COGNOME", "EMAIL");
                LOGGER.info(msg);
                for(UserBean ub : active){
                    msg = String.format("%n %-20s | %-20s | %-30s", ub.getName(), ub.getSurname(), ub.getEmail());
                    LOGGER.info(msg);
                }
            }
            CLIReader.readString("\nPress Enter to continue...");
        }catch (BaseException e){
            LOGGER.severe("Errore: " + e.getMessage());
        }
    }
    public static void pendingWorkers(WorkplaceBean wp){
        try{
            ManageMembersAC ac = new ManageMembersAC();
            List<UserBean> pending = ac.getPendingRequests(wp.getWorkplaceName());
            if (pending.isEmpty()) {
                LOGGER.info("\nNo pending requests.");
                return;
            }
            LOGGER.info("\n--- PENDING REQUESTS OF :"+ wp.getWorkplaceName() + " ---\n");
            for (int i = 0; i < pending.size(); i++) {
                UserBean u = pending.get(i);
                msg = String.format("%d. %s %s (%s)", (i + 1), u.getName(), u.getSurname(), u.getEmail());
                LOGGER.info(msg);
            }

            int choice = CLIReader.readInt("\nSelect the number of the user to handle (0 to annul): ");
            if (choice > 0 && choice <= pending.size()) {
                UserBean selected = pending.get(choice - 1);
                String action = CLIReader.readString("\nWill you accept the User? y/n: ").toLowerCase();

                boolean accept = action.equals("y");
                ac.acceptWorker(selected.getEmail(), wp.getWorkplaceName(), accept);
                LOGGER.info(accept ? "\n✅ User Accepted!" : "❌ User not accepted.");
            }
        } catch (BaseException e) {
            LOGGER.severe("Errore: " + e.getMessage());
        }
    }
}
