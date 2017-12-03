package com.martonbot.dnem;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

/**
 * Created by mgrihangne on 23/06/2017.
 */

public class Time {

    private static boolean hoursApart(long currentStamp, long previousStamp, int hoursAllowed) {
        // todo check this as I'm not sure it's doing the right thing?
        if (currentStamp <= previousStamp) {
            throw new IllegalStateException("Current timestamp has to be chronologically after previous!");
        }
        return (currentStamp - previousStamp) <= hoursAllowed * 60 * 60 * 1000;
    }

    public static boolean keepStreak(long currentStamp, DateTimeZone currentZone, long previousStamp, DateTimeZone previousZone, boolean hasStar) {
        boolean keepStreak = false;

        int daysAllowed = hasStar ? 2 : 1;
        int hoursAllowed = hasStar ? 60 : 36;

        if (currentZone.equals(previousZone)) {
            LocalDate current = new LocalDate(currentStamp, currentZone);
            LocalDate previous = new LocalDate(previousStamp, previousZone);

            keepStreak = current.minusDays(daysAllowed).equals(previous) || current.equals(previous);
        }
        else {
            // simply compare timestamps
            keepStreak = hoursApart(currentStamp, previousStamp, hoursAllowed);
        }

        return keepStreak;
    }

    public static LocalDate today() {
        return new LocalDate(DateTimeZone.getDefault());
    }
}
