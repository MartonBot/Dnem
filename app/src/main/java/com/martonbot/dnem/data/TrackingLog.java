package com.martonbot.dnem.data;

import android.support.annotation.NonNull;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

public class TrackingLog implements Comparable<TrackingLog> {

    private long id;
    private long activityId;
    private long timestamp;
    private LocalDate day;
    private DateTimeZone timezone;

    private int starCounter;

    private int streakCounter;

    public TrackingLog(long id, long activityId, long timestamp, LocalDate day, DateTimeZone timezone) {
        this.id = id;
        this.activityId = activityId;
        this.timestamp = timestamp;
        this.day = day;
        this.timezone = timezone;
    }

    public TrackingLog(Dnem dnem, long timestamp) {
        DateTimeZone tz = DateTimeZone.getDefault();
        LocalDate today = new LocalDate(timestamp, tz);

        this.id = 0;
        this.activityId = dnem.getId();
        this.timestamp = timestamp;
        this.day = today;
        this.timezone = tz;
    }

    public long getActivityId() {
        return activityId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public LocalDate getDay() {
        return day;
    }

    public DateTimeZone getTimezone() {
        return timezone;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public int compareTo(@NonNull TrackingLog tl) {
        return Long.signum(this.timestamp - tl.timestamp);
    }

    public int getStarCounter() {
        return starCounter;
    }

    public void setStarCounter(int starCounter) {
        this.starCounter = starCounter;
    }


    public int getStreakCounter() {
        return streakCounter;
    }

    public void setStreakCounter(int streakCounter) {
        this.streakCounter = streakCounter;
    }


}