package com.ispw.tryeshifts.factory;

import com.ispw.tryeshifts.dao.*;

public interface DAOFactory {
    UserDAO getUserDAO();
    WorkplaceDAO getWorkplaceDAO();
    AvailabilityDAO getAvailabilityDAO();
    MembershipDAO getMembershipDAO();
    NotificationDAO getNotificationDAO();
}
