package com.ispw.tryeshifts.graphcontroller.cli;

import com.ispw.tryeshifts.appcontroller.NotificationAC;
import com.ispw.tryeshifts.bean.NotificationBean;
import com.ispw.tryeshifts.bean.UserBean;
import com.ispw.tryeshifts.exception.BaseException;
import com.ispw.tryeshifts.graphcontroller.cli.utilities.CLIReader;
import com.ispw.tryeshifts.session.SessionContext;

import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.logging.Logger;

public class NotificationCLI {
    private static final Logger logger = Logger.getLogger(NotificationCLI.class.getName());
    private final NotificationAC notificationAC= new NotificationAC();

    public void start() {
        boolean back = false;
        UserBean user = SessionContext.getInstance().getLoggeduser();
        if (user == null) {
            CLIReader.println("User not logged in");
            return;
        }
        String userEmail = user.getEmail();
        if(userEmail == null){
            CLIReader.println("User email is null");
            return;
        }
        while (!back) {
            printHeader();
            CLIReader.println("loading notifications...");
            try {
                // 1. Fetching notifications via the Application Controller
                List<NotificationBean> notifications = notificationAC.getUserNotificationsAsync(userEmail).join();
                if (notifications.isEmpty()) {
                    CLIReader.println("No notifications found, going back to the home ->");
                    break;
                } else {
                    renderNotifications(notifications);
                    printMenu();

                    String choice = CLIReader.readString("Select an option: ");
                    back = handleChoice(choice);
                }
            }catch (CompletionException e){
                String detail = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                logger.warning("Error fetching notifications: " + detail+"\n");
                CLIReader.println("[ERROR] Unable to load notifications. Please try again later.");
                back = true;
            } catch (BaseException e) {
                logger.warning("Error fetching notifications: " + e.getMessage()+"\n");
                CLIReader.println("[ERROR] Unable to load notifications. Please try again later.");
                back = true;
            }
        }
    }

    private void printHeader() {
        CLIReader.println("========================================");
        CLIReader.println("           NOTIFICATIONS CENTER");
        CLIReader.println("========================================");
    }

    private void renderNotifications(List<NotificationBean> notifications) {
        if (notifications == null || notifications.isEmpty()) {
            CLIReader.println("No notifications found.");
            return;
        }
        String msg;

        for (int i = 0; i < notifications.size(); i++) {
            NotificationBean n = notifications.get(i);
            msg = ""+ (i + 1)+" "+ n.getTimestamp()+" "+ n.getMessage();
            CLIReader.println(msg);
        }
    }

    private void printMenu() {
        CLIReader.println("----------------------------------------");
        CLIReader.println("Available Actions:");
        CLIReader.println("[m] Mark all as read and cancel all");
        CLIReader.println("[b] Back to main menu");
        CLIReader.println("----------------------------------------");
    }

    private boolean handleChoice(String choice) throws BaseException {
        switch (choice) {
            case "m":
                try {
                    UserBean user = SessionContext.getInstance().getLoggeduser();
                    if(user == null){
                        CLIReader.println("User not logged in");
                        return false;
                    }
                    notificationAC.markAllAsRead(user.getEmail());
                    CLIReader.println("[SUCCESS] All notifications marked as read.");
                }catch(BaseException e){
                    logger.severe("[ERROR] Unable to set all notifications marked as read."+e.getMessage()+"\n");
                }
                return false;
            case "b":
                return true; // Go back
            default:
                CLIReader.println("ATTENTION unrecognized choice.");
                return false;
        }
    }
}