package com.martonbot.dnem;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.martonbot.dnem.activities.UpdatableActivity;

import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.LocalDate;

public class TrackingLogs {

    /**
     * Inserts a new tracking log into the database. If successful, it inserts the corresponding tracking log object for the Dnem activity and updates the UI of the relevant activity if necessary.
     * @param db the database to write to
     * @param activity the Dnem activity for which to insert the tracking log
     * @param updatableActivity the updatable Android activity whose UI should be refreshed
     */
    public static void insert(SQLiteDatabase db, DnemActivity activity, UpdatableActivity updatableActivity) {
        Instant now = new Instant();
        long timestamp = now.getMillis();
        insert(db, activity, updatableActivity, timestamp);
    }

    public static void insert(SQLiteDatabase db, DnemActivity activity, UpdatableActivity updatableActivity, long timestamp) {
        DateTimeZone timezone = DateTimeZone.getDefault();
        LocalDate today = new LocalDate(timestamp, timezone);
        long activityId = activity.getId();

        ContentValues trackingLogValues = new ContentValues();
        trackingLogValues.put(Dnem.TrackingLog.C_ACTIVITY_ID, activityId); // we set the foreign key
        trackingLogValues.put(Dnem.TrackingLog.C_TIMESTAMP, timestamp); // we set the current instant
        trackingLogValues.put(Dnem.TrackingLog.C_UTC_DAY, today.toString()); // we set the local date
        trackingLogValues.put(Dnem.TrackingLog.C_TIMEZONE, DateTimeZone.getDefault().getID()); // we set the local timezone

        long id = db.insertOrThrow(Dnem.TrackingLog.T_NAME, null, trackingLogValues);

        // insert the tracking log
        activity.addNewTrackingLog(new DnemTrackingLog(id, activityId, timestamp, today, timezone));
        // update the Android activity accordingly
        if (updatableActivity != null) {
            updatableActivity.updateUI();
        }
    }

    public static void delete(SQLiteDatabase db, long trackingLogId, DnemActivity activity, UpdatableActivity updatableActivity) {
        String where = Dnem.TrackingLog._ID + " =? ";
        String[] whereArgs = new String[]{
                "" + trackingLogId
        };
        db.delete(
                Dnem.TrackingLog.T_NAME,
                where,
                whereArgs
        );
        // delete the tracking log and update the UI
        activity.removeLatestTrackinglog();
        if (updatableActivity != null) {
            updatableActivity.updateUI();
        }
    }

}
