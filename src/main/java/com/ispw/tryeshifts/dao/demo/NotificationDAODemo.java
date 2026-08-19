package com.ispw.tryeshifts.dao.demo;

import com.ispw.tryeshifts.dao.NotificationDAO;
import com.ispw.tryeshifts.entity.Notification;
import com.ispw.tryeshifts.excpetion.BaseException;

import java.util.ArrayList;
import java.util.List;

public class NotificationDAODemo implements NotificationDAO {
    private static final List<Notification>  notifications = new ArrayList<>();

    // BLOCCO DI TEST: Aggiunge una notifica fittizia all'avvio per verificare il click
    static {
        try {
            // Sostituisci con l'email esatta con cui fai il login nell'app per il test
            notifications.add(new Notification("tuamail@example.com", "I turni della settimana sono usciti!", "SHIFTS", false, "Proprio Ora"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public List<Notification> findByUserEmail(String email) throws BaseException {
        List<Notification> result = new ArrayList<>();
        if(email==null || email.isEmpty()) return result;
        for(Notification n: notifications){
            if(n.getDestUser().equalsIgnoreCase(email)){
                result.add(n);
            }
        }
        return result;
    }
    @Override
    public void markAllAsread(String email) throws BaseException{
        for(Notification n: notifications){
            if(n.getDestUser().equalsIgnoreCase(email)){
                n.setRead(true);
            }
        }
    }

    @Override
    public void saveNotification(String email, String message, String type) throws BaseException {
        notifications.add(new Notification(email,message,type,false,"Proprio Ora"));
    }

    @Override
    public void deleteNotification(String email) throws BaseException {
        if(email==null || email.isEmpty()) return;

        List<Notification> toRemove=new ArrayList<>();
        for(Notification n: notifications){
            if(n.getDestUser().equalsIgnoreCase(email)){
                toRemove.add(n);
            }
        }

        notifications.removeAll(toRemove);
    }
}
