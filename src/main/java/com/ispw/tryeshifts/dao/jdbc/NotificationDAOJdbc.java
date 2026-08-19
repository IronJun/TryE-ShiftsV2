package com.ispw.tryeshifts.dao.jdbc;

import com.ispw.tryeshifts.dao.NotificationDAO;
import com.ispw.tryeshifts.entity.Notification;
import com.ispw.tryeshifts.excpetion.BaseException;

import java.util.ArrayList;
import java.util.List;

public class NotificationDAOJdbc implements NotificationDAO {
    private static final List<Notification> notifications = new ArrayList<>();

    @Override
    public List<Notification> findByUserEmail(String email) throws BaseException {
        return null;
    }
    @Override
    public void markAllAsread(String email) throws BaseException{

    }

    @Override
    public void saveNotification(String email, String message, String type) throws BaseException {
    }

    @Override
    public void deleteNotification(String email) throws BaseException {

    }
}
