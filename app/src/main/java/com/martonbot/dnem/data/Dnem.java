package com.martonbot.dnem.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.martonbot.dnem.Time;
import com.martonbot.dnem.database.DnemDatabase;
import com.martonbot.dnem.database.DnemDbHelper;

import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.LocalDate;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * And instance of this class represents an activity that needs to be done on a daily basis. For each day it is done, a tracking log is created for this Dnem, with the date of the day.
 * Some properties of the Dnem are stored in the database and loaded at creation time, some other properties need to be computed.
 */
public class Dnem {

    public List<TrackingLog> trackingLogs;

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

    public Dnem(long id) {
        this.id = id;
        this.trackingLogs = new LinkedList<>();
    }

    /**
     * This method triggers the loading of the Dnem information and tracking logs from the database.
     */
    void loadFromDatabase(SQLiteDatabase db) {
        // load info
        loadInfoFromDatabase(db);
        // load tracking logs
        loadTrackingLogsFromDb(db);
        // process the tracking logs to get the streak, stars etc
        processTrackingLogs();
    }

    /**
     * This method populates the fields of the Dnem with the values pulled from a database. The Dnem must already be instantiated.
     *
     * @param db the database to use
     */
    private void loadInfoFromDatabase(SQLiteDatabase db) {
        String querySelection = DnemDatabase.Activity.T_NAME + "." + DnemDatabase.Activity._ID + " = ?";
        String[] querySelectionArgs = {
                "" + id
        };
        Cursor cursor = db.query(
                DnemDbHelper.activitiesJoinTable,
                DnemDbHelper.activitiesProjection,
                querySelection,
                querySelectionArgs,
                DnemDbHelper.activitiesGroupBy,
                null,
                DnemDbHelper.activitiesOrderBy
        );

        cursor.moveToNext();

        int labelIndex = cursor.getColumnIndex(DnemDatabase.Activity.C_LABEL);
        int detailsIndex = cursor.getColumnIndex(DnemDatabase.Activity.C_DETAILS);
        int isActiveIndex = cursor.getColumnIndex(DnemDatabase.Schedule.C_IS_ACTIVE);
        int allowStarsIndex = cursor.getColumnIndex(DnemDatabase.Schedule.C_ALLOW_STARS);
        int weekendsOnIndex = cursor.getColumnIndex(DnemDatabase.Schedule.C_WEEKENDS_ON);

        this.label = cursor.getString(labelIndex);
        this.details = cursor.getString(detailsIndex);
        this.isActive = cursor.getInt(isActiveIndex) > 0;
        this.allowStars = cursor.getInt(allowStarsIndex) > 0;
        this.weekendsOn = cursor.getInt(weekendsOnIndex) > 0;

        cursor.close();
    }

    private void loadTrackingLogsFromDb(SQLiteDatabase db) {
        trackingLogs.clear();

        String[] queryProjection = {
                DnemDatabase.TrackingLog._ID,
                DnemDatabase.TrackingLog.C_UTC_DAY,
                DnemDatabase.TrackingLog.C_TIMESTAMP,
                DnemDatabase.TrackingLog.C_TIMEZONE
        };

        String querySelection = DnemDatabase.TrackingLog.T_NAME + "." + DnemDatabase.TrackingLog.C_ACTIVITY_ID + " = ?"
                + " AND " + DnemDatabase.TrackingLog.T_NAME + "." + DnemDatabase.TrackingLog.C_TIMESTAMP + " <= ?";
        String[] querySelectionArgs = {
                "" + this.id,
                "" + new Instant().getMillis()
        };

        String orderBy = DnemDatabase.TrackingLog.C_TIMESTAMP + " DESC";

        Cursor cursor = db.query(
                DnemDatabase.TrackingLog.T_NAME,
                queryProjection,
                querySelection,
                querySelectionArgs,
                null,
                null,
                orderBy
        );

        // indices
        int trackingLogIdIndex = cursor.getColumnIndex(DnemDatabase.TrackingLog._ID);
        int timezoneIndex = cursor.getColumnIndex(DnemDatabase.TrackingLog.C_TIMEZONE);
        int timestampIndex = cursor.getColumnIndex(DnemDatabase.TrackingLog.C_TIMESTAMP);
        int dayIndex = cursor.getColumnIndex(DnemDatabase.TrackingLog.C_UTC_DAY);

        while (cursor.moveToNext()) {
            long trackingLogId = cursor.getLong(trackingLogIdIndex);
            DateTimeZone timezone = DateTimeZone.forID(cursor.getString(timezoneIndex));
            long timestamp = cursor.getLong(timestampIndex);
            LocalDate day = new LocalDate(cursor.getString(dayIndex));
            TrackingLog trackingLog = new TrackingLog(trackingLogId, id, timestamp, day, timezone);
            trackingLogs.add(trackingLog);
        }
        cursor.close();
    }

