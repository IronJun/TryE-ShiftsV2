package com.ispw.tryeshifts.appcontroller;

import com.ispw.tryeshifts.bean.NotificationBean;
import com.ispw.tryeshifts.config.AppConfig;
import com.ispw.tryeshifts.dao.NotificationDAO;
import com.ispw.tryeshifts.entity.Notification;
import com.ispw.tryeshifts.exception.BaseException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class NotificationAC {
    private final NotificationDAO notificationDAO ;

    public NotificationAC(NotificationDAO notificationDAO) {
        this.notificationDAO = notificationDAO;
    }

    public NotificationAC(){
        this(AppConfig.getInstance().getNotificationRepository());
    }

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

     public int getNotificationNumberforUserEmail(String email) throws BaseException{
        return notificationDAO.countNotificationByUserEmail(email);
     }


    public void markAllAsRead(String email) throws BaseException {
        notificationDAO.markAllAsread(email);
        notificationDAO.deleteNotification(email);
    }

    public CompletableFuture<Integer> getUnreadNotificationCountAsync(String email) {
        return CompletableFuture.supplyAsync(()-> {
            try{
                return notificationDAO.countNotificationByUserEmail(email);
            }catch(BaseException e){
                throw new CompletionException(e);
            }
        });
    }
}
