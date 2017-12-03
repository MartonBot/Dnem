package com.martonbot.dnem;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.LocalDate;

import java.util.LinkedList;
import java.util.List;

public class DnemDataLoader {

    private DnemDataLoader() {}

    public static List<DnemActivity> loadAll(Context context) {
        List<DnemActivity> activities = loadActivities(context);

        for (DnemActivity activity : activities) {
            loadTrackingLogs(context, activity);
        }
        return activities;
    }

    private static List<DnemActivity> loadActivities(Context context) {
        List<DnemActivity> activities;

        // todo put all the arguments back there
        SQLiteDatabase db = new DnemDbHelper(context).getReadableDatabase();
        Cursor cursor = db.query(
                DnemDbHelper.activitiesJoinTable,
                DnemDbHelper.activitiesProjection,
                null,
                null,
                DnemDbHelper.activitiesGroupBy,
                null,
                DnemDbHelper.activitiesOrderBy
        );

        activities = new LinkedList<>();

        int idIndex = cursor.getColumnIndex(Dnem.Activity._ID);
        int labelIndex = cursor.getColumnIndex(Dnem.Activity.C_LABEL);
        int detailsIndex = cursor.getColumnIndex(Dnem.Activity.C_DETAILS);
        int isActiveIndex = cursor.getColumnIndex(Dnem.Schedule.C_IS_ACTIVE);

        while (cursor.moveToNext()) {
            long id = cursor.getLong(idIndex);
            String label = cursor.getString(labelIndex);
            String details = cursor.getString(detailsIndex);
            boolean isActive = cursor.getInt(isActiveIndex) > 0;
            DnemActivity activity = new DnemActivity(id, label, details, isActive);
            activities.add(activity);
        }
        cursor.close();
        db.close();
        return activities;
    }

    private static void loadTrackingLogs(Context context, DnemActivity activity) {
        long activityId = activity.getId();

        String[] queryProjection = {
                Dnem.TrackingLog._ID,
                Dnem.TrackingLog.C_UTC_DAY,
                Dnem.TrackingLog.C_TIMESTAMP,
                Dnem.TrackingLog.C_TIMEZONE
        };

        String querySelection = Dnem.TrackingLog.T_NAME + "." + Dnem.TrackingLog.C_ACTIVITY_ID + " = ?"
                + " AND " + Dnem.TrackingLog.T_NAME + "." + Dnem.TrackingLog.C_TIMESTAMP + " <= ?";
        String[] querySelectionArgs = {
                "" + activityId,
                "" + new Instant().getMillis()
        };

        String orderBy = Dnem.TrackingLog.C_TIMESTAMP + " DESC";

        SQLiteDatabase db = new DnemDbHelper(context).getReadableDatabase();
        Cursor cursor = db.query(
                Dnem.TrackingLog.T_NAME,
                queryProjection,
                querySelection,
                querySelectionArgs,
                null,
                null,
                orderBy
        );

        List<DnemTrackingLog> trackingLogs = new LinkedList<>();

        // indices
        int idIndex = cursor.getColumnIndex(Dnem.TrackingLog._ID);
        int timezoneIndex = cursor.getColumnIndex(Dnem.TrackingLog.C_TIMEZONE);
        int timestampIndex = cursor.getColumnIndex(Dnem.TrackingLog.C_TIMESTAMP);
        int dayIndex = cursor.getColumnIndex(Dnem.TrackingLog.C_UTC_DAY);

        while (cursor.moveToNext()) {
            long id = cursor.getLong(idIndex);
            DateTimeZone timezone = DateTimeZone.forID(cursor.getString(timezoneIndex));
            long timestamp = cursor.getLong(timestampIndex);
            LocalDate day = new LocalDate(cursor.getString(dayIndex));
            DnemTrackingLog trackingLog = new DnemTrackingLog(id, activityId, timestamp, day, timezone);
            trackingLogs.add(trackingLog);
        }
        cursor.close();
        db.close();
        activity.setTrackingLogs(trackingLogs);
    }

}
