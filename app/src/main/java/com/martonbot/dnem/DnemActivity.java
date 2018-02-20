package com.martonbot.dnem;

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

    public DnemActivity(long id, String label, String details, boolean isActive) {
        this.id = id;
        this.label = label;
        this.details = details;
        this.isActive = isActive;
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
        Collections.sort(trackingLogs);
        computeStreaks();
        Collections.sort(trackingLogs, Collections.<DnemTrackingLog>reverseOrder());
    }

    public void addNewTrackingLog(DnemTrackingLog newTrackingLog) {
        trackingLogs.add(0, newTrackingLog);
        Collections.sort(trackingLogs);
        computeStreaks();
        Collections.sort(trackingLogs, Collections.<DnemTrackingLog>reverseOrder());
    }

    public void removeLatestTrackinglog() {
        trackingLogs.remove(0);
        Collections.sort(trackingLogs);
        computeStreaks();
        Collections.sort(trackingLogs, Collections.<DnemTrackingLog>reverseOrder());
    }

    private void computeStreaks() {
        if (trackingLogs == null) {
            throw new IllegalStateException("The tracking logs must have been loaded first");
        }

        int maxStreak = 0; // keeps a record of the max consecutive logs
        int runningStreak = 0; // the current consecutive log count
        int starCounter = 0; // a stack that increments with consecutive logs but is lost when streak is broken

        // start from the first tracking log
        DnemTrackingLog previousLog = null;
        boolean isFirstTrackingLog = true;

        for (DnemTrackingLog currentLog : trackingLogs) {
            if (isFirstTrackingLog) {
                runningStreak = 1;
                starCounter = 1;
                isFirstTrackingLog = false;
            } else {
                // sanity check
                if (previousLog.getTimestamp() > currentLog.getTimestamp()) {
                    throw new IllegalStateException("Previous tracking log is chronologically after current log.");
                }
                // let's compare the current log with the previous log and see whether the streak is conserved
                if (keepStreak(currentLog, previousLog)) {
                    // well done! we increment the streakCount
                    runningStreak++;
                    // and the star count is incremented as well
                    starCounter++;
                } else {
                    // the streak was broken!
                    // now can we patch it using the star counter, or do we simply lose the streak?
                    // do we have a star to use?
                    if (starCounter >= 7 && keepStreakWithStar(currentLog, previousLog)) {
                        // we can patch our streak
                        runningStreak++;
                    } else {
                        // we lose the streak
                        runningStreak = 1;
                    }
                    // we lose the star stack anyway because the streak was broken
                    starCounter = 1;
                }
            }
            currentLog.setStreakCounter(runningStreak);
            currentLog.setStarCounter(starCounter);
            // update the max
            maxStreak = Math.max(runningStreak, maxStreak);
            // move
            previousLog = currentLog;
        }
        // we have finished going through the tracking logs
        // now to see if the streak and star are kept with regards to today!
        if (previousLog == null) {
            // there were no tracking logs at all
            currentStreak = 0;
            bestStreak = 0;
        } else {
            long todayStamp = new Instant().getMillis();
            DateTimeZone todayZone = DateTimeZone.getDefault();
            if (!keepStreak(todayStamp, todayZone, previousLog)) {
                if (starCounter >= 7 && keepStreakWithStar(todayStamp, todayZone, previousLog)) {
                    // we don't lose streak
                } else {
                    runningStreak = 0;
                }
                starCounter = 0;
            }
            currentStreak = runningStreak;
            bestStreak = maxStreak;
            this.starCounter = starCounter;
        }
    }

    private boolean keepStreak(DnemTrackingLog current, DnemTrackingLog previous) {
        return Time.keepStreak(current.getTimestamp(), current.getTimezone(), previous.getTimestamp(), previous.getTimezone(), false);
    }

    private boolean keepStreak(long currentTimestamp, DateTimeZone currentTimeZone, DnemTrackingLog previous) {
        return Time.keepStreak(currentTimestamp, currentTimeZone, previous.getTimestamp(), previous.getTimezone(), false);
    }

    private boolean keepStreakWithStar(DnemTrackingLog current, DnemTrackingLog previous) {
        return Time.keepStreak(current.getTimestamp(), current.getTimezone(), previous.getTimestamp(), previous.getTimezone(), true);
    }

    private boolean keepStreakWithStar(long currentTimestamp, DateTimeZone currentTimeZone, DnemTrackingLog previous) {
        return Time.keepStreak(currentTimestamp, currentTimeZone, previous.getTimestamp(), previous.getTimezone(), true);
    }


    public boolean isActive() {
        return isActive;
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

}
