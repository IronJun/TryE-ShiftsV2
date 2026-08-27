package com.ispw.tryeshifts.appcontroller.utils;

import com.ispw.tryeshifts.config.AppConfig;
import com.ispw.tryeshifts.dao.MembershipDAO;
import com.ispw.tryeshifts.dao.NotificationDAO;
import com.ispw.tryeshifts.entity.Membership;
import com.ispw.tryeshifts.exception.BaseException;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class NotificationDispatcher {
    private final NotificationDAO notificationDAO ;
    private final MembershipDAO membershipDAO ;

    public NotificationDispatcher(NotificationDAO notificationDAO, MembershipDAO membershipDAO) {
        this.membershipDAO = membershipDAO;
        this.notificationDAO = notificationDAO;
    }

    public NotificationDispatcher(){
        this(AppConfig.getInstance().getNotificationRepository(),AppConfig.getInstance().getMembershipRepository());
    }

    public void sendActiveWorkerNotifAsync(String workplaceName, String message, String type) {
        CompletableFuture.runAsync(() -> {
            try {
                List<Membership> memberships = membershipDAO.getMembershipsByWorkplace(workplaceName);
                for (Membership m : memberships) {
                    if (m.isAccepted()) {
                        notificationDAO.saveNotification(m.getUser().getEmail(), message, type);
                    }
                }
            } catch (BaseException e) {
                throw new CompletionException("Error during the saving of the notifications.", e);
            }
        });
    }

    public void sendUserNotif(String email, String message, String type) {
        CompletableFuture.runAsync(() -> {
            try {
                notificationDAO.saveNotification(email, message, type);
            } catch (BaseException e) {
                throw new CompletionException("Error during the saving of the notifications.", e);
            }
        });
    }
}
