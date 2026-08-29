package com.ispw.tryeshifts.graphcontroller.cli;

import com.ispw.tryeshifts.appcontroller.ManageMembersAC;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.bean.WorkplaceBean;
import com.ispw.tryeshifts.exception.BaseException;
import com.ispw.tryeshifts.graphcontroller.cli.utilities.CLIService;
import com.ispw.tryeshifts.session.SessionContext;

import java.util.List;
import java.util.logging.Logger;

public class WorkersCLI {
    private final Logger logger = Logger.getLogger(WorkersCLI.class.getName());
    private final ManageMembersAC ac =  new ManageMembersAC();
    private  String msg;
    public  void activeWorkers(WorkplaceBean wp){
        try {
            CLIService.println("--- ACTIVE MEMBERS OF : " + wp.getWorkplaceName() + " ---");
            List<UserBean> active = ac.getActiveMembers(wp.getWorkplaceName());
            if(active.isEmpty()){
                CLIService.println("No active members yet.");
            }else{
                msg = String.format("%n %-20s | %-20s | %-30s", "NOME", "COGNOME", "EMAIL");
                CLIService.println(msg);
                for(UserBean ub : active){
                    msg = String.format("%n %-20s | %-20s | %-30s", ub.getName(), ub.getSurname(), ub.getEmail());
                    CLIService.println(msg);
                }
            }
            CLIService.readString("Press Enter to continue...");
        }catch (BaseException e){
            logger.severe("Errore: " + e.getMessage()+"\n");
        }
    }
    public  void pendingWorkers(WorkplaceBean wp){
        try{
            UserBean loggedUser = SessionContext.getInstance().getLoggeduser();
            if(loggedUser == null||!loggedUser.getEmail().equals(wp.getOwnerEmail())){
                logger.warning("Only the Boss can handle the pending workers.\n");
                return;
            }
            List<UserBean> pending = ac.getPendingRequests(wp.getWorkplaceName());
            if (pending.isEmpty()) {
                CLIService.println("No pending requests.");
                return;
            }
            CLIService.println("--- PENDING REQUESTS OF :"+ wp.getWorkplaceName() + " ---");
            for (int i = 0; i < pending.size(); i++) {
                UserBean u = pending.get(i);
                msg = String.format("%d. %s %s (%s)", (i + 1), u.getName(), u.getSurname(), u.getEmail());
                CLIService.println(msg);
            }

            int choice = CLIService.readInt("Select the number of the user to handle (0 to annul): ");
            if (choice > 0 && choice <= pending.size()) {
                UserBean selected = pending.get(choice - 1);
                String action;
                do{
                    action = CLIService.readString("Will you accept the User? y/n: ").toLowerCase();
                }while(!action.equals("y") && !action.equals("n"));

                boolean accept = action.equals("y");
                ac.acceptWorker(selected.getEmail(), wp.getWorkplaceName(), accept);
                if(accept){
                    CLIService.println("✅ User Accepted!");
                }else{
                    CLIService.println( "❌ User not accepted.");
                }
            }
        } catch (BaseException e) {
            logger.severe("Errore: " + e.getMessage()+"\n");
        }
    }
}
