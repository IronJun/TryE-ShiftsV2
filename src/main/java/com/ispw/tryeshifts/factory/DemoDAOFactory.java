package com.ispw.tryeshifts.factory;

import com.ispw.tryeshifts.dao.*;
import com.ispw.tryeshifts.dao.demo.*;
import com.ispw.tryeshifts.dao.jdbc.*;

public class DemoDAOFactory implements DAOFactory{
    @Override
    public UserDAO getUserDAO() {
        return new UserDAODemo();
    }

    @Override
    public WorkplaceDAO getWorkplaceDAO() {
        return new WorkplaceDAODemo();
    }

    @Override
    public AvailabilityDAO getAvailabilityDAO() {
        return new AvailabilityDAODemo();
    }

    @Override
    public MembershipDAO getMembershipDAO() {
        return new MembershipDAODemo();
    }

    @Override
    public NotificationDAO getNotificationDAO() {return new NotificationDAODemo();}
}
