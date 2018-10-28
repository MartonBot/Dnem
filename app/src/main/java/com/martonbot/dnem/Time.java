package com.martonbot.dnem;

import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.LocalDate;

/**
 * Created by mgrihangne on 23/06/2017.
 */

public class Time {

    private static final int HALF_DAY_MILLIS = 12 * 60 * 60 * 1000;

    public static int daysMissed(long currentStamp, DateTimeZone currentZone, long previousStamp, DateTimeZone previousZone) {
        int daysMissed;

        LocalDate currentDate = new LocalDate(currentStamp, currentZone);
        LocalDate previousDate = new LocalDate(previousStamp, previousZone);
        if (currentZone.equals(previousZone)) {
            daysMissed = Math.max(0, Days.daysBetween(previousDate, currentDate).getDays() - 1);
        } else {
            // allow for half a day more
            LocalDate previousDatePlusTwelveHours = new LocalDate(previousStamp + HALF_DAY_MILLIS, previousZone);
            daysMissed = Math.max(0, Days.daysBetween(previousDatePlusTwelveHours, currentDate).getDays() - 1);
        }
        return daysMissed;
    }

    public static LocalDate today() {
        return new LocalDate(DateTimeZone.getDefault());
    }
}
