package com.martonbot.dnem;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.LocalDate;

public class TrackingLogs {

    public static DnemTrackingLog insert(SQLiteDatabase db, long activityId) {
        Instant now = new Instant();
        long timestamp = now.getMillis();
        DateTimeZone timezone = DateTimeZone.getDefault();
        LocalDate today = new LocalDate(now, timezone);

        ContentValues trackingLogValues = new ContentValues();
        trackingLogValues.put(Dnem.TrackingLog.C_ACTIVITY_ID, activityId); // we set the foreign key
        trackingLogValues.put(Dnem.TrackingLog.C_TIMESTAMP, timestamp); // we set the current instant
        trackingLogValues.put(Dnem.TrackingLog.C_UTC_DAY, today.toString()); // we set the local date
        trackingLogValues.put(Dnem.TrackingLog.C_TIMEZONE, DateTimeZone.getDefault().getID()); // we set the local timezone

        long id = db.insertOrThrow(Dnem.TrackingLog.T_NAME, null, trackingLogValues);

        return new DnemTrackingLog(id, activityId, timestamp, today, timezone);
    }

    public static void delete(SQLiteDatabase db, long trackingLogId) {
        String where = Dnem.TrackingLog._ID + " =? ";
        String[] whereArgs = new String[]{
                "" + trackingLogId
        };
        db.delete(
                Dnem.TrackingLog.T_NAME,
                where,
                whereArgs
        );
    }

}