    private void processTrackingLogs() {
        Collections.sort(trackingLogs);
        computeStreaks();
        Collections.sort(trackingLogs, Collections.<TrackingLog>reverseOrder());
    }

    private void computeStreaks() {

        if (trackingLogs == null) {
            throw new IllegalStateException("The tracking logs must have been loaded first");
        }

        int maxStreak = 0; // keeps a record of the max consecutive logs
        int runningStreak = 0; // the current consecutive log count
        int starCounter = 0; // a stack that increments with consecutive logs but is lost when streak is broken

        // start from the first tracking log
        TrackingLog previousLog = null;
        boolean isFirstTrackingLog = true;

        // iterate through all the tracking logs in ascending order
        for (TrackingLog currentLog : trackingLogs) {
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
                if (allowStars && daysMissed <= starStack(starCounter)) {
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

    private int daysMissed(TrackingLog currentLog, TrackingLog previousLog) {
        return Time.daysMissed(currentLog.getTimestamp(), currentLog.getTimezone(), previousLog.getTimestamp(), previousLog.getTimezone());
    }

    private int daysMissed(long todayStamp, DateTimeZone todayZone, TrackingLog previousLog) {
        return Time.daysMissed(todayStamp, todayZone, previousLog.getTimestamp(), previousLog.getTimezone());
    }


    public TrackingLog getTodayTrackingLog() {
        TrackingLog tl = null;
        if (trackingLogs != null && trackingLogs.size() > 0) {
            TrackingLog mostRecentTrackingLog = getMostRecentTrackingLog();
            DateTimeZone timezone = mostRecentTrackingLog.getTimezone();
            long timestamp = mostRecentTrackingLog.getTimestamp();
            if (new LocalDate(timestamp, timezone).equals(Time.today())) {
                tl = mostRecentTrackingLog;
            }
        }
        return tl;
    }

    public int getStarCounter() {
        return starCounter;
    }

    private TrackingLog getMostRecentTrackingLog() {
        return trackingLogs.get(0);
    }

    public boolean trackingLogFor(LocalDate date) {
        for (TrackingLog tl : trackingLogs) {
            if (tl.getDay().equals(date)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method should be called when inserting a new tracking log in the database, so that it is also added to the corresponding Dnem.
     */
    public void insert(Context context, TrackingLog trackingLog) {
        if (trackingLog.getId() != 0) {
            throw new IllegalStateException("Illegal insertion of existing tracking log");
        }
        if (trackingLog.getActivityId() != this.getId()) {
            throw new IllegalStateException("Illegal insertion of a tracking log for the wrong Dnem");
        }

        DateTimeZone timezone = DateTimeZone.getDefault();
        LocalDate today = new LocalDate(trackingLog.getTimestamp(), timezone);

        ContentValues trackingLogValues = buildTrackingLogContentValues(trackingLog);

        // insert
        SQLiteDatabase db = new DnemDbHelper(context).getWritableDatabase();
        long newTrackingLogId = db.insertOrThrow(DnemDatabase.TrackingLog.T_NAME, null, trackingLogValues);
        trackingLog.setId(newTrackingLogId);
        db.close(); // todo handle the possible exception and close in a finally?

        trackingLogs.add(trackingLog);
        processTrackingLogs();
    }

    public void delete(Context context, TrackingLog trackingLog) {
        long trackingLogId = trackingLog.getId();
        if (trackingLog.getId() == 0) {
            throw new IllegalStateException("Illegal deletion of inexisting tracking log");
        }
        String where = DnemDatabase.TrackingLog._ID + " =? ";
        String[] whereArgs = new String[]{
                "" + trackingLogId
        };

        // delete
        SQLiteDatabase db = new DnemDbHelper(context).getWritableDatabase();
        db.delete(
                DnemDatabase.TrackingLog.T_NAME,
                where,
                whereArgs
        );
        db.close();

        trackingLogs.remove(trackingLog);
        processTrackingLogs();
    }

    private ContentValues buildTrackingLogContentValues(TrackingLog trackingLog) {
        ContentValues trackingLogValues = new ContentValues();
        trackingLogValues.put(DnemDatabase.TrackingLog.C_ACTIVITY_ID, trackingLog.getActivityId()); // we set the foreign key
        trackingLogValues.put(DnemDatabase.TrackingLog.C_TIMESTAMP, trackingLog.getTimestamp()); // we set the current instant
        trackingLogValues.put(DnemDatabase.TrackingLog.C_UTC_DAY, trackingLog.getDay().toString()); // we set the local date
        trackingLogValues.put(DnemDatabase.TrackingLog.C_TIMEZONE, trackingLog.getTimezone().getID()); // we set the local timezone
        return trackingLogValues;
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
    } // todo

    public void setId(long id) {
        this.id = id;
    }

    public boolean isDoneForToday() {
        return getTodayTrackingLog() != null;
    }

}
