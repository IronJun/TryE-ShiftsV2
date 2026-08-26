package com.ispw.tryeshifts.appcontroller;

import com.ispw.tryeshifts.bean.NotificationBean;
import com.ispw.tryeshifts.config.AppConfig;
import com.ispw.tryeshifts.dao.MembershipDAO;
import com.ispw.tryeshifts.dao.NotificationDAO;
import com.ispw.tryeshifts.entity.Membership;
import com.ispw.tryeshifts.entity.Notification;
import com.ispw.tryeshifts.exception.BaseException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class NotificationAC {
    private final NotificationDAO notificationDAO = AppConfig.getInstance().getNotificationRepository();
    private final MembershipDAO membershipDAO = AppConfig.getInstance().getMembershipRepository();


    public CompletableFuture<List<NotificationBean>> getUserNotificationsAsync(String email) {
        return CompletableFuture.supplyAsync(()->{
            try{
                List<NotificationBean> notificationBeans = new ArrayList<>();
                List<Notification> notfications = notificationDAO.findByUserEmail(email);
                if(notfications != null) {
                    for (Notification n : notfications) {
                        notificationBeans.add(new NotificationBean(
                                n.getMessage(),
                                n.getType(),
                                n.isRead(),
                                n.getTimestamp()
                        ));
                    }
                }
                return notificationBeans;
            }catch(BaseException e){
                throw new CompletionException(e);
            }
        });
    }

    //method that would be used if i wanted a user to send a notification manually
    public CompletableFuture<Void> sendUserNotif(String email, String message, String type) {
        return CompletableFuture.runAsync(() -> {
            try{
                notificationDAO.saveNotification(email,message,type);
            }catch(BaseException e){
                throw new CompletionException("Error during the saving of the notifications.", e);
            }
        });
     }
     public int getNotificationNumberforUserEmail(String email) throws BaseException{
        return notificationDAO.countNotificationByUserEmail(email);
     }

    public CompletableFuture<Void> sendActiveWorkerNotifAsync(String workplaceName, String message, String type) {
        return CompletableFuture.runAsync(() -> {
            try{
                List<Membership> memberships = membershipDAO.getMembershipsByWorkplace(workplaceName);
                for(Membership m : memberships) {
                    if(m.isAccepted()){
                        notificationDAO.saveNotification(m.getUser().getEmail(),message,type);
                    }
                }
            }catch(BaseException e){
                throw new CompletionException("Error during the saving of the notifications.", e);
            }
        });
    }

    public void markAllAsRead(String email) throws BaseException {
        notificationDAO.markAllAsread(email);
        notificationDAO.deleteNotification(email);
    }
}
