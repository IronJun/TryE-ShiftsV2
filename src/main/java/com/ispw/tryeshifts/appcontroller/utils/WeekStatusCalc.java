package com.ispw.tryeshifts.appcontroller.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;

public class WeekStatusCalc {

    public String getAutomaticWeekStatus(String weekId){
        String[] parts = weekId.split("_");
        int year = Integer.parseInt(parts[0]);
        int week = Integer.parseInt(parts[1]);

        LocalDate targetMonday = LocalDate.ofYearDay(year, 1)
                .with(WeekFields.ISO.weekOfWeekBasedYear(),week)
                .with(DayOfWeek.MONDAY);

        LocalDateTime weDeadLine = targetMonday.minusDays(5).atTime(23,59,59);
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());

        if(!now.isAfter(weDeadLine)){  // DA QUI partirebbe anche una logica di publicazione automatica futura
            return "OPEN";
        }
        return "LOCKED";
    }

    public LocalDateTime getNextDeadLine(String weekId, String currentStatus){

        String[] parts = weekId.split("_");
        int year = Integer.parseInt(parts[0]);
        int week = Integer.parseInt(parts[1]);


        LocalDate targetMonday = LocalDate.now(ZoneId.systemDefault())
                .with(IsoFields.WEEK_BASED_YEAR, year)
                .with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, week)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        if("OPEN".equals(currentStatus)){
            return targetMonday.minusDays(5).atTime(23,59,59);
        }else if("LOCKED".equals(currentStatus)){
            return targetMonday.minusDays(3).atTime(23,59,59);
        }
        return null;
    }
}
