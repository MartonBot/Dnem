package com.martonbot.dnem;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.martonbot.dnem.Dnem.Activity;
import com.martonbot.dnem.Dnem.Schedule;
import com.martonbot.dnem.Dnem.TrackingLog;

/**
 * Created by mgrihangne on 5/05/2017.
 */
public class DnemDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Dnem.db";

    public static final String activitiesJoinTable = Activity.T_NAME
            + " JOIN "
            + Schedule.T_NAME
            + " ON " + Activity.T_NAME + "." + Activity._ID
            + " = " + Schedule.T_NAME + "." + Schedule.C_ACTIVITY_ID
            + " LEFT OUTER JOIN "
            + TrackingLog.T_NAME
            + " ON " + Activity.T_NAME + "." + Activity._ID
            + " = " + TrackingLog.T_NAME + "." + TrackingLog.C_ACTIVITY_ID;
    public static final String[] activitiesProjection = {
            Activity.T_NAME + "." + Activity._ID,
            Activity.C_LABEL,
            Activity.C_DETAILS,
            Schedule.C_IS_ACTIVE,
            "MAX(" + TrackingLog.C_TIMESTAMP + ") AS " + TrackingLog.C_TIMESTAMP,
            TrackingLog.C_TIMEZONE
    };

    public static final String activitiesSelection = TrackingLog.T_NAME + "." + TrackingLog.C_TIMESTAMP +
            " <= ?" +
            " OR " + TrackingLog.T_NAME + "." + TrackingLog.C_TIMESTAMP +
            " IS NULL";

    public static final String activitiesGroupBy = Activity.T_NAME + "." + Activity._ID;

    public static final String activitiesOrderBy = TrackingLog.C_TIMESTAMP + " ASC, " + Activity.C_LABEL + " ASC";

    public DnemDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Dnem.SQL_CREATE_ACTIVITY);
        db.execSQL(Dnem.SQL_CREATE_SCHEDULE);
        db.execSQL(Dnem.SQL_CREATE_TRACKING_LOG);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(Dnem.SQL_DELETE_ACTIVITY);
        db.execSQL(Dnem.SQL_DELETE_SCHEDULE);
        db.execSQL(Dnem.SQL_DELETE_TRACKING_LOG);
        onCreate(db);
    }

}
