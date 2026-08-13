package com.ispw.tryeshifts.factory;

import com.ispw.tryeshifts.dao.AvailabilityDAO;
import com.ispw.tryeshifts.dao.MembershipDAO;
import com.ispw.tryeshifts.dao.UserDAO;
import com.ispw.tryeshifts.dao.WorkplaceDAO;
import com.ispw.tryeshifts.dao.csv.UserDAOCsv;
import com.ispw.tryeshifts.dao.jdbc.UserDAOJdbc;

public class CsvDAOFactory implements DAOFactory{

    @Override
    public UserDAO getUserDAO() {
        return new UserDAOCsv();
    }

    @Override
    public WorkplaceDAO getWorkplaceDAO() {
        return null;
    }

    @Override
    public AvailabilityDAO getAvailabilityDAO() {
        return null;
    }

    @Override
    public MembershipDAO getMembershipDAO() {
        return null;
    }
}
