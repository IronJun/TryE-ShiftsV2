package com.ispw.tryeshifts.dao;

import com.ispw.tryeshifts.entity.Notification;
import com.ispw.tryeshifts.excpetion.BaseException;

import java.util.List;

public interface NotificationDAO {
    List<Notification> findByUserEmail(String email) throws BaseException;
    void markAllAsread(String email) throws BaseException;
    void saveNotification(String email, String message, String type) throws BaseException;
    void deleteNotification(String email) throws BaseException;
    int countNotificationByUserEmail(String email) throws BaseException ;

    }
