package com.ispw.tryeshifts.dao;

import com.ispw.tryeshifts.entity.Notification;
import com.ispw.tryeshifts.excpetion.BaseException;
import com.ispw.tryeshifts.excpetion.DataFetchException;
import com.ispw.tryeshifts.excpetion.EntityNotFoundException;

import javax.xml.crypto.Data;
import java.util.List;
import java.util.zip.DataFormatException;

public interface NotificationDAO {
    List<Notification> findByUserEmail(String email) throws DataFetchException;
    void markAllAsread(String email) throws DataFetchException;
    void saveNotification(String email, String message, String type) throws DataFetchException;
    void deleteNotification(String email) throws DataFetchException;
    int countNotificationByUserEmail(String email) throws DataFetchException ;

    }
