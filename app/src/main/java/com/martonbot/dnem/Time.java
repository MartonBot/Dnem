package com.martonbot.dnem;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

/**
 * Created by mgrihangne on 23/06/2017.
 */

public class Time {

    private static final int TWO_DAYS = 2;
    private static final int ONE_DAY = 1;
    private static final int SEVENTY_TWO_HOURS = 72;
    private static final int THIRTY_SIX_HOURS = 36;


    private static boolean hoursApart(long currentStamp, long previousStamp, int hoursAllowed) {
        // todo check this as I'm not sure it's doing the right thing?
        if (currentStamp <= previousStamp) {
            throw new IllegalStateException("Current timestamp has to be chronologically after previous!");
        }
        return (currentStamp - previousStamp) <= hoursAllowed * 60 * 60 * 1000;
    }

    // on two consecutive dates if same time zone, otherwise timestamps less than 36 hours apart
    public static boolean isStreakConserved(long currentStamp, DateTimeZone currentZone, long previousStamp, DateTimeZone previousZone) {
        boolean keepStreak = false;

        // if same time zone
        if (currentZone.equals(previousZone)) {
            LocalDate current = new LocalDate(currentStamp, currentZone);
            LocalDate previous = new LocalDate(previousStamp, previousZone);

            keepStreak = current.minusDays(ONE_DAY).equals(previous) || current.equals(previous);
        }
        else {
            // simply compare timestamps
            keepStreak = hoursApart(currentStamp, previousStamp, THIRTY_SIX_HOURS);
        }

        return keepStreak;
    }

    // allows for one day between the two timestamps (or 72h if different time zones)
    public static boolean isStreakConservedUsingStar(long currentStamp, DateTimeZone currentZone, long previousStamp, DateTimeZone previousZone) {
        boolean keepStreak = false;

        if (currentZone.equals(previousZone)) {
            LocalDate current = new LocalDate(currentStamp, currentZone);
            LocalDate previous = new LocalDate(previousStamp, previousZone);

            keepStreak = current.minusDays(TWO_DAYS).equals(previous);
        }
        else {
            // simply compare timestamps
            keepStreak = hoursApart(currentStamp, previousStamp, SEVENTY_TWO_HOURS);
        }

        return keepStreak;
    }

    public static LocalDate today() {
        return new LocalDate(DateTimeZone.getDefault());
    }
}
