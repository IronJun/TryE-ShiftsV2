package com.ispw.tryeshifts.graphcontroller.cli;

import com.ispw.tryeshifts.appcontroller.NotificationAC;
import com.ispw.tryeshifts.bean.NotificationBean;
import com.ispw.tryeshifts.excpetion.BaseException;
import com.ispw.tryeshifts.graphcontroller.cli.utilities.CLIReader;
import com.ispw.tryeshifts.session.SessionContext;

import java.util.List;
import java.util.logging.Logger;

public class NotificationCLI {
    private static final Logger logger = Logger.getLogger(NotificationCLI.class.getName());
    private final NotificationAC notificationAC= new NotificationAC();

    public void start() {
        boolean back = false;
        String userEmail = SessionContext.getInstance().getLoggeduser().getEmail();
        if(userEmail == null|| SessionContext.getInstance().getLoggeduser() == null){
            logger.info("User email is null");
            return;
        }
        while (!back) {
            printHeader();
            logger.info("\nloading notifications...\n");
            try {
                // 1. Fetching notifications via the Application Controller
                List<NotificationBean> notifications = notificationAC.getUserNotificationsAsync(userEmail).join();
                if(notifications.isEmpty()){
                    logger.info("No notifications found, going back to the home ->\n");
                    break;
                }else {
                    renderNotifications(notifications);
                    printMenu();

                    String choice = CLIReader.readString("\nSelect an option: ");
                    back = handleChoice(choice);
                }
            } catch (BaseException e) {
                logger.warning("Error fetching notifications: " + e.getMessage());
                logger.info("\n[ERROR] Unable to load notifications. Please try again later.");
                back = true;
            }
        }
    }

    private void printHeader() {
        logger.info("\n========================================");
        logger.info("\n           NOTIFICATIONS CENTER");
        logger.info("\n========================================");
    }

    private void renderNotifications(List<NotificationBean> notifications) {
        if (notifications == null || notifications.isEmpty()) {
            logger.info("No notifications found.");
            return;
        }
        String msg;

        for (int i = 0; i < notifications.size(); i++) {
            NotificationBean n = notifications.get(i);
            msg = ""+ (i + 1)+" "+ n.getTimestamp()+" "+ n.getMessage();
            logger.info(msg);
        }
    }

    private void printMenu() {
        logger.info("\n----------------------------------------\n");
        logger.info("Available Actions:\n");
        logger.info("[m] Mark all as read and cancel all\n");
        logger.info("[b] Back to main menu\n");
        logger.info("----------------------------------------\n");
    }

    private boolean handleChoice(String choice) throws BaseException {
        switch (choice) {
            case "m":
                try {
                    notificationAC.markAllAsRead(SessionContext.getInstance().getLoggeduser().getEmail());
                    if(SessionContext.getInstance().getLoggeduser()==null){
                        logger.warning("User email is null");
                        return false;
                    }
                    logger.info("\n[SUCCESS] All notifications marked as read.");
                }catch(BaseException e){
                    logger.severe("[ERROR] Unable to set all notifications marked as read."+e.getMessage());
                }
                return false;
            case "b":
                return true; // Go back
            default:
                logger.info("ATTENTION unrecognized choice.");
                return false;
        }
    }
}