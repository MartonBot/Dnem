package com.martonbot.dnem;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

/**
 * Created by mgrihangne on 23/06/2017.
 */

public class Time {

    private final static long THIRTY_SIX_HOURS_IN_MILLIS = 36 * 60 * 60 * 1000;

    private static boolean thirtySixHoursApart(long todayStamp, long yesterdayStamp) {
        if (todayStamp <= yesterdayStamp) {
            throw new IllegalStateException("Today has to be after yesterday!");
        }
        return (todayStamp - yesterdayStamp) <= THIRTY_SIX_HOURS_IN_MILLIS;
    }

    public static boolean keepStreak(long todayStamp, DateTimeZone todayZone, long yesterdayStamp, DateTimeZone yesterdayZone) {
        boolean keepStreak = false;
        if (todayZone.equals(yesterdayZone)) {
            LocalDate today = new LocalDate(todayStamp, todayZone);
            LocalDate yesterday = new LocalDate(yesterdayStamp, yesterdayZone);

            keepStreak = today.minusDays(1).equals(yesterday) || today.equals(yesterday);
        }
        else {
            // simply compare timestamps
            keepStreak = thirtySixHoursApart(todayStamp, yesterdayStamp);
        }

        return keepStreak;
    }

    public static LocalDate today() {
        return new LocalDate();
    }
}
