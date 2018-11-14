package com.martonbot.dnem;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.martonbot.dnem.activities.UpdatableActivity;

import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.LocalDate;

public class TrackingLogs {

    /**
     * Inserts a new tracking log into the database. If successful, it inserts the corresponding tracking log object for the DnemDatabase activity and updates the UI of the relevant activity if necessary.
     *
     * @param dnem          the DnemDatabase activity for which to insert the tracking log
     * @param updatableActivity the updatable Android activity whose UI should be refreshed
     */
    static void insert(Dnem dnem, UpdatableActivity updatableActivity) {
        Instant now = new Instant();
        long timestamp = now.getMillis();
        insert(dnem, updatableActivity, timestamp);
    }

    public static void insert(Dnem dnem, UpdatableActivity updatableActivity, long timestamp) {
        DateTimeZone timezone = DateTimeZone.getDefault();
        LocalDate today = new LocalDate(timestamp, timezone);
        long activityId = dnem.getId();

        ContentValues trackingLogValues = new ContentValues();
        trackingLogValues.put(DnemDatabase.TrackingLog.C_ACTIVITY_ID, activityId); // we set the foreign key
        trackingLogValues.put(DnemDatabase.TrackingLog.C_TIMESTAMP, timestamp); // we set the current instant
        trackingLogValues.put(DnemDatabase.TrackingLog.C_UTC_DAY, today.toString()); // we set the local date
        trackingLogValues.put(DnemDatabase.TrackingLog.C_TIMEZONE, DateTimeZone.getDefault().getID()); // we set the local timezone

        // insert
        SQLiteDatabase db = new DnemDbHelper(updatableActivity).getWritableDatabase();
        long id = db.insertOrThrow(DnemDatabase.TrackingLog.T_NAME, null, trackingLogValues);
        db.close(); // todo handle the possible exception and close in a finally

        // update the Android activity accordingly
        if (updatableActivity != null) {
            dnem.onTrackingLogInserted(updatableActivity); // the updatable activity is the context
            updatableActivity.update();
        }
    }

    static void delete(long trackingLogId, Dnem dnem, UpdatableActivity updatableActivity) {
        String where = DnemDatabase.TrackingLog._ID + " =? ";
        String[] whereArgs = new String[]{
                "" + trackingLogId
        };

        // delete
        SQLiteDatabase db = new DnemDbHelper(updatableActivity).getWritableDatabase();
        db.delete(
                DnemDatabase.TrackingLog.T_NAME,
                where,
                whereArgs
        );
        db.close();

        if (updatableActivity != null) {
            dnem.onTrackingLogDeleted(updatableActivity); // the updatable activity is the context
            updatableActivity.update();
        }
    }

}
