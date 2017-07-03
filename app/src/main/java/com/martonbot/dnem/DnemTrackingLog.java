package com.martonbot.dnem;

import android.support.annotation.NonNull;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

public class DnemTrackingLog implements Comparable<DnemTrackingLog> {

    private long id;
    private long activityId;
    private long timestamp;
    private LocalDate day;
    private DateTimeZone timezone;

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

    public DnemTrackingLog(long id, long activityId, long timestamp, LocalDate day, DateTimeZone timezone) {
        this.id = id;
        this.activityId = activityId;
        this.timestamp = timestamp;
        this.day = day;
        this.timezone = timezone;
    }

    public long getId() {
        return id;
    }

    @Override
    public int compareTo(@NonNull DnemTrackingLog tl) {
        return Long.signum(this.timestamp - tl.timestamp);
    }
}
