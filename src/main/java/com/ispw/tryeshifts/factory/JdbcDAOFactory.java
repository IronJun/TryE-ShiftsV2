package com.ispw.tryeshifts.factory;

import com.ispw.tryeshifts.dao.*;
import com.ispw.tryeshifts.dao.jdbc.*;

public class JdbcDAOFactory extends DAOFactory{
    @Override
    public UserDAO getUserDAO(){
        return new UserDAOJdbc();
    }

    @Override
    public WorkplaceDAO getWorkplaceDAO() {
        return new WorkplaceDAOJdbc();
    }

    @Override
    public AvailabilityDAO getAvailabilityDAO() {
        return new AvailabilityDAOJdbc();
    }

    @Override
    public MembershipDAO getMembershipDAO() {
        return new MembershipDAOJdbc();
    }

    @Override
    public NotificationDAO getNotificationDAO() {
        return new NotificationDAOJdbc();
    }
}
