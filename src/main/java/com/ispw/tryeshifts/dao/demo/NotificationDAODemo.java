package com.ispw.tryeshifts.dao.demo;

import com.ispw.tryeshifts.dao.NotificationDAO;
import com.ispw.tryeshifts.entity.Notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationDAODemo implements NotificationDAO {
    private static final List<Notification> notifications = new ArrayList<>();


    @Override
    public List<Notification> findByUserEmail(String email)  {
        List<Notification> result = new ArrayList<>();
        if (email == null || email.isEmpty()) throw new IllegalArgumentException("email empty");
        for (Notification n : notifications) {
            if (n.getDestUser().equalsIgnoreCase(email)) {
                result.add(n);
            }
        }
        return result;
    }

    @Override
    public void markAllAsread(String email) {
        for (Notification n : notifications) {
            if (n.getDestUser().equalsIgnoreCase(email)) {
                n.setRead(true);
            }
        }
    }

    @Override
    public void saveNotification(Notification notif){
        notifications.add(new Notification(notif.getDestUser(), notif.getMessage(), notif.getType(), notif.isRead(), notif.getTimestamp()));
    }

    @Override
    public void deleteNotification(String email)  {
        if (email == null || email.isEmpty()) throw new IllegalArgumentException("Email is empty");

        List<Notification> toRemove = new ArrayList<>();
        for (Notification n : notifications) {
            if (n.getDestUser().equalsIgnoreCase(email)) {
                toRemove.add(n);
            }
        }

        notifications.removeAll(toRemove);
    }

    public int countNotificationByUserEmail(String email) {
        if(email == null || email.isEmpty()) throw new IllegalArgumentException("email is empty");
        int count = 0;
        for (Notification n : notifications) {
            if (n.getDestUser().equalsIgnoreCase(email)) {
                count++;
            }
        }
        return count;
    }
}

