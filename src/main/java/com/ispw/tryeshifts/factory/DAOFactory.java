package com.ispw.tryeshifts.factory;

import com.ispw.tryeshifts.dao.AvailabilityDAO;
import com.ispw.tryeshifts.dao.MembershipDAO;
import com.ispw.tryeshifts.dao.UserDAO;
import com.ispw.tryeshifts.dao.WorkplaceDAO;

public interface DAOFactory {
    UserDAO getUserDAO();
    WorkplaceDAO getWorkplaceDAO();
    AvailabilityDAO getAvailabilityDAO();
    MembershipDAO getMembershipDAO();
}
