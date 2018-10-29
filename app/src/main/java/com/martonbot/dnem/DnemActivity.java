package com.martonbot.dnem;

import android.view.TextureView;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.LocalDate;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class DnemActivity {

    public List<DnemTrackingLog> trackingLogs;
    private long id;
    private String label;
    private String details;
    private int currentStreak;
    private int bestStreak;
    private int starCounter;
    private boolean isActive;
    private boolean allowStars;
    private boolean weekendsOn;
    private boolean remindersOn;
    // todo weekends on
    // todo remindersOn

    public DnemActivity(long id, String label, String details, boolean isActive, boolean allowStars, boolean weekendsOn) {
        this.id = id;
        this.label = label;
        this.details = details;
        this.isActive = isActive;
        this.allowStars = allowStars;
        this.weekendsOn = weekendsOn;
    }

    public DnemActivity() {
        this.id = 0;
        this.label = "";
        this.details = "";
        this.isActive = false;
        this.trackingLogs = new LinkedList<>();
    }

    public long getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getDetails() {
        return details;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public int getBestStreak() {
        return bestStreak;
    }

    public void setTrackingLogs(List<DnemTrackingLog> trackingLogs) {
        this.trackingLogs = trackingLogs; // at this stage the list is in descending order, most recent first
        processTrackingLogs();
    }

    public void processTrackingLogs() {
        Collections.sort(trackingLogs);
        computeStreaks();
        Collections.sort(trackingLogs, Collections.<DnemTrackingLog>reverseOrder());
    }

    private void computeStreaks() {
        // todo take into account the preference for 'allow stars'
        if (trackingLogs == null) {
            throw new IllegalStateException("The tracking logs must have been loaded first");
        }

        int maxStreak = 0; // keeps a record of the max consecutive logs
        int runningStreak = 0; // the current consecutive log count
        int starCounter = 0; // a stack that increments with consecutive logs but is lost when streak is broken

        // start from the first tracking log
        DnemTrackingLog previousLog = null;
        boolean isFirstTrackingLog = true;

        // iterate through all the tracking logs in ascending order
        for (DnemTrackingLog currentLog : trackingLogs) {
            if (isFirstTrackingLog) {
                // initialisation
                runningStreak = 1; // we start at 1 because there is a tracking log for that day
                if (allowStars) {
                    starCounter = 1;
                }
                isFirstTrackingLog = false;
            } else {
                // the previous tracking log exists, we check that it is chronologically before the current one
                if (previousLog.getTimestamp() >= currentLog.getTimestamp()) {
                    throw new IllegalStateException("Previous tracking log is chronologically after current log.");
                }
                // let's compare the current log with the previous log and see whether the streak is conserved
                int daysMissed = daysMissed(currentLog, previousLog);
                if (daysMissed < 1) {
                    // well done! we increment the streakCount
                    runningStreak++;
                    // and the star count is incremented as well
                    if (allowStars) {
                        starCounter++;
                    }
                } else {
                    // shit we missed a day, or more, apparently
                    // now can we patch the missing day(s) using the star counter, or do we simply lose the streak?
                    // do we have a star to use?
                    if (allowStars && daysMissed <= starStack(starCounter)) {
                        // we can patch our streak
                        runningStreak++; // the streak increases because we are on a tracking log
                    } else {
                        // we lose the streak
                        runningStreak = 1; // but we land on 1 because there is a tracking log for the day considered
                    }
                    // and we downgrade the star stack anyway because the streak was broken
                    if (allowStars) {
                        starCounter = downgradeStarCounter(starCounter, daysMissed);
                    }
                }
            }
            currentLog.setStreakCounter(runningStreak);
            currentLog.setStarCounter(starCounter);
            // update the max
            maxStreak = Math.max(runningStreak, maxStreak);
            // move the log down the stack
            previousLog = currentLog;
        }
        // we have finished going through the tracking logs
        // now to see if the streak and star are kept with regards to today!
        if (previousLog == null) {
            // there were no tracking logs at all
            currentStreak = 0;
            bestStreak = 0;
        } else {
            // there is at least one tracking log
            long todayStamp = new Instant().getMillis();
            DateTimeZone todayZone = DateTimeZone.getDefault();
            // has it been more than one day?
            int daysMissed = daysMissed(todayStamp, todayZone, previousLog);
            if (daysMissed < 1) {
                // all good it's been less than a day, we don't lose anything
            } else {
                // can we patch the gap with stars?
                if (allowStars && daysMissed < starStack(starCounter)) {
                    // we don't lose the streak
                } else {
                    // we lose the streak
                    runningStreak = 0;
                }
                if (allowStars) {
                    // we downgrade the star counter anyways
                    starCounter = downgradeStarCounter(starCounter, daysMissed);
                }
            }
            currentStreak = runningStreak;
            bestStreak = maxStreak;
            this.starCounter = starCounter;
        }
    }

    private int starStack(int starCounter) {
        int starStack;
        if (starCounter < 0) {
            throw new IllegalArgumentException("The star counter cannot be negative.");
        } else if (starCounter < 7) {
            starStack = 0;
        } else if (starCounter < 14) {
            starStack = 1; // bronze
        } else if (starCounter < 28) {
            starStack = 2; // silver
        } else {
            starStack = 3; // gold
        }
        return starStack;
    }

    private int downgradeStarCounter(int starCounter, int daysMissed) {
        while (daysMissed > 0) {
            starCounter = downgradeStarCounter(starCounter);
            daysMissed--;
        }
        return starCounter;
    }

    private int downgradeStarCounter(int starCounter) {
        int remainder;
        if (starCounter < 1) {
            throw new IllegalArgumentException("The star counter cannot be less than one.");
        } else if (starCounter < 14) {
            remainder = 1; // bronze to nothing
        } else if (starCounter < 28) {
            remainder = 7; // silver to bronze
        } else {
            remainder = 14; // gold to silver
        }
        return remainder;
    }

    private int daysMissed(DnemTrackingLog currentLog, DnemTrackingLog previousLog) {
        return Time.daysMissed(currentLog.getTimestamp(), currentLog.getTimezone(), previousLog.getTimestamp(), previousLog.getTimezone());
    }

    private int daysMissed(long todayStamp, DateTimeZone todayZone, DnemTrackingLog previousLog) {
        return Time.daysMissed(todayStamp, todayZone, previousLog.getTimestamp(), previousLog.getTimezone());
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean allowStars() {
        return allowStars;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setAllowStars(boolean allow) {
        allowStars = allow;
    }

    public void setWeekendsOn(boolean on) {
        weekendsOn = on;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isDoneForToday() {
        boolean isDone = false;
        if (trackingLogs != null && trackingLogs.size() > 0) {
            DnemTrackingLog mostRecentTrackingLog = getMostRecentTrackingLog();
            DateTimeZone timezone = mostRecentTrackingLog.getTimezone();
            long timestamp = mostRecentTrackingLog.getTimestamp();
            isDone = new LocalDate(timestamp, timezone).equals(Time.today());
        }
        return isDone;
    }

    public int getStarCounter() {
        return starCounter;
    }

    public DnemTrackingLog getMostRecentTrackingLog() {
        return trackingLogs.get(0);
    }

    public boolean trackingLogFor(LocalDate date) {
        for (DnemTrackingLog tl : trackingLogs) {
            if (tl.getDay().equals(date)) {
                return true;
            }
        }
        return false;
    }

    public void updateTo(DnemActivity newActivity) {
        this.id = newActivity.id;
        this.label = newActivity.label;
        this.details = newActivity.details;
        this.isActive = newActivity.isActive;
        this.allowStars = newActivity.allowStars;
        this.weekendsOn = newActivity.weekendsOn;
        trackingLogs.clear();
        trackingLogs.addAll(newActivity.trackingLogs);
    }
}
