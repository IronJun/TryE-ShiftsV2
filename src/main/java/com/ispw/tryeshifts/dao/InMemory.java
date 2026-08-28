package com.ispw.tryeshifts.dao;

import com.ispw.tryeshifts.entity.Availability;
import com.ispw.tryeshifts.entity.Membership;
import com.ispw.tryeshifts.entity.UserInfo;
import com.ispw.tryeshifts.entity.Workplace;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemory {

    // Sposta qui tutte le tue variabili demo
    private final Map<String, UserInfo> usersDbDemo = new HashMap<>();
    private final Map<String, Workplace> workplacesDbDemo = new HashMap<>();
    private final List<Membership> membershipsDbDemo = new ArrayList<>();
    private final List<Availability> availabilities = new ArrayList<>();
    private final Map<String, String> weekStatusDbDemo = new HashMap<>();
    private final Map<String, List<String>> publishedShiftsDbDemo = new HashMap<>();

    private InMemory(){
    }

    private static class LazyContainer {
        public static final InMemory instance = new InMemory();
    }

    // singleton method
    public static InMemory getInstance() {
        return LazyContainer.instance;
    }


    public Map<String, UserInfo> getUsers() { return usersDbDemo; }
    public Map<String, Workplace> getWorkplaces() { return workplacesDbDemo; }
    public List<Membership> getMemberships() { return membershipsDbDemo; }
    public List<Availability> getAvailabilities() { return availabilities; }
    public Map<String, List<String>> getPublishedShifts() { return publishedShiftsDbDemo; }
    public Map<String, String> getWeekStatusDbDemo() { return weekStatusDbDemo; }

}

