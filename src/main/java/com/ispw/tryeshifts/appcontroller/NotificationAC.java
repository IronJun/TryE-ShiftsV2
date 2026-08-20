package com.ispw.tryeshifts.appcontroller;

import com.ispw.tryeshifts.bean.NotificationBean;
import com.ispw.tryeshifts.config.AppConfig;
import com.ispw.tryeshifts.dao.NotificationDAO;
import com.ispw.tryeshifts.entity.Notification;
import com.ispw.tryeshifts.excpetion.BaseException;
import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class NotificationAC {
    private final NotificationDAO notificationDAO = AppConfig.getNotificationRepository();

    public void loadUserNotificationsAsync(String email, Consumer<List<NotificationBean>> onSuccess, Consumer<Throwable> onError) throws BaseException{
        Task<List<NotificationBean>> task = new Task<>(){
            @Override
            protected List<NotificationBean> call() throws Exception{
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
            }
        };
        task.setOnSucceeded(e-> onSuccess.accept(task.getValue()));
        task.setOnFailed(e->onError.accept(task.getException()));

        new Thread(task).start();

    }

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


    public void markAllAsRead(String email) throws BaseException {
        notificationDAO.markAllAsread(email);
    }
}
