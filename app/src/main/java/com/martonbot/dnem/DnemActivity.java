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
    // todo weekends on

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
                runningStreak = 1;
                if (allowStars()) {
                    starCounter = 1;
                }
                isFirstTrackingLog = false;
            } else {
                // the previous tracking log exists, we check that it is chronologically before the current one
                if (previousLog.getTimestamp() >= currentLog.getTimestamp()) {
                    throw new IllegalStateException("Previous tracking log is chronologically after current log.");
                }
                // let's compare the current log with the previous log and see whether the streak is conserved
                if (keepStreakDefault(currentLog, previousLog)) {
                    // well done! we increment the streakCount
                    runningStreak++;
                    // and the star count is incremented as well
                    if (allowStars()) {
                        starCounter++;
                    }
                } else {
                    // shit we missed a day apparently
                    // now can we patch it using the star counter, or do we simply lose the streak?
                    // do we have a star to use?
                    if (allowStars() && starCounter >= 7 && keepStreakWithStar(currentLog, previousLog)) {
                        // we can patch our streak
                        runningStreak++;
                    } else {
                        // we lose the streak
                        runningStreak = 1;
                    }
                    // we lose the star stack anyway because the streak was broken
                    if (allowStars()) {
                        starCounter = 1;
                    }

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
            if (!keepStreakDefault(todayStamp, todayZone, previousLog.getTimestamp(), previousLog.getTimezone())) {
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

    private boolean allowStars() {
        return allowStars;
    }

    // todo not the most elegant
    private boolean keepStreakDefault(DnemTrackingLog current, DnemTrackingLog previous) {
        return Time.isStreakConserved(current.getTimestamp(), current.getTimezone(), previous.getTimestamp(), previous.getTimezone());
    }

    private boolean keepStreakDefault(long currentTimestamp, DateTimeZone currentTimeZone, long previousTimestamp, DateTimeZone previousTimeZone) {
        return Time.isStreakConserved(currentTimestamp, currentTimeZone, previousTimestamp, previousTimeZone);
    }

    private boolean keepStreakWithStar(DnemTrackingLog current, DnemTrackingLog previous) {
        return Time.isStreakConservedUsingStar(current.getTimestamp(), current.getTimezone(), previous.getTimestamp(), previous.getTimezone());
    }

    private boolean keepStreakWithStar(long currentTimestamp, DateTimeZone currentTimeZone, DnemTrackingLog previous) {
        return Time.isStreakConservedUsingStar(currentTimestamp, currentTimeZone, previous.getTimestamp(), previous.getTimezone());
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
}
