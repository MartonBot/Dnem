package com.martonbot.dnem;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.martonbot.dnem.DnemDatabase.Activity;
import com.martonbot.dnem.DnemDatabase.Schedule;
import com.martonbot.dnem.DnemDatabase.TrackingLog;

public class DnemDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Dnem.db";

    static final String activitiesJoinTable = Activity.T_NAME
            + " JOIN "
            + Schedule.T_NAME
            + " ON " + Activity.T_NAME + "." + Activity._ID
            + " = " + Schedule.T_NAME + "." + Schedule.C_ACTIVITY_ID
            + " LEFT OUTER JOIN "
            + TrackingLog.T_NAME
            + " ON " + Activity.T_NAME + "." + Activity._ID
            + " = " + TrackingLog.T_NAME + "." + TrackingLog.C_ACTIVITY_ID;

    static final String activitiesTable = Activity.T_NAME;

    static final String[] activitiesProjectionIdOnly = {
            Activity.T_NAME + "." + Activity._ID
    };

    static final String[] activitiesProjection = {
            Activity.T_NAME + "." + Activity._ID,
            Activity.C_LABEL,
            Activity.C_DETAILS,
            Schedule.C_IS_ACTIVE,
            Schedule.C_ALLOW_STARS,
            Schedule.C_WEEKENDS_ON
    };

    static final String activitiesGroupBy = Activity.T_NAME + "." + Activity._ID;

    static final String activitiesOrderBy = Activity.C_LABEL + " ASC";

    public DnemDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DnemDatabase.SQL_CREATE_ACTIVITY);
        db.execSQL(DnemDatabase.SQL_CREATE_SCHEDULE);
        db.execSQL(DnemDatabase.SQL_CREATE_TRACKING_LOG);
        db.execSQL(DnemDatabase.SQL_CREATE_INDEX_TRACKING_LOG);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(DnemDatabase.SQL_DELETE_ACTIVITY);
        db.execSQL(DnemDatabase.SQL_DELETE_SCHEDULE);
        db.execSQL(DnemDatabase.SQL_DELETE_TRACKING_LOG);
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.execSQL("PRAGMA foreign_keys=ON");
    }
}
