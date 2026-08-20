package com.ispw.tryeshifts.appcontroller;

import com.ispw.tryeshifts.bean.NotificationBean;
import com.ispw.tryeshifts.config.AppConfig;
import com.ispw.tryeshifts.dao.NotificationDAO;
import com.ispw.tryeshifts.entity.Notification;
import com.ispw.tryeshifts.excpetion.BaseException;
import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NotificationAC {
    private final NotificationDAO notificationDAO = AppConfig.getNotificationRepository();



    public CompletableFuture<List<NotificationBean>> getUserNotificationsAsync(String email) throws BaseException{
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
                throw new RuntimeException("Errore nel recuper delle notifiche",e);
            }
        });
    }

    //method that would be used if i wanted a user to send a notification manually
    public void sendNotificationsAsync(String email, String message, String type) throws BaseException{
        Task<Void> task = new Task<>(){
            @Override
            protected Void call() throws Exception{
                notificationDAO.saveNotification(email, message, type);
                return null;
            }
        };
        new Thread(task).start();
     }
     public int getNotificationNumberforUserEmail(String email) throws BaseException{
        return notificationDAO.countNotificationByUserEmail(email);
     }

    public void markAllAsRead(String email) throws BaseException {
        notificationDAO.markAllAsread(email);
        notificationDAO.deleteNotification(email);
    }
}
