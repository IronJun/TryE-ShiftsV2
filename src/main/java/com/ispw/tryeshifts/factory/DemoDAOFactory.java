package com.ispw.tryeshifts.factory;

import com.ispw.tryeshifts.dao.AvailabilityDAO;
import com.ispw.tryeshifts.dao.MembershipDAO;
import com.ispw.tryeshifts.dao.UserDAO;
import com.ispw.tryeshifts.dao.WorkplaceDAO;
import com.ispw.tryeshifts.dao.demo.AvailabilityDAODemo;
import com.ispw.tryeshifts.dao.jdbc.AvailabilityDAOJdbc;
import com.ispw.tryeshifts.dao.jdbc.MembershipDAOJdbc;
import com.ispw.tryeshifts.dao.jdbc.UserDAOJdbc;
import com.ispw.tryeshifts.dao.jdbc.WorkplaceDAOJdbc;

public class DemoDAOFactory implements DAOFactory{
    @Override
    public UserDAO getUserDAO() {
        return new UserDAOJdbc();
    }

    @Override
    public WorkplaceDAO getWorkplaceDAO() {
        return new WorkplaceDAOJdbc();
    }

    @Override
    public AvailabilityDAO getAvailabilityDAO() {
        return new AvailabilityDAODemo();
    }

    @Override
    public MembershipDAO getMembershipDAO() {
        return new MembershipDAOJdbc();
    }
}
