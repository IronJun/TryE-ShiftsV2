package com.ispw.tryeshifts.appcontroller.utils;

import com.ispw.tryeshifts.bean.NotificationBean;
import com.ispw.tryeshifts.config.AppConfig;
import com.ispw.tryeshifts.dao.MembershipDAO;
import com.ispw.tryeshifts.dao.NotificationDAO;
import com.ispw.tryeshifts.entity.Membership;
import com.ispw.tryeshifts.entity.Notification;
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

    public void sendActiveWorkerNotifAsync(String workplaceName, NotificationBean notifBean) {
        CompletableFuture.runAsync(() -> {
            try {
                List<Membership> memberships = membershipDAO.getMembershipsByWorkplace(workplaceName);
                for (Membership m : memberships) {
                    if (m.isAccepted()) {
                        Notification notif = new Notification(m.getUser().getEmail(), notifBean.getMessage(), notifBean.getType());

                        notificationDAO.saveNotification(notif);
                    }
                }
            } catch (BaseException e) {
                throw new CompletionException("Error during the saving of the notifications.", e);
            }
        });
    }

    public void sendUserNotif(NotificationBean notificationBean) {
        CompletableFuture.runAsync(() -> {
            try {
                Notification notif = new Notification(notificationBean.getDestUser(),
                        notificationBean.getMessage(),
                        notificationBean.getType());
                notificationDAO.saveNotification(notif);
            } catch (BaseException e) {
                throw new CompletionException("Error during the saving of the notifications.", e);
            }
        });
    }
}
