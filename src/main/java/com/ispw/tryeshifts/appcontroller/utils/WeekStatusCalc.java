package com.ispw.tryeshifts.appcontroller.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class WeekStatusCalc {

    public String getAutomaticWeekStatus(String weekId){
        String[] parts = weekId.split("_");
        int year = Integer.parseInt(parts[0]);
        int week = Integer.parseInt(parts[1]);

        LocalDate targetMonday = LocalDate.ofYearDay(year, 1)
                .with(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear(), week)
                .with(DayOfWeek.MONDAY);

        LocalDateTime weDeadLine = targetMonday.minusDays(5).atTime(23,59,59);
        LocalDateTime friDeadline = targetMonday.minusDays(3).atTime(23,59,59);
        LocalDateTime sunDeadline = targetMonday.minusDays(1).atTime(23,59,59);
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());

        if(now.isAfter(friDeadline)&&now.isBefore(sunDeadline)){
            return "PUBLISHED";
        }
        if(now.isAfter(weDeadLine) && now.isBefore(friDeadline)){
            return "LOCKED";
        }
        return "OPEN";
    }
}
