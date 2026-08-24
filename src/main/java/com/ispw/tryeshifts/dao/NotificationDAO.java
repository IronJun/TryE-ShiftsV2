package com.ispw.tryeshifts.dao;

import com.ispw.tryeshifts.entity.Notification;
import com.ispw.tryeshifts.exception.DataFetchException;
import java.util.List;

public interface NotificationDAO {
    List<Notification> findByUserEmail(String email) throws DataFetchException;
    void markAllAsread(String email) throws DataFetchException;
    void saveNotification(String email, String message, String type) throws DataFetchException;
    void deleteNotification(String email) throws DataFetchException;
    int countNotificationByUserEmail(String email) throws DataFetchException ;

    }
